"use client";

import { FormEvent, ReactNode, useEffect, useRef, useState } from "react";
import {
  checkPurchase,
  createSnapshot,
  currency,
  ProfileInput,
  PurchaseCheck,
  recoveryMessage,
  shouldEnterRecovery,
  Snapshot,
  Verdict,
} from "@/lib/money";
import {
  clearPersistedState,
  hasBackendConfig,
  loadPersistedState,
  saveProfile,
  savePurchaseCheck,
  sendSignInOtp,
  signOut,
  verifyEmailOtp,
} from "@/lib/persistence";

const defaultProtectedBuffer = 300;
const maxOtpAttempts = 3;
const minOtpLength = 6;
const maxOtpLength = 6;
const otpResetMs = 60_000;
const otpResendDelayMs = 30_000;
const spendToastDurationMs = 3600;

const safeSpendMessages = [
  "Spend recorded. You spent within today's safe range.",
  "Spend recorded. You're still on track.",
  "Spend recorded. Your safe-to-spend amount has been updated.",
  "Spend recorded. Nice, this stays within your limit.",
  "Spend recorded. You made a controlled spend.",
  "Spend recorded. You spent wisely and stayed in range.",
  "Spend recorded. Your budget still has breathing room.",
];

const cautionSpendMessages = [
  "Spend recorded. You're getting close to protected money.",
  "Spend recorded. Your spendable amount is under pressure.",
  "Spend recorded. You're moving faster toward RM0 spendable.",
  "Spend recorded. This makes the next few days tighter.",
  "Spend recorded. Your buffer needs attention now.",
  "Spend recorded. You're spending beyond the safe pace.",
  "Spend recorded. Slow down, your spendable room is shrinking.",
];

function verdictClasses(verdict: Verdict) {
  if (verdict === "Safe") {
    return "bg-signal-safe text-[#eef7f1]";
  }

  if (verdict === "Risky") {
    return "bg-signal-risk text-[#fff3ec]";
  }

  return "bg-signal-danger text-[#fff0ec]";
}

function statusTone(status: Snapshot["status"]) {
  if (status === "Safe") {
    return "text-signal-safe";
  }

  if (status === "Close") {
    return "text-signal-risk";
  }

  return "text-signal-danger";
}

function confidenceHint(confidence: Snapshot["confidence"]) {
  if (confidence === "Fresh") {
    return "Based on a recently updated balance.";
  }

  if (confidence === "Aging") {
    return "Still usable, but your balance should be refreshed soon.";
  }

  return "This estimate may be outdated until you refresh your balance.";
}

function sanitizeMoneyInput(value: string) {
  return value.replace(/[^\d.]/g, "");
}

function formatTime(value: number) {
  return new Intl.DateTimeFormat("en", {
    hour: "numeric",
    minute: "2-digit",
    second: "2-digit",
  }).format(new Date(value));
}

function readOptionalMoneyValue(value: FormDataEntryValue | null, fallback: number) {
  if (value === null || value === "") {
    return fallback;
  }

  return Number(value);
}

