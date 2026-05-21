# Decision Rules

## Purpose

Document the initial behavioral and calculation rules for the Budget Control System MVP. These rules are meant to be simple, explainable, and trustworthy enough for an approximate first version.

## Inputs

Required onboarding inputs:
- `monthly_income`
- `monthly_commitments`
- `current_balance`

Optional support input:
- `protected_buffer`

If `protected_buffer` is not explicitly set, the system may default to `0` for MVP or use a simple product-level default chosen during implementation.

## Core Definitions

- `monthly_income`: the money the user typically has available per cycle
- `monthly_commitments`: the total amount already spoken for each cycle
- `current_balance`: rough amount currently available to spend from
- `protected_buffer`: amount that should not be touched except in emergency
- `remaining_days`: number of days until next income event
- `disposable_monthly`: income minus commitments
- `spendable_balance`: current balance minus protected buffer
- `safe_daily`: spendable balance divided by remaining days

## Base Formula

```text
disposable_monthly = monthly_income - monthly_commitments
spendable_balance = current_balance - protected_buffer
safe_daily = spendable_balance / remaining_days
```

## Minimum Rule Constraints

- Never show a negative safe-to-spend value as if it were normal
- If `remaining_days <= 0`, force a cycle reset flow
- If `spendable_balance <= 0`, return `Not safe`
- Clamp displayed safe-to-spend to `0` at minimum for user-facing output
- Round currency display cleanly for readability

## Confidence Rules

The system must not pretend high precision when balance data is stale.

### Confidence states

- `Fresh`
  - Balance updated recently enough for normal confidence
- `Aging`
  - Balance may still be useful, but user should be nudged to refresh soon
- `Stale`
  - System should request a quick balance update before acting too confidently

### Suggested MVP triggers

- `Fresh`: balance updated today
- `Aging`: balance updated 1-3 days ago
- `Stale`: balance updated more than 3 days ago

These thresholds can be tuned later, but the first version should favor trust over coverage.

## Purchase Check Rules

Given `purchase_amount`:

```text
remaining_after_purchase = spendable_balance - purchase_amount
safe_after_purchase = remaining_after_purchase / remaining_days
```

### Output bands

- `Safe`
  - purchase is within today's safe range
- `Risky`
  - purchase exceeds today's safe range but does not immediately break protected money
- `Not safe`
  - purchase would push user into protected money, negative spendable balance, or a materially unsafe state

## Suggested Verdict Logic

### Safe

Return `Safe` when:
- `purchase_amount <= safe_daily`
- confidence is not `Stale`

Example output:
- "You can make this purchase and stay on track."

### Risky

Return `Risky` when:
- `purchase_amount > safe_daily`
- and `remaining_after_purchase > 0`

Example output:
- "This is likely to push you off plan."
- "This puts you above your safe daily amount."

### Not safe

Return `Not safe` when:
- `remaining_after_purchase <= 0`
- or protected money would be consumed
- or the system detects a hard obligation risk

Example output:
- "This puts your protected money at risk."
- "This is not safe right now."

## Consequence Framing Rules

The user should see consequence, not just amount.

Preferred consequence styles:
- buffer impact
- weekly limit impact
- savings tradeoff
- obligation risk

Examples:
- "This takes RM50 away from your protected amount."
- "This puts you 25% over your weekly pace."
- "This makes your next few days tighter."

Avoid:
- guilt
- shame
- vague generic warnings

## Balance Refresh Rules

Ask for a balance refresh when:
- confidence is `Stale`
- the user repeatedly checks purchases without updating balance
- a purchase result sits near the boundary between `Safe` and `Risky`
- recent user behavior implies the stored balance may no longer be reliable

Preferred prompt:
- "Update your balance to refresh this number."

## Recovery Mode Rules

Recovery mode exists to reduce damage and restore control quickly.

### Enter recovery mode when

- user records multiple risky or not safe checks in a short period
- updated balance shows burn rate is materially faster than plan
- protected money is trending toward risk

### Recovery mode outputs

- temporary lower daily limit
- short corrective plan for the next 1-3 days
- one clear next step

Examples:
- "Keep today under RM30."
- "Avoid non-essential spending for the next 2 days."
- "Protect your upcoming bill before discretionary spending."

### Exit recovery mode when

- balance stabilizes
- user follows the temporary limit for a short recovery window
- protected money is no longer at immediate risk

## Product Behavior Rules

- Always prefer approximate honesty over false precision
- Always explain why a verdict is risky when possible
- Always keep the result fast enough for pre-spend use
- Never require category selection for the core purchase check
- Never block the user with analysis before giving the verdict

## MVP Event Model

Useful event types for implementation:
- onboarding_completed
- balance_updated
- purchase_checked
- recovery_mode_entered
- recovery_mode_exited

This is enough for v1 analytics and future behavior tuning without overengineering.

## Explicit Non-Rules For MVP

Do not include these in the first rule engine:
- merchant-level behavioral profiling
- emotion detection
- investment allocation rules
- household shared budgeting logic
- automatic banking ingestion dependencies

These can be layered later after the safe-to-spend loop proves useful.
