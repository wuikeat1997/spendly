---
title: 'Budget Control System MVP'
type: 'feature'
created: '2026-03-27'
status: 'ready-for-dev'
context:
  - '_bmad-output/brainstorming/brainstorming-session-2026-03-26-161142.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Existing budgeting tools mostly help after money is already spent. The target user needs a fast, trustworthy answer in the 2-5 seconds before payment: "Am I safe to spend this or not?"

**Approach:** Build a lightweight pre-spend decision system centered on approximate safe-to-spend guidance, low-friction balance updates, and clear guardrail messaging. The MVP should optimize for control, certainty, and trust rather than detailed tracking, charts, or financial complexity.

## Boundaries & Constraints

**Always:** Keep the MVP focused on one core loop: set protected money, check before spending, get a safe/risky/not safe answer. Use only three onboarding inputs for v1: monthly income, monthly commitments, and rough current available balance. Be explicit that outputs are approximate, fast, and decision-oriented. Keep the UX sub-second and non-judgmental.

**Ask First:** Choosing the implementation platform and stack; adding bank sync; adding shared household support; adding automated savings or investment actions; changing the MVP from approximate guidance to exact transaction-grade calculations.

**Never:** Do not turn v1 into a generic budgeting dashboard. Do not require manual transaction-by-transaction logging. Do not depend on expense categorization, heavy AI insights, social features, or investment management to make the core loop work.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| First setup | User provides income, commitments, and current balance | App calculates initial safe daily spend and explains that guidance is approximate | If any required value is missing or invalid, ask for correction with plain examples |
| Pre-spend happy path | User enters or selects a purchase amount while current state is within plan | App returns a fast safe verdict, updated remaining safe spend, and concise consequence framing | N/A |
| Risky spend | Purchase would materially exceed safe daily spend or threaten protected money | App returns risky or not safe verdict and explains the consequence in plain language | If calculation inputs are stale, prompt for quick balance refresh before final verdict |
| Stale state | User has not updated balance recently or behavior suggests drift from expected burn | App downgrades confidence, asks for a 2-second balance update, then recalculates | Never pretend precision when confidence is low |
| Recovery mode | User has overspent for multiple days or is trending toward a missed obligation | App switches to recovery framing with a short corrective plan for the next few days | If the system cannot produce a confident plan, it should still warn and recommend a manual reset |

</frozen-after-approval>

## Code Map

- `_bmad-output/brainstorming/brainstorming-session-2026-03-26-161142.md` -- source discovery and product intent
- `_bmad-output/planning-artifacts/budget-control-system-mvp-spec.md` -- approved product/MVP quick spec
- `_bmad-output/implementation-artifacts/` -- target folder for follow-on implementation specs once stack is chosen

## Tasks & Acceptance

**Execution:**
- [ ] `_bmad-output/planning-artifacts/budget-control-system-mvp-spec.md` -- finalize the MVP definition, product boundaries, and core decision loop -- creates the approved source of truth before architecture or coding starts
- [ ] `_bmad-output/planning-artifacts/mvp-screen-flow.md` -- define the minimum screen flow for onboarding, home safe-to-spend view, pre-spend check, balance update, and recovery mode -- prevents scope drift during design and implementation
- [ ] `_bmad-output/planning-artifacts/decision-rules.md` -- document the initial safe-to-spend formula, confidence rules, stale-data handling, and recovery triggers -- gives engineering a stable behavioral ruleset without overcommitting to infrastructure

**Acceptance Criteria:**
- Given a new user with monthly income, monthly commitments, and current balance, when onboarding is completed, then the system can immediately show an approximate daily safe-to-spend figure.
- Given a user checks a purchase before spending, when the amount is within safe limits, then the system returns a fast safe verdict with updated remaining spend guidance.
- Given a purchase threatens protected money or pushes the user materially off plan, when the check is performed, then the system returns a risky or not safe verdict with a concrete consequence.
- Given confidence in the current balance is low, when the user requests a spend check, then the system asks for a quick balance refresh instead of presenting false precision.
- Given a user is drifting off plan for several days, when the system detects that pattern, then it enters recovery mode with a short, low-cognitive-load corrective plan.

## Design Notes

The product should be positioned as a personal financial control system, not a budgeting tracker. The emotional job is certainty under pressure.

Golden product behaviors:
- "You can roughly spend RM45 today."
- "This is risky. It puts your weekly limit 25% over plan."
- "This takes RM50 away from your savings goal."
- "Update your balance to refresh your safe-to-spend number."

The trust model for v1 is:
- approximate but honest
- fast enough to use before payment
- specific enough to affect behavior
- non-judgmental in tone

## Verification

**Manual checks (if no CLI):**
- Review the spec and confirm the MVP remains centered on pre-spend control rather than general budgeting
- Verify all banned non-MVP features stay out of the frozen scope
- Verify the onboarding inputs remain limited to income, commitments, and current balance
