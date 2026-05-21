export type ConfidenceState = "Fresh" | "Aging" | "Stale";
export type Verdict = "Safe" | "Risky" | "Not safe";

export type ProfileInput = {
  monthlyIncome: number;
  monthlyCommitments: number;
  currentBalance: number;
  protectedBuffer: number;
  lastBalanceUpdate: string;
};

export type PurchaseCheck = {
  id: string;
  amount: number;
  verdict: Verdict;
  consequence: string;
  checkedAt: string;
};

export type Snapshot = {
  remainingDays: number;
  spendableBalance: number;
  safeDaily: number;
  confidence: ConfidenceState;
  protectedBuffer: number;
  status: "Safe" | "Close" | "Risky";
  headline: string;
};

const DAY_IN_MS = 1000 * 60 * 60 * 24;

function daysSince(dateString: string) {
  const then = new Date(dateString).getTime();
  const now = Date.now();
  return Math.max(0, Math.floor((now - then) / DAY_IN_MS));
}

export function getConfidence(lastBalanceUpdate: string): ConfidenceState {
  const diff = daysSince(lastBalanceUpdate);

  if (diff <= 0) return "Fresh";
  if (diff <= 3) return "Aging";
  return "Stale";
}

export function getRemainingDays(today = new Date()) {
  const year = today.getFullYear();
  const month = today.getMonth();
  const lastDay = new Date(year, month + 1, 0).getDate();
  return Math.max(1, lastDay - today.getDate() + 1);
}

export function createSnapshot(profile: ProfileInput): Snapshot {
  const remainingDays = getRemainingDays();
  const spendableBalance = Math.max(
    0,
    profile.currentBalance - profile.protectedBuffer,
  );
  const safeDaily = spendableBalance / remainingDays;
  const confidence = getConfidence(profile.lastBalanceUpdate);

  let status: Snapshot["status"] = "Safe";
  let headline = "You have room today.";

  if (safeDaily <= 0) {
    status = "Risky";
    headline = "Protected money is under pressure.";
  } else if (safeDaily < 25) {
    status = "Risky";
    headline = "Today is tight. Keep spends small.";
  } else if (safeDaily < 60) {
    status = "Close";
    headline = "You are safe, but close to the edge.";
  }

  if (confidence === "Stale") {
    headline = "Refresh your balance before trusting this fully.";
  }

  return {
    remainingDays,
    spendableBalance,
    safeDaily,
    confidence,
    protectedBuffer: profile.protectedBuffer,
    status,
    headline,
  };
}

function formatPercent(value: number) {
  return Math.round(value);
}

export function checkPurchase(profile: ProfileInput, purchaseAmount: number) {
  const snapshot = createSnapshot(profile);
  const remainingAfterPurchase = snapshot.spendableBalance - purchaseAmount;
  const paceUsage = snapshot.safeDaily > 0 ? purchaseAmount / snapshot.safeDaily : 0;

  let verdict: Verdict = "Safe";
  let consequence = "This keeps you on track.";

  if (snapshot.confidence === "Stale") {
    verdict = "Risky";
    consequence = "Update your balance to refresh this number before spending.";
  } else if (remainingAfterPurchase <= 0) {
    verdict = "Not safe";
    consequence = "This cuts into protected money and puts your buffer at risk.";
  } else if (purchaseAmount > snapshot.safeDaily) {
    verdict = "Risky";
    consequence = `This puts you ${formatPercent(
      paceUsage * 100,
    )}% of today's safe pace and makes the next few days tighter.`;
  } else if (paceUsage > 0.75) {
    consequence = `Safe, but it uses ${formatPercent(
      paceUsage * 100,
    )}% of today's safe amount.`;
  } else {
    consequence = `Safe. You would still have roughly RM${remainingAfterPurchase.toFixed(
      0,
    )} of spendable room left.`;
  }

  return {
    snapshot,
    verdict,
    consequence,
    remainingAfterPurchase,
  };
}

export function shouldEnterRecovery(history: PurchaseCheck[], snapshot: Snapshot) {
  const recent = history.slice(0, 5);
  const highRiskCount = recent.filter(
    (item) => item.verdict === "Risky" || item.verdict === "Not safe",
  ).length;

  return snapshot.safeDaily < 30 || highRiskCount >= 3;
}

export function recoveryMessage(history: PurchaseCheck[], snapshot: Snapshot) {
  const recent = history.slice(0, 5);
  const unsafeCount = recent.filter((item) => item.verdict === "Not safe").length;

  if (unsafeCount >= 2 || snapshot.safeDaily <= 0) {
    return "Hold non-essential spending today and protect your committed money first.";
  }

  if (snapshot.safeDaily < 20) {
    return "Keep today under a very small limit and refresh your balance tonight.";
  }

  return "You are drifting over pace. Keep the next 2 days lean to regain control.";
}

export function currency(value: number) {
  return `RM${Math.max(0, value).toFixed(0)}`;
}
