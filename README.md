# Spendly

Spendly is now organized as a small monorepo:

```text
spendly-frontend/  Next.js and Capacitor iOS app
spendly-backend/   New backend service workspace
```

The current frontend still contains the legacy Supabase integration while the
new backend is being built. Sunset Supabase after the replacement API and auth
flows are ready.

## Frontend

```bash
cd spendly-frontend
npm run dev
```

For TestFlight/App Store sync:

```bash
cd spendly-frontend
CAPACITOR_SERVER_URL=https://spendly-tawny-two.vercel.app npm run ios:sync
```

## Backend

Backend work starts in `spendly-backend/`.
