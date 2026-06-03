# Native iOS Build

Spendly now has a Capacitor iOS shell under `ios/`. The first native build path
loads the deployed Next.js app in a native iOS WebView, because the current
Next.js app uses server routes for Supabase OTP auth.

## Local Simulator Build

1. Start the web app:

```bash
npm run dev
```

2. Sync the iOS project against local dev:

```bash
CAPACITOR_SERVER_URL=http://localhost:3000 npm run ios:sync
```

3. Open Xcode:

```bash
npm run ios:open
```

4. Select an iPhone simulator and run the `App` scheme.

## TestFlight or App Store Build

Use the deployed HTTPS URL, not localhost:

```bash
CAPACITOR_SERVER_URL=https://your-production-domain.example npm run ios:sync
npm run ios:open
```

Then in Xcode:

1. Select the `App` target.
2. Set `Bundle Identifier` to the Apple Developer bundle ID.
3. Set `Version` and `Build`.
4. Select your signing team.
5. Replace generated app icons and splash assets with final Spendly assets.
6. Build on a real iPhone.
7. Use `Product > Archive`.
8. Upload the archive to App Store Connect.

## Required Production Checks

- The production Spendly site is served over HTTPS.
- `CAPACITOR_SERVER_URL` is the exact production origin before archive.
- Supabase redirect URLs include `https://your-production-domain.example/auth/callback`.
- Passwordless sign-in works from Mail back into the iOS app flow.
- App Store Connect has a privacy policy URL.
- App Privacy labels account for email auth, financial profile data, and purchase check history.
- The app includes account/data deletion if cloud sign-in is enabled.
- Any paid unlocks use Apple-compliant in-app purchase rules.
- The app description clearly frames Spendly as budgeting guidance, not financial advice.

## Current Constraint

The iOS project is a native shell, not a fully offline native rewrite. The app
still depends on the deployed Next.js server for API routes and auth callbacks.
If a fully bundled offline binary becomes a requirement, the auth flow and API
routes need to move out of Next.js server routes or be replaced with native/API
client flows.

## Troubleshooting

If `xcodebuild` fails with `iOS Platform Not Installed` or `Found no
destinations`, open Xcode and install the iOS platform from
`Xcode > Settings > Components`, then rerun:

```bash
xcodebuild -project ios/App/App.xcodeproj -target App -configuration Debug -sdk iphonesimulator CODE_SIGNING_ALLOWED=NO build
```
