import type { ProfileInput, PurchaseCheck } from "@/lib/money";

const STORAGE_KEY = "ringly-mvp-state";
const SESSION_KEY = "spendly-api-session";
const API_BOOT_TIMEOUT_MS = 4000;
const API_AUTH_TIMEOUT_MS = 15000;

export type ApiSession = {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    refNo: string;
  };
};

export type PersistedState = {
  profile: ProfileInput | null;
  history: PurchaseCheck[];
  session: ApiSession | null;
  mode: "local" | "backend";
};

type StoredState = {
  profile: ProfileInput | null;
  history: PurchaseCheck[];
};

type SendOtpResponse = {
  error?: string;
  ok?: boolean;
  requestCount?: number;
  requestLimit?: number;
  resetAt?: number;
  resetInSeconds?: number;
};

type AuthSessionResponse = {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    refNo: string;
  };
};

function getApiBaseUrl() {
  return (process.env.NEXT_PUBLIC_API_BASE_URL ?? "").replace(/\/$/, "");
}

function hasApiEnv() {
  return Boolean(getApiBaseUrl());
}

function getLocalState(): StoredState {
  if (typeof window === "undefined") {
    return { profile: null, history: [] };
  }

  const raw = window.localStorage.getItem(STORAGE_KEY);

  if (!raw) {
    return { profile: null, history: [] };
  }

  try {
    return JSON.parse(raw) as StoredState;
  } catch {
    return { profile: null, history: [] };
  }
}

function setLocalState(state: StoredState) {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function getSession(): ApiSession | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = window.localStorage.getItem(SESSION_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as ApiSession;
  } catch {
    window.localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

function setSession(session: ApiSession) {
  window.localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

function clearSession() {
  window.localStorage.removeItem(SESSION_KEY);
}

function withTimeout<T>(promise: Promise<T>, timeoutMs: number): Promise<T> {
  return new Promise((resolve, reject) => {
    const timeout = window.setTimeout(() => {
      reject(new Error("Backend request timed out."));
    }, timeoutMs);

    promise
      .then(resolve)
      .catch(reject)
      .finally(() => window.clearTimeout(timeout));
  });
}

async function readError(response: Response, fallback: string) {
  try {
    const body = (await response.json()) as { error?: string };
    return body.error ?? fallback;
  } catch {
    return fallback;
  }
}

async function apiFetch<T>(
  path: string,
  init: RequestInit = {},
  timeoutMs = API_BOOT_TIMEOUT_MS,
): Promise<T> {
  const session = getSession();
  const headers = new Headers(init.headers);

  if (!headers.has("Content-Type") && init.body) {
    headers.set("Content-Type", "application/json");
  }

  if (session) {
    headers.set("Authorization", `Bearer ${session.accessToken}`);
  }

  const response = await withTimeout(
    fetch(`${getApiBaseUrl()}${path}`, {
      ...init,
      headers,
    }),
    timeoutMs,
  );

  if (!response.ok) {
    throw new Error(await readError(response, "Backend request failed."));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

function toDateOnly(value: string) {
  return value.slice(0, 10);
}

function normalizeProfile(profile: ProfileInput): ProfileInput {
  return {
    ...profile,
    lastBalanceUpdate: toDateOnly(profile.lastBalanceUpdate),
  };
}

export async function loadPersistedState(): Promise<PersistedState> {
  if (!hasApiEnv()) {
    const local = getLocalState();
    return { ...local, session: null, mode: "local" };
  }

  const session = getSession();

  if (!session) {
    return { profile: null, history: [], session: null, mode: "backend" };
  }

  try {
    const [profile, history] = await Promise.all([
      apiFetch<ProfileInput | null>("/api/profile").catch((error) => {
        if (error instanceof Error && error.message.includes("Backend request failed")) {
          return null;
        }

        throw error;
      }),
      apiFetch<PurchaseCheck[]>("/api/purchase-checks"),
    ]);

    return {
      profile,
      history,
      session,
      mode: "backend",
    };
  } catch {
    const local = getLocalState();
    return { ...local, session, mode: "local" };
  }
}

export async function saveProfile(profile: ProfileInput) {
  if (!hasApiEnv() || !getSession()) {
    const local = getLocalState();
    setLocalState({ ...local, profile });
    return;
  }

  await apiFetch("/api/profile", {
    body: JSON.stringify(normalizeProfile(profile)),
    method: "PUT",
  });
}

export async function savePurchaseCheck(entry: PurchaseCheck, profile: ProfileInput) {
  if (!hasApiEnv() || !getSession()) {
    const local = getLocalState();
    setLocalState({
      profile,
      history: [entry, ...local.history].slice(0, 8),
    });
    return;
  }

  await apiFetch("/api/purchase-checks", {
    body: JSON.stringify({
      amount: entry.amount,
      verdict: entry.verdict,
      consequence: entry.consequence,
      checkedAt: entry.checkedAt,
    }),
    method: "POST",
  });
}

export async function clearPersistedState() {
  window.localStorage.removeItem(STORAGE_KEY);

  if (!hasApiEnv() || !getSession()) {
    return;
  }

  await apiFetch("/api/me/data", {
    method: "DELETE",
  });
}

export async function sendSignInOtp(
  email: string,
  reason: "initial" | "resend" = "initial",
) {
  if (!hasApiEnv()) {
    return { error: new Error("Backend API is not configured.") };
  }

  try {
    const response = await withTimeout(
      fetch(`${getApiBaseUrl()}/api/auth/send-otp`, {
        body: JSON.stringify({ email, reason }),
        headers: {
          "Content-Type": "application/json",
        },
        method: "POST",
      }),
      API_AUTH_TIMEOUT_MS,
    );

    if (!response.ok) {
      const body = (await response.json()) as SendOtpResponse;
      return {
        error: new Error(body.error ?? "Could not send sign-in email."),
        resetAt: body.resetAt,
        resetInSeconds: body.resetInSeconds,
      };
    }

    const body = (await response.json()) as SendOtpResponse;

    return {
      error: null,
      requestCount: body.requestCount,
      requestLimit: body.requestLimit,
      resetAt: body.resetAt,
    };
  } catch (error) {
    return {
      error:
        error instanceof Error
          ? error
          : new Error("Could not send sign-in email."),
    };
  }
}

export async function verifyEmailOtp(email: string, token: string) {
  if (!hasApiEnv()) {
    return { error: new Error("Backend API is not configured.") };
  }

  try {
    const response = await withTimeout(
      fetch(`${getApiBaseUrl()}/api/auth/verify-otp`, {
        body: JSON.stringify({ email, token }),
        headers: {
          "Content-Type": "application/json",
        },
        method: "POST",
      }),
      API_AUTH_TIMEOUT_MS,
    );

    if (!response.ok) {
      return {
        error: new Error(await readError(response, "Code verification failed.")),
      };
    }

    const body = (await response.json()) as AuthSessionResponse;
    setSession({
      accessToken: body.accessToken,
      refreshToken: body.refreshToken,
      user: body.user,
    });

    return { error: null };
  } catch (error) {
    return {
      error:
        error instanceof Error
          ? error
          : new Error("Code verification failed."),
    };
  }
}

export async function signOut() {
  clearSession();
}

export function hasBackendConfig() {
  return hasApiEnv();
}
