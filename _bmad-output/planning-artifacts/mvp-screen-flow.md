# MVP Screen Flow

## Purpose

Define the minimum user-facing flow for the approved Budget Control System MVP. This document exists to keep implementation focused on the pre-spend control loop and prevent drift into generic budgeting UI.

## Product Principle

The app should answer one question quickly:

"Am I safe to spend this right now?"

Every screen should either help the user:
- establish trusted inputs
- check a spending decision
- refresh confidence in the answer
- recover when they drift off plan

## Screen 1: Welcome / Value Framing

**Goal:** Explain the product in one screen and set correct expectations.

**Primary message:**
- Know what is safe to spend before you pay
- Guidance is approximate but designed to keep you in control

**Elements:**
- Short headline
- One-sentence explanation
- "Get Started" CTA
- Optional note: no bank sync required for MVP

**User action:**
- Start onboarding

## Screen 2: Onboarding Input Flow

**Goal:** Collect the three required inputs with minimal friction.

**Fields:**
- Monthly income or usable monthly money
- Monthly committed amount
- Current available balance

**Design rules:**
- One concept per step or a very short stacked form
- Plain-language examples under each field
- Avoid financial jargon such as gross/net, discretionary ratio, category setup
- Show progress and keep the flow under one minute

**Output:**
- Initial safe-to-spend estimate
- Remaining days until next income cycle

## Screen 3: Home / Safe-To-Spend Dashboard

**Goal:** Provide the fastest possible decision-ready view.

**Primary content above the fold:**
- Safe to spend today: `RM X`
- Status label: `Safe`, `Close`, or `Risky`
- Confidence state: `Fresh` or `Needs update`

**Secondary content:**
- Days left in current cycle
- Protected amount reminder
- Short warning or reassurance line
- CTA: `Check a purchase`
- CTA: `Update balance`

**What this screen must not become:**
- A chart dashboard
- Category breakdown center
- Transaction history feed

## Screen 4: Pre-Spend Check

**Goal:** Let the user test a purchase in under a few seconds.

**Input:**
- Purchase amount

**Output states:**
- `Safe`
- `Risky`
- `Not safe`

**Decision card contents:**
- Purchase amount
- Safe-to-spend remaining after purchase
- Plain-language consequence
- Optional quick rationale:
  - "This keeps you on track"
  - "This pushes you 25% over your weekly limit"
  - "This reduces your savings buffer"

**Actions:**
- `Check another amount`
- `Back to home`
- `Update balance` if confidence is low

## Screen 5: Balance Update

**Goal:** Refresh the system without making the user manually log transactions.

**Primary prompt:**
- "Roughly how much do you have available now?"

**Design rules:**
- Single-field fast entry
- Accept rough values
- Reinforce that an estimate is okay

**After submit:**
- Recalculate safe-to-spend
- Show whether confidence improved
- Return user to home or pre-spend check

## Screen 6: Recovery Mode

**Goal:** Help the user regain control after drift with minimal cognitive load.

**Trigger examples:**
- Multiple risky checks
- Balance updates showing faster burn than expected
- Protected money at risk

**Primary content:**
- Clear warning that user is off plan
- Short 1-3 day corrective plan
- Simple next action

**Examples:**
- "Limit today to RM30"
- "Avoid non-essential spending for the next 2 days"
- "Protect your bill due soon before discretionary spending"

**Tone:**
- Calm
- Direct
- Non-judgmental

## Primary User Flow

1. User opens app for the first time
2. User enters three onboarding inputs
3. App shows initial safe-to-spend figure
4. User returns to the home screen before a purchase
5. User taps `Check a purchase`
6. User enters amount
7. App returns safe/risky/not safe verdict
8. User optionally updates balance if confidence is stale
9. App recalculates and continues the loop

## Secondary Flow: Recovery

1. System detects drift or protected money risk
2. Home screen changes state to recovery
3. User sees a short corrective plan
4. User follows a smaller temporary limit
5. System returns user to normal mode once risk improves

## UX Constraints

- Core answer should be reachable in one tap from home
- No required transaction categorization
- No required manual spend logging
- No more than one primary CTA per screen
- Output language should optimize for certainty, not analysis

## Copy Direction

Preferred:
- "You can roughly spend RM45 today"
- "This looks risky"
- "Update your balance to refresh this number"
- "This takes money away from your protected amount"

Avoid:
- moralizing tone
- dense financial language
- precision claims the system cannot support

## Open Follow-On Decisions

These are intentionally not resolved here:
- mobile-first web app vs native app
- authentication approach
- notification strategy
- bank sync timing
