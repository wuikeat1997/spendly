# Spendly Web Deployment

This guide deploys Spendly as:

- Frontend: Vercel, rooted at `spendly-frontend`
- Backend: Railway, rooted at `spendly-backend`
- Database: Railway PostgreSQL
- Email: Resend API over HTTPS

App Store and Play Store release can wait until later.

## 1. Deploy Backend To Railway

Create a Railway project and add two services:

1. PostgreSQL database
2. Backend service from this GitHub repo

For the backend service:

```text
Root directory: spendly-backend
Config file: /spendly-backend/railway.toml
Builder: Dockerfile
Health check: /actuator/health
```

The backend has a Dockerfile and Railway config:

```text
spendly-backend/Dockerfile
spendly-backend/railway.toml
```

Set Railway backend variables:

```text
SPRING_PROFILES_ACTIVE=prod

SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}
SPRING_DATASOURCE_PASSWORD=${{Postgres.POSTGRES_PASSWORD}}

APP_CORS_ALLOWED_ORIGINS=https://your-vercel-domain.vercel.app
APP_FRONTEND_URL=https://your-vercel-domain.vercel.app
APP_MAIL_FROM=no-reply@spendly.com
APP_MAIL_PROVIDER=resend
RESEND_API_KEY=<your-resend-api-key>

JWT_SECRET=<long-random-secret-at-least-32-bytes>
```

Generate a strong JWT secret locally:

```bash
openssl rand -base64 64
```

After deploy, generate a public Railway domain and verify:

```text
https://your-railway-backend.up.railway.app/actuator/health
```

Expected:

```json
{"status":"UP"}
```

## 2. Deploy Frontend To Vercel

Import the same GitHub repo into Vercel.

Vercel project settings:

```text
Root Directory: spendly-frontend
Framework Preset: Next.js
Install Command: npm install
Build Command: npm run build
Output Directory: .next
```

Set Vercel environment variable:

```text
NEXT_PUBLIC_API_BASE_URL=https://your-railway-backend.up.railway.app
```

Deploy and verify:

```text
https://your-vercel-domain.vercel.app/app
```

## 3. Update Backend CORS

Once Vercel gives the final production URL, update Railway:

```text
APP_CORS_ALLOWED_ORIGINS=https://your-vercel-domain.vercel.app
APP_FRONTEND_URL=https://your-vercel-domain.vercel.app
```

Redeploy/restart backend.

## 4. Smoke Test

Test the production flow:

1. Open `/app` on Vercel.
2. Request OTP.
3. Confirm the email arrives through Resend.
4. Verify OTP.
5. Add profile data.
6. Save a purchase check.
7. Refresh the page and confirm profile/history load.
8. Sign out and sign in again.
9. Clear data and confirm rows are removed.

## 5. Native Shell Later

After web deploy is stable, sync Capacitor using the Vercel frontend URL:

```bash
cd spendly-frontend
CAPACITOR_SERVER_URL=https://your-vercel-domain.vercel.app npm run ios:sync
CAPACITOR_SERVER_URL=https://your-vercel-domain.vercel.app npm run android:sync
```

Store submission can wait until Apple Developer and Google Play accounts are
ready.
