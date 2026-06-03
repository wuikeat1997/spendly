# iOS Release Readiness

## Current Target

Spendly should first ship as an iOS-playable PWA:

- Open in Safari.
- Add to Home Screen.
- Run in standalone display mode.
- Preserve safe-area spacing on iPhone.
- Keep core checks usable without native APIs.

## App Store Path

The project now includes a Capacitor iOS shell under `ios/`. The current binary
path loads the deployed Next.js app from a production HTTPS URL, because the app
uses server routes for OTP auth and callbacks.

Before creating a TestFlight or App Store archive:

```bash
CAPACITOR_SERVER_URL=https://your-production-domain.example npm run ios:sync
npm run ios:open
```

Before App Store submission, verify:

- Production HTTPS domain is configured.
- `ios/App/App/capacitor.config.json` points at the production URL, not localhost.
- Supabase redirect URLs use the production domain.
- Apple app icons and splash assets are final.
- Privacy policy is published.
- App Privacy nutrition labels are prepared.
- Account deletion/data deletion is available if cloud accounts are enabled.
- Any paid features use Apple-compliant in-app purchase rules.
- Offline and poor-network states are reviewed on real iPhones.

See [native-ios-build.md](./native-ios-build.md) for the binary build steps.

## Device Test Checklist

- iPhone Safari can open `/app`.
- Home-screen install launches without browser chrome.
- OTP sign-in works from Mail back into Safari/home-screen app.
- Numeric fields do not zoom on focus.
- Keyboard does not hide primary actions.
- Safe-to-spend check works on iOS Safari.
- Reset data clears local state.
- Protected buffer value `0` remains `0`.
