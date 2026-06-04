# Spendly

Spendly is a mobile-first safe-to-spend app. It helps a user answer one urgent question before paying:

> Can I safely make this purchase right now?

The current product is a Next.js PWA prototype with optional Spendly backend auth and persistence. Without a backend API URL, it still runs in local-only mode using `localStorage`.

## What It Does

- Captures a simple financial profile: monthly income, monthly commitments, current balance, and protected buffer.
- Calculates spendable balance and a daily safe-to-spend pace for the rest of the month.
- Checks a purchase amount and returns `Safe`, `Risky`, or `Not safe`.
- Warns when the balance estimate is stale.
- Records recent purchase checks.
- Updates the current balance when a checked spend is recorded.
- Supports passwordless email OTP sign-in with the Spendly backend when configured.
- Runs as an installable iOS-friendly PWA.

## Tech Stack

- Next.js 16 App Router
- React 19
- TypeScript
- Tailwind CSS 4
- Spendly Java backend API, optional at runtime
- PWA manifest and iOS web app metadata
- Capacitor iOS shell for TestFlight/App Store binary builds

## Project Structure

```text
src/app/
  page.tsx                    Marketing/product overview
  app/page.tsx                Main Spendly app shell
  layout.tsx                  App metadata, viewport, manifest hookup
  manifest.ts                 PWA manifest
  globals.css                 Global theme and Tailwind tokens

src/components/
  mvp-console.tsx             Main client-side product experience

src/lib/
  money.ts                    Pure safe-to-spend rules
  persistence.ts              localStorage/backend API persistence adapter

docs/
  architecture.md             Technical architecture notes
  ios-release-readiness.md    PWA and future App Store checklist
  native-ios-build.md         Capacitor iOS build and archive notes
  next-phase-product-notes.md Product direction notes
```

## Getting Started

Install dependencies:

```bash
npm install
```

Run the development server:

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

Useful routes:

- `/` - overview and product positioning
- `/app` - the working safe-to-spend console

## Environment Variables

Spendly can run without environment variables. In that mode, data is stored in the browser only.

To enable backend auth and cloud persistence, create `.env.local`:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

For production, point `NEXT_PUBLIC_API_BASE_URL` to the Railway backend URL.

## Scripts

```bash
npm run dev      # Start the local Next.js dev server
npm run build    # Create a production build
npm run start    # Start the production server
npm run lint     # Run ESLint
npm run ios:sync # Sync the Capacitor iOS project
npm run ios:open # Open the iOS project in Xcode
```

For TestFlight/App Store builds, sync Capacitor with the production HTTPS URL:

```bash
CAPACITOR_SERVER_URL=https://your-production-domain.example npm run ios:sync
```

See [docs/native-ios-build.md](./docs/native-ios-build.md) for the native iOS
binary flow.

## Architecture

Read [docs/architecture.md](./docs/architecture.md) for the system overview, data flow, persistence model, and deployment notes.

At a high level:

1. `src/app/app/page.tsx` renders the app shell.
2. `MvpConsole` owns the interactive UI state.
3. `src/lib/money.ts` calculates snapshots, verdicts, and recovery messages.
4. `src/lib/persistence.ts` chooses either the Spendly backend API or localStorage.
5. The Java backend handles OTP sign-in, profile persistence, and purchase-check history.

## Product Notes

Spendly should stay focused on pre-spend confidence, not full budgeting. The app intentionally favors:

- approximate but honest guidance,
- fast mobile input,
- clear consequences,
- non-judgmental recovery language,
- minimal setup.

See [docs/next-phase-product-notes.md](./docs/next-phase-product-notes.md) for the product direction and [docs/ios-release-readiness.md](./docs/ios-release-readiness.md) for iOS release checks.