function createClientId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }

  return `check-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

function pickRandomMessage(messages: string[]) {
  return messages[Math.floor(Math.random() * messages.length)];
}

type UpdateReturnState = {
  profile: ProfileInput;
  history: PurchaseCheck[];
  result: ReturnType<typeof checkPurchase> | null;
  balanceDraft: string;
  purchaseAmount: string;
  checkedPurchaseAmount: number | null;
};

type SpendToast = {
  message: string;
  tone: "safe" | "caution";
};

type TapTooltipProps = {
  id: string;
  label: string;
  children: ReactNode;
};

function TapTooltip({ id, label, children }: TapTooltipProps) {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!open) return;

    function closeTooltip() {
      setOpen(false);
    }

    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        closeTooltip();
      }
    }

    window.addEventListener("scroll", closeTooltip, { passive: true });
    window.addEventListener("pointerdown", closeTooltip);
    window.addEventListener("keydown", closeOnEscape);

    return () => {
      window.removeEventListener("scroll", closeTooltip);
      window.removeEventListener("pointerdown", closeTooltip);
      window.removeEventListener("keydown", closeOnEscape);
    };
  }, [open]);

  return (
    <div className="group relative">
      <button
        aria-describedby={id}
        aria-expanded={open}
        aria-label={label}
        className="flex h-5 w-5 items-center justify-center rounded-full border border-white/14 bg-white/8 text-[11px] font-semibold text-[#eef0de]"
        onClick={(event) => {
          event.stopPropagation();
          setOpen((current) => !current);
        }}
        onPointerDown={(event) => {
          event.stopPropagation();
        }}
        type="button"
      >
        ?
      </button>
      <div
        id={id}
        role="tooltip"
        className={`pointer-events-none absolute left-1/2 top-7 z-10 w-64 -translate-x-1/2 rounded-2xl border border-white/10 bg-[#f4e4c9] p-3 text-left text-xs leading-5 text-[#5b4031] shadow-[0_14px_40px_rgba(12,25,17,0.28)] transition-opacity group-hover:opacity-100 group-focus-within:opacity-100 ${
          open ? "opacity-100" : "opacity-0"
        }`}
      >
        {children}
      </div>
    </div>
  );
}

export function MvpConsole() {
  const safeToSpendRef = useRef<HTMLDivElement | null>(null);
  const [profile, setProfile] = useState<ProfileInput | null>(null);
  const [history, setHistory] = useState<PurchaseCheck[]>([]);
  const [purchaseAmount, setPurchaseAmount] = useState("18");
  const [checkedPurchaseAmount, setCheckedPurchaseAmount] = useState<
    number | null
  >(null);
  const [balanceDraft, setBalanceDraft] = useState("");
  const [result, setResult] = useState<ReturnType<typeof checkPurchase> | null>(
    null,
  );
  const [loading, setLoading] = useState(true);
  const [signedIn, setSignedIn] = useState(false);
  const [email, setEmail] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [otpRequested, setOtpRequested] = useState(false);
  const [otpAttempts, setOtpAttempts] = useState(0);
  const [otpLockedUntil, setOtpLockedUntil] = useState<number | null>(null);
  const [otpResendAvailableAt, setOtpResendAvailableAt] = useState<
    number | null
  >(null);
  const [otpEmailLimitResetAt, setOtpEmailLimitResetAt] = useState<
    number | null
  >(null);
  const [lockoutNow, setLockoutNow] = useState(() => Date.now());
  const [authMessage, setAuthMessage] = useState("");
  const [authSubmitting, setAuthSubmitting] = useState(false);
  const [updateReturnState, setUpdateReturnState] =
    useState<UpdateReturnState | null>(null);
  const [spendToast, setSpendToast] = useState<SpendToast | null>(null);

  useEffect(() => {
    let active = true;

    async function hydrate() {
      try {
        const state = await loadPersistedState();

        if (!active) return;

        setProfile(state.profile);
        setHistory(state.history);
        setSignedIn(Boolean(state.session));
        setBalanceDraft(
          state.profile ? String(state.profile.currentBalance) : "",
        );

        const authError = new URLSearchParams(window.location.search).get(
          "auth_error",
        );
        if (authError) {
          setAuthMessage(authError);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void hydrate();

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!otpLockedUntil && !otpResendAvailableAt && !otpEmailLimitResetAt) return;

    const timer = window.setInterval(() => {
      const nextNow = Date.now();
      setLockoutNow(nextNow);

      if (otpLockedUntil && nextNow >= otpLockedUntil) {
        setOtpLockedUntil(null);
        setOtpAttempts(0);
        setAuthMessage("You can try another code now.");
      }
    }, 1000);

    return () => window.clearInterval(timer);
  }, [otpLockedUntil, otpResendAvailableAt, otpEmailLimitResetAt]);

  useEffect(() => {
    if (!spendToast) return;

    const timeout = window.setTimeout(() => {
      setSpendToast(null);
    }, spendToastDurationMs);

    return () => window.clearTimeout(timeout);
  }, [spendToast]);

  const snapshot = profile ? createSnapshot(profile) : null;
  const inRecovery = snapshot ? shouldEnterRecovery(history, snapshot) : false;
  const usesBackend = hasBackendConfig();
  const otpLocked = otpLockedUntil !== null && lockoutNow < otpLockedUntil;
  const otpSecondsRemaining = otpLockedUntil
    ? Math.max(0, Math.ceil((otpLockedUntil - lockoutNow) / 1000))
    : 0;
  const canResendOtp =
    otpRequested &&
    otpResendAvailableAt !== null &&
    (!otpEmailLimitResetAt || lockoutNow >= otpEmailLimitResetAt) &&
    lockoutNow >= otpResendAvailableAt;
  const resendSecondsRemaining =
    otpRequested && otpResendAvailableAt
      ? Math.max(
          0,
          Math.ceil(
            (Math.max(otpResendAvailableAt, otpEmailLimitResetAt ?? 0) -
              lockoutNow) /
              1000,
          ),
        )
      : 0;

  async function submitOnboarding(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);

    const nextProfile: ProfileInput = {
      monthlyIncome: Number(formData.get("monthlyIncome")),
      monthlyCommitments: Number(formData.get("monthlyCommitments")),
      currentBalance: Number(formData.get("currentBalance")),
      protectedBuffer: readOptionalMoneyValue(
        formData.get("protectedBuffer"),
        defaultProtectedBuffer,
      ),
      lastBalanceUpdate: new Date().toISOString(),
    };

    if (updateReturnState) {
      await clearPersistedState();
    }

    setProfile(nextProfile);
    setHistory([]);
    setBalanceDraft(String(nextProfile.currentBalance));
    setResult(null);
    setCheckedPurchaseAmount(null);
    setUpdateReturnState(null);
    await saveProfile(nextProfile);
  }

  async function submitPurchaseCheck(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!profile) return;

    const amount = Number(purchaseAmount);
    const nextResult = checkPurchase(profile, amount);
    const nextEntry: PurchaseCheck = {
      id: createClientId(),
      amount,
      verdict: nextResult.verdict,
      consequence: nextResult.consequence,
      checkedAt: new Date().toISOString(),
    };

    setResult(nextResult);
    setCheckedPurchaseAmount(amount);
    setHistory((current) => [nextEntry, ...current].slice(0, 8));
    await savePurchaseCheck(nextEntry, profile);
  }

  async function recordSpend() {
    if (!profile || checkedPurchaseAmount === null || !result) return;

    const nextProfile = {
      ...profile,
      currentBalance: profile.currentBalance - checkedPurchaseAmount,
      lastBalanceUpdate: new Date().toISOString(),
    };
    const isSafeSpend = result.verdict === "Safe";

    setProfile(nextProfile);
    setBalanceDraft(String(nextProfile.currentBalance));
    setResult(null);
    setCheckedPurchaseAmount(null);
    setSpendToast({
      message: pickRandomMessage(
        isSafeSpend ? safeSpendMessages : cautionSpendMessages,
      ),
      tone: isSafeSpend ? "safe" : "caution",
    });

    window.requestAnimationFrame(() => {
      safeToSpendRef.current?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    });

    await saveProfile(nextProfile);
  }

  async function updateBalance(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!profile) return;

    const nextProfile = {
      ...profile,
      currentBalance: Number(balanceDraft),
      lastBalanceUpdate: new Date().toISOString(),
    };

    setProfile(nextProfile);
    setResult(null);
    await saveProfile(nextProfile);
  }

  async function clearData() {
    try {
      await clearPersistedState();
      setProfile(null);
      setHistory([]);
      setResult(null);
      setCheckedPurchaseAmount(null);
      setSpendToast(null);
      setBalanceDraft("");
      setPurchaseAmount("18");
      setOtpCode("");
      setOtpRequested(false);
      setOtpAttempts(0);
      setOtpLockedUntil(null);
      setOtpResendAvailableAt(null);
      setOtpEmailLimitResetAt(null);
      setAuthMessage("");
    } catch {
      setAuthMessage("Could not update data. Please try again.");
    }
  }

  function startUpdateFlow() {
    if (!profile) {
      void clearData();
      return;
    }

    setUpdateReturnState({
      profile,
      history,
      result,
      balanceDraft,
      purchaseAmount,
      checkedPurchaseAmount,
    });
    setProfile(null);
    setHistory([]);
    setResult(null);
    setCheckedPurchaseAmount(null);
    setSpendToast(null);
    setBalanceDraft("");
    setPurchaseAmount("18");
  }

  function cancelUpdateFlow() {
    if (!updateReturnState) return;

    setProfile(updateReturnState.profile);
    setHistory(updateReturnState.history);
    setResult(updateReturnState.result);
    setSpendToast(null);
    setBalanceDraft(updateReturnState.balanceDraft);
    setPurchaseAmount(updateReturnState.purchaseAmount);
    setCheckedPurchaseAmount(updateReturnState.checkedPurchaseAmount);
    setUpdateReturnState(null);
  }

  async function submitAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setAuthSubmitting(true);
    setAuthMessage("Sending sign-in email...");

    try {
      const { error, requestCount, requestLimit, resetAt } =
        await sendSignInOtp(email);

      if (error) {
        setAuthMessage(error.message);
        return;
      }

      setOtpRequested(true);
      setOtpCode("");
      setOtpAttempts(0);
      setOtpLockedUntil(null);
      setOtpResendAvailableAt(Date.now() + otpResendDelayMs);
      setOtpEmailLimitResetAt(
        requestCount && requestLimit && requestCount >= requestLimit
          ? (resetAt ?? null)
          : null,
      );
      setAuthMessage(
        `Email sent. Request ${requestCount ?? 1}/${requestLimit ?? 2}. ${
          resetAt ? `Email request limit resets at ${formatTime(resetAt)}. ` : ""
        }Enter the 6-digit code from your email.`,
      );
    } catch (error) {
      setAuthMessage(
        error instanceof Error
          ? error.message
          : "Sign-in failed. Check backend API settings.",
      );
    } finally {
      setAuthSubmitting(false);
    }
  }

  async function resendOtp() {
    if (!canResendOtp) return;

    setAuthSubmitting(true);
    setAuthMessage("Resending sign-in email...");

    try {
      const { error, requestCount, requestLimit, resetAt } =
        await sendSignInOtp(email, "resend");

      if (error) {
        setAuthMessage(error.message);
        return;
      }

      setOtpCode("");
      setOtpAttempts(0);
      setOtpLockedUntil(null);
      setOtpResendAvailableAt(Date.now() + otpResendDelayMs);
      setOtpEmailLimitResetAt(
        requestCount && requestLimit && requestCount >= requestLimit
          ? (resetAt ?? null)
          : null,
      );
      setAuthMessage(
        `New email sent. Request ${requestCount ?? 2}/${requestLimit ?? 2}. ${
          resetAt ? `Email request limit resets at ${formatTime(resetAt)}. ` : ""
        }Use the latest code from your email.`,
      );
    } catch (error) {
      setAuthMessage(
        error instanceof Error
          ? error.message
          : "Could not resend email. Check backend API settings.",
      );
    } finally {
      setAuthSubmitting(false);
    }
  }

  async function submitOtp(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (otpLocked) {
      setAuthMessage(`Too many attempts. Try again in ${otpSecondsRemaining}s.`);
      return;
    }

    setAuthSubmitting(true);
    setAuthMessage("Verifying code...");

    try {
      const { error } = await verifyEmailOtp(email, otpCode);

      if (error) {
        const nextAttempts = otpAttempts + 1;
        setOtpAttempts(nextAttempts);

        if (nextAttempts >= maxOtpAttempts) {
          setOtpLockedUntil(Date.now() + otpResetMs);
          setAuthMessage("Too many wrong codes. Try again in 60s.");
          return;
        }

        setAuthMessage(
          `${error.message}. ${maxOtpAttempts - nextAttempts} attempt${
            maxOtpAttempts - nextAttempts === 1 ? "" : "s"
          } left.`,
        );
        return;
      }

      const state = await loadPersistedState();
      setProfile(state.profile);
      setHistory(state.history);
      setSignedIn(Boolean(state.session));
      setBalanceDraft(
        state.profile ? String(state.profile.currentBalance) : "",
      );
      setOtpCode("");
      setCheckedPurchaseAmount(null);
      setSpendToast(null);
      setOtpRequested(false);
      setOtpAttempts(0);
      setOtpLockedUntil(null);
      setOtpResendAvailableAt(null);
      setOtpEmailLimitResetAt(null);
      setAuthMessage("");
    } catch (error) {
      setAuthMessage(
        error instanceof Error
          ? error.message
          : "Code verification failed. Check backend API settings.",
      );
    } finally {
      setAuthSubmitting(false);
    }
  }

  async function handleSignOut() {
    await signOut();
    setSignedIn(false);
    setProfile(null);
    setHistory([]);
    setResult(null);
    setCheckedPurchaseAmount(null);
    setSpendToast(null);
    setBalanceDraft("");
  }

  if (loading) {
    return (
      <section className="rounded-[2rem] border border-line bg-surface px-6 py-10 shadow-[0_20px_80px_rgba(112,77,37,0.08)]">
        <p className="font-mono text-xs uppercase tracking-[0.28em] text-foreground/55">
          Loading workspace
        </p>
        <p className="mt-4 text-base leading-7 text-foreground/72">
          Preparing your safe-to-spend console.
        </p>
      </section>
    );
  }

  return (
    <section className="grid gap-6">
      <div className="rounded-[2rem] border border-line bg-surface px-6 py-7 shadow-[0_20px_80px_rgba(112,77,37,0.08)]">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="font-mono text-xs uppercase tracking-[0.28em] text-foreground/55">
              Spending Advisor
            </p>
            <h2 className="mt-3 text-3xl font-semibold tracking-[-0.05em] text-foreground sm:text-4xl">
              Build the habit before the spend.
            </h2>
          </div>
          {usesBackend && signedIn ? (
            <button
              className="rounded-full border border-line bg-surface-strong px-4 py-2 text-sm font-semibold text-foreground/75 transition-colors hover:bg-white"
              onClick={handleSignOut}
              type="button"
            >
              Sign out
            </button>
          ) : null}
        </div>

        {usesBackend && !signedIn ? (
          <div className="mt-8 grid gap-5">
            <div className="rounded-[1.6rem] border border-line bg-[#fff7ea] p-5">
              <p className="font-mono text-xs uppercase tracking-[0.28em] text-accent-deep">
                Sign In
              </p>
              <h3 className="mt-3 text-2xl font-semibold tracking-[-0.04em] text-foreground">
                Save your financial control loop to Spendly.
              </h3>
              <p className="mt-3 text-base leading-7 text-foreground/72">
                This app uses passwordless email sign-in. Enter the 6-digit code
                from your email to continue.
              </p>

              <form className="mt-5 grid gap-3 sm:max-w-lg" onSubmit={submitAuth}>
                <input
                  className="rounded-2xl border border-line bg-white px-4 py-3 outline-none transition-colors focus:border-accent"
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="you@example.com"
                  required
                  type="email"
                  value={email}
                />
                <button
                  className="rounded-full bg-accent px-6 py-3 text-sm font-semibold text-[#fff8f2] transition-transform hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
                  disabled={authSubmitting}
                  type="submit"
                >
                  {authSubmitting ? "Sending..." : "Send sign-in email"}
                </button>
              </form>

              {otpRequested ? (
                <form className="mt-4 grid gap-3 sm:max-w-lg" onSubmit={submitOtp}>
                  <input
                    className="rounded-2xl border border-line bg-white px-4 py-3 font-mono text-base tracking-[0.22em] outline-none transition-colors focus:border-accent disabled:opacity-60"
                    disabled={otpLocked}
                    inputMode="numeric"
                    maxLength={maxOtpLength}
                    onChange={(event) =>
                      setOtpCode(event.target.value.replace(/\D/g, ""))
                    }
                    placeholder="000000"
                    required
                    type="text"
                    value={otpCode}
                  />
                  <button
                    className="rounded-full border border-line bg-surface-strong px-6 py-3 text-sm font-semibold text-foreground/80 transition-colors hover:bg-white disabled:cursor-not-allowed disabled:opacity-60"
                    disabled={
                      authSubmitting ||
                      otpLocked ||
                      otpCode.length < minOtpLength ||
                      otpCode.length > maxOtpLength
                    }
                    type="submit"
                  >
                    {otpLocked
                      ? `Try again in ${otpSecondsRemaining}s`
                      : "Verify code"}
                  </button>

                  {canResendOtp ? (
                    <button
                      className="rounded-full border border-line bg-white px-6 py-3 text-sm font-semibold text-foreground/75 transition-colors hover:bg-surface-strong disabled:cursor-not-allowed disabled:opacity-60"
                      disabled={authSubmitting}
                      onClick={resendOtp}
                      type="button"
                    >
                      Resend email
                    </button>
                  ) : (
                    <p className="text-sm leading-6 text-foreground/60">
                      Resend available in {resendSecondsRemaining}s.
                    </p>
                  )}
                </form>
              ) : null}

              {authMessage ? (
                <p className="mt-4 text-sm leading-6 text-foreground/68">
                  {authMessage}
                </p>
              ) : null}
            </div>
          </div>
        ) : !profile ? (
          <form className="mt-8 grid gap-4" onSubmit={submitOnboarding}>
            <p className="max-w-2xl text-base leading-7 text-foreground/72">
              Start with the minimum setup: monthly income, monthly commitments,
              current balance, and an optional protected buffer.
            </p>

            <label className="grid gap-2">
              <span className="text-sm font-semibold text-foreground/80">
                Monthly income or usable monthly money
              </span>
              <input
                className="rounded-2xl border border-line bg-white px-4 py-3 outline-none transition-colors focus:border-accent"
                defaultValue="4200"
                min="0"
                name="monthlyIncome"
                required
                step="1"
                type="number"
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-semibold text-foreground/80">
                Monthly commitments
              </span>
              <input
                className="rounded-2xl border border-line bg-white px-4 py-3 outline-none transition-colors focus:border-accent"
                defaultValue="2400"
                min="0"
                name="monthlyCommitments"
                required
                step="1"
                type="number"
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-semibold text-foreground/80">
                Current available balance
              </span>
              <input
                className="rounded-2xl border border-line bg-white px-4 py-3 outline-none transition-colors focus:border-accent"
                defaultValue="1180"
                min="0"
                name="currentBalance"
                required
                step="1"
                type="number"
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-semibold text-foreground/80">
                Protected buffer
              </span>
              <input
                className="rounded-2xl border border-line bg-white px-4 py-3 outline-none transition-colors focus:border-accent"
                defaultValue={defaultProtectedBuffer}
                min="0"
                name="protectedBuffer"
                step="1"
                type="number"
              />
            </label>

            <div className="mt-2 flex flex-wrap gap-3">
              <button
                className="rounded-full bg-accent px-6 py-3 text-sm font-semibold text-[#fff8f2] transition-transform hover:-translate-y-0.5"
                type="submit"
              >
                Calculate safe-to-spend
              </button>
              {updateReturnState ? (
                <button
                  className="rounded-full border border-line bg-white px-5 py-3 text-sm font-semibold text-foreground/70 transition-colors hover:bg-surface-strong"
                  onClick={cancelUpdateFlow}
                  type="button"
                >
                  Back
                </button>
              ) : null}
            </div>
          </form>
        ) : (
          <div className="mt-8 grid gap-6">
            <div className="grid gap-4 sm:grid-cols-[1.1fr_0.9fr]">
              <div
                ref={safeToSpendRef}
                className="rounded-[1.8rem] bg-[#13261a] p-5 text-[#f7f1e5] scroll-mt-6"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="font-mono text-[11px] uppercase tracking-[0.28em] text-[#d4ddc6]">
                      Safe-To-Spend Today
                    </p>
                    <p className="mt-4 text-5xl font-semibold tracking-[-0.05em]">
                      {currency(snapshot!.safeDaily)}
                    </p>
                  </div>
                  {profile ? (
                    <button
                      className="rounded-full border border-white/10 bg-white/6 px-3 py-2 font-mono text-[11px] uppercase tracking-[0.18em] text-[#eef0de]"
                      onClick={startUpdateFlow}
                      type="button"
                    >
                      Update data
                    </button>
                  ) : null}
                </div>

                <div className="mt-4 flex items-end justify-between gap-4">
                  <p
                    className={`text-sm font-semibold ${statusTone(snapshot!.status)}`}
                  >
                    {snapshot!.headline}
                  </p>
                  <div className="flex flex-col items-end gap-2 text-right">
                    <p className="font-mono text-[10px] uppercase tracking-[0.24em] text-[#c9d3c2]">
                      Confidence State
                    </p>
                    <div className="rounded-full bg-white/8 px-3 py-2 font-mono text-[11px] uppercase tracking-[0.18em] text-[#eef0de]">
                      {snapshot!.confidence}
                    </div>
                  </div>
                </div>
                <p className="mt-3 max-w-sm text-sm leading-6 text-[#cfd8ca]">
                  {confidenceHint(snapshot!.confidence)}
                </p>

                <div className="mt-5 grid gap-3 sm:grid-cols-3">
                  <div className="rounded-[1.2rem] border border-white/10 bg-white/6 p-4">
                    <p className="font-mono text-[11px] uppercase tracking-[0.22em] text-[#d4ddc6]">
                      Spendable
                    </p>
                    <p className="mt-2 text-2xl font-semibold">
                      {currency(snapshot!.spendableBalance)}
                    </p>
                  </div>
                  <div className="rounded-[1.2rem] border border-white/10 bg-white/6 p-4">
                    <div className="flex items-center gap-2">
                      <p className="font-mono text-[11px] uppercase tracking-[0.22em] text-[#d4ddc6]">
                        Protected money
                      </p>
                      <TapTooltip
                        id="protected-money-tip"
                        label="What is protected money?"
                      >
                        Protected money is the amount kept aside for bills,
                        commitments, and your safety buffer. It is not meant
                        for casual spending.
                      </TapTooltip>
                    </div>
                    <p className="mt-2 text-2xl font-semibold">
                      {currency(snapshot!.protectedBuffer)}
                    </p>
                  </div>
                  <div className="rounded-[1.2rem] border border-white/10 bg-white/6 p-4">
                    <p className="font-mono text-[11px] uppercase tracking-[0.22em] text-[#d4ddc6]">
                      Days Left
                    </p>
                    <p className="mt-2 text-2xl font-semibold">
                      {snapshot!.remainingDays}
                    </p>
                  </div>
                </div>
              </div>

              <form
                className="rounded-[1.8rem] border border-line bg-[#fff7ea] p-5"
                onSubmit={submitPurchaseCheck}
              >
                <p className="font-mono text-xs uppercase tracking-[0.28em] text-accent-deep">
                  Pre-Spend Check
                </p>
                <h3 className="mt-3 text-2xl font-semibold tracking-[-0.04em] text-foreground">
                  Test a purchase before you pay.
                </h3>
                <label className="mt-5 grid gap-2">
                  <span className="text-sm font-semibold text-foreground/75">
                    Purchase amount
                  </span>
                  <input
                    className="rounded-2xl border border-line bg-white px-4 py-3 outline-none transition-colors focus:border-accent"
                    min="0"
                    onChange={(event) => setPurchaseAmount(event.target.value)}
                    step="1"
                    type="number"
                    value={purchaseAmount}
                  />
                </label>

                <button
                  className="mt-5 rounded-full bg-accent px-6 py-3 text-sm font-semibold text-[#fff8f2] transition-transform hover:-translate-y-0.5"
                  type="submit"
                >
                  Check this spend
                </button>

                {result ? (
                  <div className="mt-5 rounded-[1.4rem] bg-[#1f2f24] p-4 text-[#f6f1e5]">
                    <div className="flex items-center justify-between gap-3">
                      <p className="font-mono text-[11px] uppercase tracking-[0.22em] text-[#cedac5]">
                        Verdict
                      </p>
                      <span
                        className={`rounded-full px-3 py-2 font-mono text-[11px] uppercase tracking-[0.18em] ${verdictClasses(
                          result.verdict,
                        )}`}
                      >
                        {result.verdict}
                      </span>
                    </div>
                    <p className="mt-4 text-xl font-semibold">
                      {currency(checkedPurchaseAmount ?? Number(purchaseAmount))}
                    </p>
                    <p className="mt-2 text-sm leading-6 text-[#e5ebde]">
                      {result.consequence}
                    </p>
                    <button
                      className="mt-4 rounded-full bg-accent px-5 py-3 text-sm font-semibold text-[#fff8f2] transition-transform hover:-translate-y-0.5"
                      onClick={recordSpend}
                      type="button"
                    >
                      Spend!
                    </button>
                  </div>
                ) : (
                  <p className="mt-5 text-sm leading-6 text-foreground/65">
                    Enter an amount and get a safe, risky, or not safe decision
                    with a plain consequence statement.
                  </p>
                )}
              </form>
            </div>

            <div className="grid gap-4 lg:grid-cols-[0.82fr_1.18fr]">
              <form
                className="rounded-[1.6rem] border border-line bg-surface-strong p-5"
                onSubmit={updateBalance}
              >
                <p className="font-mono text-xs uppercase tracking-[0.28em] text-foreground/55">
                  Balance Refresh
                </p>
                <p className="mt-3 text-base leading-7 text-foreground/72">
                  Keep this approximate, not perfect. A quick refresh restores
                  confidence faster than manual logging.
                </p>
                <label className="mt-4 grid gap-2">
                  <span className="text-sm font-semibold text-foreground/75">
                    Current balance now
                  </span>
                  <input
                    className="rounded-2xl border border-line bg-white px-4 py-3 outline-none transition-colors focus:border-accent"
                    inputMode="decimal"
                    onChange={(event) =>
                      setBalanceDraft(sanitizeMoneyInput(event.target.value))
                    }
                    placeholder="0"
                    type="text"
                    value={balanceDraft}
                  />
                </label>
                <p className="mt-3 text-sm leading-6 text-foreground/62">
                  If this still shows spendable as `RM0`, your balance is still
                  below protected money.
                </p>
                <button
                  className="mt-4 rounded-full border border-line bg-white px-5 py-3 text-sm font-semibold text-foreground/80 transition-colors hover:bg-[#fffdf7]"
                  type="submit"
                >
                  Update balance
                </button>
              </form>

              <div className="rounded-[1.6rem] border border-line bg-surface p-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="font-mono text-xs uppercase tracking-[0.28em] text-foreground/55">
                      Recovery + History
                    </p>
                    <h3 className="mt-3 text-2xl font-semibold tracking-[-0.04em] text-foreground">
                      Keep control when pace breaks.
                    </h3>
                  </div>
                  {inRecovery ? (
                    <span className="rounded-full bg-signal-danger px-3 py-2 font-mono text-[11px] uppercase tracking-[0.18em] text-[#fff0ec]">
                      Recovery
                    </span>
                  ) : (
                    <span className="rounded-full bg-signal-safe px-3 py-2 font-mono text-[11px] uppercase tracking-[0.18em] text-[#eef7f1]">
                      Stable
                    </span>
                  )}
                </div>

                <p className="mt-4 text-base leading-7 text-foreground/72">
                  {snapshot
                    ? recoveryMessage(history, snapshot)
                    : "Run checks and update your balance to see recovery guidance."}
                </p>

                <div className="mt-5 grid gap-3">
                  {history.length === 0 ? (
                    <div className="rounded-[1.2rem] border border-dashed border-line bg-[#fffdfa] px-4 py-4 text-sm text-foreground/60">
                      No checks yet. Run a purchase check to start your control
                      trail.
                    </div>
                  ) : (
                    history.map((item) => (
                      <div
                        key={item.id}
                        className="rounded-[1.2rem] border border-line bg-[#fffdfa] px-4 py-4"
                      >
                        <div className="flex items-center justify-between gap-3">
                          <p className="font-semibold text-foreground">
                            {currency(item.amount)}
                          </p>
                          <span
                            className={`rounded-full px-3 py-1.5 font-mono text-[11px] uppercase tracking-[0.18em] ${verdictClasses(
                              item.verdict,
                            )}`}
                          >
                            {item.verdict}
                          </span>
                        </div>
                        <p className="mt-2 text-sm leading-6 text-foreground/68">
                          {item.consequence}
                        </p>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {spendToast ? (
        <div
          aria-live="polite"
          className={`fixed left-1/2 top-5 z-50 w-[min(calc(100vw-2rem),26rem)] -translate-x-1/2 rounded-2xl border px-4 py-3 text-sm font-semibold shadow-[0_18px_55px_rgba(19,38,26,0.18)] ${
            spendToast.tone === "safe"
              ? "border-signal-safe/25 bg-[#eef7f1] text-signal-safe"
              : "border-signal-risk/30 bg-[#fff3ec] text-signal-danger"
          }`}
        >
          {spendToast.message}
        </div>
      ) : null}
    </section>
  );
}
