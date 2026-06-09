# Next Phase Product Notes

## Core Human Value

Spendly is useful if it stays focused on one human moment:

> "Can I safely make this purchase right now?"

The app should help people feel less blind before spending. Its value is not detailed budgeting, charts, or transaction analysis. Its value is pre-spend confidence.

## Product Direction

Keep the product positioned as a safe-to-spend control system, not a budgeting tracker.

The strongest loop is:

1. User opens the app before spending.
2. User sees a rough safe-to-spend amount.
3. User enters a purchase amount.
4. App returns a clear verdict.
5. If risky, app gives one small next action.

## What To Preserve

- Fast mobile-first use.
- Approximate but honest guidance.
- Non-judgmental tone.
- Minimal setup.
- No transaction-by-transaction logging requirement.
- No category-heavy budget dashboard.

## Key Risks To Improve

### Trust

The app must not sound more precise than it is. If balance data is stale or user-entered, the product should say so clearly.

Potential wording:

- "Looks safe"
- "Likely risky"
- "Update your balance to refresh this"

Avoid overly absolute wording when the data is approximate.

### Manual Balance Entry

Manual balance entry keeps v1 simple, but user trust depends on discipline. The next phase should make refreshes feel quick and low-friction.

Ideas:

- One-tap balance refresh prompt.
- Gentle stale-balance reminder.
- Make "rough estimate is okay" visible near balance update.

### Auth Friction

OTP login must feel smooth on iOS Safari and the Capacitor shell. Any auth friction can damage first impression.

Next phase should verify:

- OTP email copy is clear.
- OTP length matches app input.
- Resend behavior feels predictable.
- Friendly errors stay non-technical.

### Recovery Mode

Recovery should feel like help, not punishment.

Good recovery copy:

- "Keep today under RM30."
- "Protect your buffer first."
- "Make the next spend smaller."

Avoid shame, guilt, or moralizing.

## Next Phase Candidate Improvements

- Rename verdicts from absolute language to softer confidence language.
- Improve stale-balance handling before showing a verdict.
- Add clearer iOS home-screen install testing.
- Add friendly empty/error/loading states for auth and backend sync.
- Add a small "why this verdict?" explanation that is short enough to read before payment.
- Validate the product with real users by asking whether it changed a spending decision.

## Product Test Question

The next phase should answer:

> Did Spendly help a user pause and make a better spending decision before payment?

If yes, the product is useful.

If no, more charts or features will not fix it. The pre-spend decision loop needs to be clearer, faster, or more trusted.
