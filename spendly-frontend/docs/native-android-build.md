# Native Android Build

Spendly now has a Capacitor Android shell under `android/`. Like the iOS shell,
it loads the Next.js frontend in a native WebView and calls the Spendly Java
backend directly for OTP auth and persistence.

## Local Emulator Build

Prerequisite: install Android Studio and the Android SDK. Gradle needs either
`ANDROID_HOME` or `android/local.properties` with an SDK path.

1. Start the backend:

```bash
cd ../spendly-backend
docker compose up -d
```

2. Start the frontend:

```bash
cd ../spendly-frontend
npm run dev
```

3. Sync the Android project against local dev:

```bash
CAPACITOR_SERVER_URL=http://localhost:3000 npm run android:sync
```

4. Open Android Studio:

```bash
npm run android:open
```

5. Select an Android emulator and run the `app` configuration.

If Gradle reports `SDK location not found`, create `android/local.properties`
after installing Android Studio:

```properties
sdk.dir=/Users/your-user/Library/Android/sdk
```

## Real Android Device

For a physical Android phone on the same Wi-Fi network, use your Mac LAN IP
instead of `localhost`.

Example:

```bash
NEXT_PUBLIC_API_BASE_URL=http://192.168.1.50:8080
CAPACITOR_SERVER_URL=http://192.168.1.50:3000 npm run android:sync
```

Also add that frontend origin to backend CORS while testing locally:

```text
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://192.168.1.50:3000
```

Local OTP emails are captured in Mailpit:

```text
http://localhost:8025
```

## Play Store Build

Use the deployed HTTPS URL, not localhost:

```bash
CAPACITOR_SERVER_URL=https://your-production-domain.example npm run android:sync
npm run android:open
```

Then in Android Studio:

1. Set the final `applicationId`.
2. Set version name and version code.
3. Replace generated icons and splash assets with final Spendly assets.
4. Build a release app bundle.
5. Upload the `.aab` to Google Play Console.

## Required Production Checks

- The production Spendly frontend is served over HTTPS.
- `CAPACITOR_SERVER_URL` is the exact production frontend origin.
- `NEXT_PUBLIC_API_BASE_URL` points at the Railway backend URL.
- Backend CORS allows the production frontend origin.
- Real SMTP is configured on the backend.
- OTP sign-in works in the Android WebView.
- Privacy policy and data deletion/account deletion paths are ready.
