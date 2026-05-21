import { createBrowserClient } from "@supabase/ssr";
import type { Session, SupabaseClient } from "@supabase/supabase-js";
import type { ProfileInput, PurchaseCheck } from "@/lib/money";

const STORAGE_KEY = "ringly-mvp-state";

export type PersistedState = {
  profile: ProfileInput | null;
  history: PurchaseCheck[];
  session: Session | null;
  mode: "local" | "supabase";
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

let browserClient: SupabaseClient | null | undefined;
const SUPABASE_BOOT_TIMEOUT_MS = 4000;

function getSupabasePublishableKey() {
  return (
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY ||
    process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_DEFAULT_KEY ||
    ""
  );
}

function hasSupabaseEnv() {
  return Boolean(
    process.env.NEXT_PUBLIC_SUPABASE_URL && getSupabasePublishableKey(),
  );
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

function withTimeout<T>(promise: Promise<T>, timeoutMs: number): Promise<T> {
  return new Promise((resolve, reject) => {
    const timeout = window.setTimeout(() => {
      reject(new Error("Supabase request timed out."));
    }, timeoutMs);

    promise
      .then(resolve)
      .catch(reject)
      .finally(() => window.clearTimeout(timeout));
  });
}

export function getSupabaseBrowserClient() {
  if (!hasSupabaseEnv()) {
    return null;
  }

  if (browserClient !== undefined) {
    return browserClient;
  }

  browserClient = createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    getSupabasePublishableKey(),
  );

  return browserClient;
}

export async function loadPersistedState(): Promise<PersistedState> {
  const client = getSupabaseBrowserClient();

  if (!client) {
    const local = getLocalState();
    return { ...local, session: null, mode: "local" };
  }

  let session: Session | null = null;

  try {
    const result = await withTimeout(
      client.auth.getSession(),
      SUPABASE_BOOT_TIMEOUT_MS,
    );
    session = result.data.session;
  } catch {
    const local = getLocalState();
    return { ...local, session: null, mode: "local" };
  }

  if (!session) {
    return { profile: null, history: [], session: null, mode: "supabase" };
  }

  let profileResult;
  let checksResult;

  try {
    [profileResult, checksResult] = await withTimeout(
      Promise.all([
        client
          .from("ringly_profiles")
          .select(
            "monthly_income, monthly_commitments, current_balance, protected_buffer, last_balance_update",
          )
          .eq("user_id", session.user.id)
          .maybeSingle(),
        client
          .from("ringly_purchase_checks")
          .select("id, amount, verdict, consequence, checked_at")
          .eq("user_id", session.user.id)
          .order("checked_at", { ascending: false })
          .limit(8),
      ]),
      SUPABASE_BOOT_TIMEOUT_MS,
    );
  } catch {
    const local = getLocalState();
    return { ...local, session, mode: "local" };
  }

  const profile = profileResult.data
    ? {
        monthlyIncome: Number(profileResult.data.monthly_income),
        monthlyCommitments: Number(profileResult.data.monthly_commitments),
        currentBalance: Number(profileResult.data.current_balance),
        protectedBuffer: Number(profileResult.data.protected_buffer),
        lastBalanceUpdate: profileResult.data.last_balance_update,
      }
    : null;

  const history =
    checksResult.data?.map((item) => ({
      id: item.id,
      amount: Number(item.amount),
      verdict: item.verdict as PurchaseCheck["verdict"],
      consequence: item.consequence,
      checkedAt: item.checked_at,
    })) ?? [];

  return {
    profile,
    history,
    session,
    mode: "supabase",
  };
}

export async function saveProfile(profile: ProfileInput) {
  const client = getSupabaseBrowserClient();

  if (!client) {
    const local = getLocalState();
    setLocalState({ ...local, profile });
    return;
  }

  const {
    data: { session },
  } = await client.auth.getSession();

  if (!session) return;

  await client.from("ringly_profiles").upsert({
    user_id: session.user.id,
    monthly_income: profile.monthlyIncome,
    monthly_commitments: profile.monthlyCommitments,
    current_balance: profile.currentBalance,
    protected_buffer: profile.protectedBuffer,
    last_balance_update: profile.lastBalanceUpdate,
    updated_at: new Date().toISOString(),
  });
}

export async function savePurchaseCheck(entry: PurchaseCheck, profile: ProfileInput) {
  const client = getSupabaseBrowserClient();

  if (!client) {
    const local = getLocalState();
    setLocalState({
      profile,
      history: [entry, ...local.history].slice(0, 8),
    });
    return;
  }

  const {
    data: { session },
  } = await client.auth.getSession();

  if (!session) return;

  await client.from("ringly_purchase_checks").insert({
    id: entry.id,
    user_id: session.user.id,
    amount: entry.amount,
    verdict: entry.verdict,
    consequence: entry.consequence,
    checked_at: entry.checkedAt,
  });
}

export async function clearPersistedState() {
  window.localStorage.removeItem(STORAGE_KEY);

  const client = getSupabaseBrowserClient();

  if (!client) {
    return;
  }

  const {
    data: { session },
  } = await client.auth.getSession();

  if (!session) return;

  await Promise.all([
    client.from("ringly_purchase_checks").delete().eq("user_id", session.user.id),
    client.from("ringly_profiles").delete().eq("user_id", session.user.id),
  ]);
}

export async function signInWithMagicLink(
  email: string,
  reason: "initial" | "resend" = "initial",
) {
  if (!hasSupabaseEnv()) {
    return { error: new Error("Supabase is not configured.") };
  }

  try {
    const response = await withTimeout(
      fetch("/api/auth/send-otp", {
        body: JSON.stringify({ email, reason }),
        headers: {
          "Content-Type": "application/json",
        },
        method: "POST",
      }),
      SUPABASE_BOOT_TIMEOUT_MS,
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
  const client = getSupabaseBrowserClient();

  if (!client) {
    return { error: new Error("Supabase is not configured.") };
  }

  return withTimeout(
    client.auth.verifyOtp({
      email,
      token,
      type: "email",
    }),
    SUPABASE_BOOT_TIMEOUT_MS,
  );
}

export async function signOut() {
  const client = getSupabaseBrowserClient();

  if (!client) return;

  await client.auth.signOut();
}

export function hasSupabaseConfig() {
  return hasSupabaseEnv();
}

export function getAuthRedirectUrl() {
  return typeof window !== "undefined"
    ? `${window.location.origin}/auth/callback`
    : undefined;
}
