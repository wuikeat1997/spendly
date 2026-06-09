# Spendly Local And Production Env

Secrets should be set in your local shell, Docker Compose, Railway, or Vercel.
Do not commit real secret values.

## Local

Local frontend:

```text
spendly-frontend/.env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

Local backend uses Docker Compose:

```text
SPRING_PROFILES_ACTIVE=local
APP_MAIL_PROVIDER=smtp
SPRING_MAIL_HOST=mailpit
SPRING_MAIL_PORT=1025
```

Local OTP emails go to Mailpit:

```text
http://localhost:8025
```

## Production Frontend: Vercel

Set in Vercel project settings:

```text
NEXT_PUBLIC_API_BASE_URL=https://your-railway-backend.up.railway.app
```

## Production Backend: Railway

Set in Railway backend service variables:

```text
SPRING_PROFILES_ACTIVE=prod

PGHOST=${{Postgres.PGHOST}}
PGPORT=${{Postgres.PGPORT}}
PGDATABASE=${{Postgres.PGDATABASE}}
PGUSER=${{Postgres.PGUSER}}
PGPASSWORD=${{Postgres.PGPASSWORD}}

APP_CORS_ALLOWED_ORIGINS=https://your-vercel-domain.vercel.app
APP_FRONTEND_URL=https://your-vercel-domain.vercel.app

APP_MAIL_PROVIDER=resend
APP_MAIL_FROM=no-reply@spendly.com
RESEND_API_KEY=<your-resend-api-key>

JWT_SECRET=<long-random-secret-at-least-32-bytes>
```

Generate JWT secret:

```bash
openssl rand -base64 64
```

## Production Email

Railway Free, Trial, and Hobby do not support outbound SMTP. Use Resend over
HTTPS API for production OTP email.

Before production:

1. Verify `spendly.com` in Resend.
2. Configure DNS records from Resend.
3. Use `no-reply@spendly.com` as `APP_MAIL_FROM`.
4. Set `RESEND_API_KEY` only in Railway variables.
