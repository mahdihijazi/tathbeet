# Firebase Hosting For Android App Links

This is the missing manual step for Android email-link sign-in.

Without Firebase Hosting serving `/.well-known/assetlinks.json` on the same domain used by Firebase Auth, Android may keep opening the email link in the browser instead of handing it to the app.

## What is already prepared in the repo

- `firebase.json`
- `.firebaserc`
- `hosting-public/.well-known/assetlinks.json`
- `hosting-public/index.html`

The current setup targets:

- project: `tathbeet-b40d5`
- package: `com.quran.tathbeet`
- debug SHA-256:
  `1E:CF:AB:A2:A2:F3:76:15:DA:5E:9D:B6:CE:BC:79:E3:7A:78:9D:9E:CF:13:FC:CF:2A:11:78:D7:4C:2B:1D:CF`

The Hosting bundle now also includes a browser fallback page at:

- `https://tathbeet-b40d5.firebaseapp.com/finishSignIn/`

If Android App Links still do not fire, that page redirects into the app through a custom `tathbeet://` link.

## Manual steps

1. Install Firebase CLI if it is not installed:

```bash
npm install -g firebase-tools
```

2. Log in:

```bash
firebase login
```

3. From the repo root, deploy Hosting:

```bash
firebase deploy --only hosting
```

4. After deploy finishes, verify these URLs in a browser:

- `https://tathbeet-b40d5.firebaseapp.com/`
- `https://tathbeet-b40d5.firebaseapp.com/.well-known/assetlinks.json`
- `https://tathbeet-b40d5.firebaseapp.com/finishSignIn/`

The `assetlinks.json` URL must load successfully and include `com.quran.tathbeet`.

5. On the Android device:

- uninstall and reinstall the debug app
- open `Settings > Apps > Tathbeet > Open by default`
- enable the supported web address if Android does not do it automatically

6. Request a fresh sign-in email and open it on the same Android device.

## Later for release builds

Before shipping a release build, add the release SHA-256 fingerprint both:

- in Firebase Android app settings
- in `hosting-public/.well-known/assetlinks.json`

You can keep multiple fingerprints in the same file.
