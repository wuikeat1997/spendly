# Spendly

Spendly is a mobile-first safe-to-spend app. It helps a user answer one urgent question before paying:

> Can I safely make this purchase right now?

The current product is a Next.js PWA prototype with optional Supabase auth and persistence. Without Supabase environment variables, it still runs in local-only mode using `localStorage`.

## What It Does

- Captures a simple financial profile: monthly income, monthly commitments, current balance, and protected buffer.
- Calculates spendable balance and a daily safe-to-spend pace for the rest of the month.
- Checks a purchase amount and returns `Safe`, `Risky`, or `Not safe`.
- Warns when the balance estimate is stale.
- Records recent purchase checks.
- Updates the current balance when a checked spend is recorded.
- Supports passwordless email sign-in with Supabase when configured.
- Runs as an installable iOS-friendly PWA.

## Tech Stack

- Next.js 16 App Router
- React 19
- TypeScript
- Tailwind CSS 4
- Supabase Auth and Postgres, optional at runtime
- PWA manifest and iOS web app metadata

## Project Structure

```text
src/app/
  page.tsx                    Marketing/product overview
  app/page.tsx                Main Spendly app shell
  api/auth/send-otp/route.ts  Server route for passwordless email OTP
  auth/callback/route.ts      Supabase magic-link callback
  layout.tsx                  App metadata, viewport, manifest hookup
  manifest.ts                 PWA manifest
  globals.css                 Global theme and Tailwind tokens

src/components/
  mvp-console.tsx             Main client-side product experience

src/lib/
  money.ts                    Pure safe-to-spend rules
  persistence.ts              localStorage/Supabase persistence adapter
  supabase/server.ts          Server-side Supabase client factory

supabase/
  migrations/                  Versioned database changes
  schema.sql                  Tables, foreign keys, and RLS policies

docs/
  architecture.md             Technical architecture notes
  ios-release-readiness.md    PWA and future App Store checklist
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

To enable Supabase auth and cloud persistence, create `.env.local`:

```bash
NEXT_PUBLIC_SUPABASE_URL=your-project-url
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-or-publishable-key
```

`NEXT_PUBLIC_SUPABASE_PUBLISHABLE_DEFAULT_KEY` is also supported as a fallback key name.

## Supabase Setup

1. Create a Supabase project.
2. Run the SQL files in [supabase/migrations](./supabase/migrations) in filename order.
3. Enable email OTP/magic-link auth in Supabase.
4. Add local and production redirect URLs:

```text
http://localhost:3000/auth/callback
https://your-domain.example/auth/callback
```

The schema creates:

- `profiles` for each user's current financial profile.
- `purchase_checks` for recent safe-to-spend checks.
- Row level security policies so users can only manage their own rows.

[supabase/schema.sql](./supabase/schema.sql) is kept as a readable snapshot of the current schema. Use the files in `supabase/migrations/` as the versioned source of truth for applying database changes.

## Scripts

```bash
npm run dev      # Start the local Next.js dev server
npm run build    # Create a production build
npm run start    # Start the production server
npm run lint     # Run ESLint
```

## Architecture

Read [docs/architecture.md](./docs/architecture.md) for the system overview, data flow, persistence model, and deployment notes.

At a high level:

1. `src/app/app/page.tsx` renders the app shell.
2. `MvpConsole` owns the interactive UI state.
3. `src/lib/money.ts` calculates snapshots, verdicts, and recovery messages.
4. `src/lib/persistence.ts` chooses either Supabase or localStorage.
5. Supabase server routes handle passwordless sign-in and callback exchange.

## Product Notes

Spendly should stay focused on pre-spend confidence, not full budgeting. The app intentionally favors:

- approximate but honest guidance,
- fast mobile input,
- clear consequences,
- non-judgmental recovery language,
- minimal setup.

See [docs/next-phase-product-notes.md](./docs/next-phase-product-notes.md) for the product direction and [docs/ios-release-readiness.md](./docs/ios-release-readiness.md) for iOS release checks.
