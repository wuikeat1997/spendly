# iOS Release Readiness

## Current Target

Spendly should first ship as an iOS-playable PWA:

- Open in Safari.
- Add to Home Screen.
- Run in standalone display mode.
- Preserve safe-area spacing on iPhone.
- Keep core checks usable without native APIs.

## App Store Path

When the PWA proves the product loop, wrap the app for App Store release with Capacitor or a native shell.

Before App Store submission, verify:

- Production HTTPS domain is configured.
- Supabase redirect URLs use the production domain.
- Apple app icons and splash assets are final.
- Privacy policy is published.
- App Privacy nutrition labels are prepared.
- Any paid features use Apple-compliant in-app purchase rules.
- Offline and poor-network states are reviewed on real iPhones.

## Device Test Checklist

- iPhone Safari can open `/app`.
- Home-screen install launches without browser chrome.
- OTP sign-in works from Mail back into Safari/home-screen app.
- Numeric fields do not zoom on focus.
- Keyboard does not hide primary actions.
- Safe-to-spend check works on iOS Safari.
- Reset data clears local state.
- Protected buffer value `0` remains `0`.
