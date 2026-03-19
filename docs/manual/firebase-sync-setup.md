# Firebase Sync Setup

This document covers the manual Firebase steps that cannot be completed from the repository alone.

The implementation is written so the app can compile and the sync feature can be developed with placeholders first. Tomorrow, replace the placeholder values and follow the steps below.

## 1. Create The Firebase Project

1. Open the Firebase console.
2. Create a new Firebase project for Tathbeet.
3. Enable billing and switch the project to `Blaze`.

Reason:

- Spark is too restrictive for email-link sign-in because it only allows a very small number of sign-in emails per day.

## 2. Register The Android App

1. Add an Android app to the Firebase project.
2. Use the Android application id:

```text
com.quran.tathbeet
```

3. Download `google-services.json`.
4. Keep the file ready, but do not commit it to git.

Even if the app uses manual Firebase initialization, keep `google-services.json` for reference and future plugin-based setup if needed.

## 3. Enable Firebase Authentication

1. Open `Authentication`.
2. Enable `Email link (passwordless sign-in)`.
3. Do not enable Google sign-in for this MVP unless product direction changes later.

## 4. Configure The Auth Link Domain

1. Open `Authentication` settings.
2. Confirm the Firebase Hosting auth domain that will be used for email links.
3. Decide the HTTPS domain that the app should accept for the sign-in return link.

Placeholders currently expected by the app:

```text
TODO_FIREBASE_AUTH_DOMAIN
TODO_FIREBASE_AUTH_HOST
TODO_FIREBASE_ANDROID_PACKAGE_NAME
```

Recommended values:

- auth domain: the Firebase Hosting domain for the project
- auth scheme: `https`
- Android package name: `com.quran.tathbeet`

## 5. Enable Cloud Firestore

1. Open `Firestore Database`.
2. Create the database in native mode.
3. Choose the region you want for the app.

Recommendation:

- pick one region close to the expected users and keep it stable

## 6. Add Android App Links / Deep Links

The app needs an HTTPS callback for email-link sign-in.

Tomorrow, update the Android configuration to use the real host:

```text
TODO_FIREBASE_AUTH_HOST
```

Then make sure:

1. The Hosting/auth domain matches the link the email uses.
2. The app manifest accepts that host.
3. The email link returns to the app with the same package name and host.

## 7. Fill The Placeholder Build Values

The code will use placeholder build values until the project is configured.

Tomorrow, set real values for:

```text
FIREBASE_API_KEY
FIREBASE_APPLICATION_ID
FIREBASE_PROJECT_ID
FIREBASE_STORAGE_BUCKET
FIREBASE_AUTH_DOMAIN
FIREBASE_AUTH_HOST
FIREBASE_ANDROID_PACKAGE_NAME
FIREBASE_EMAIL_LINK_URL
```

Expected meaning:

- `FIREBASE_API_KEY`: Firebase Web API key from project settings
- `FIREBASE_APPLICATION_ID`: Firebase Android app id
- `FIREBASE_PROJECT_ID`: Firebase project id
- `FIREBASE_STORAGE_BUCKET`: Firebase storage bucket value from project settings
- `FIREBASE_AUTH_DOMAIN`: auth domain used for email-link flow
- `FIREBASE_AUTH_HOST`: host part accepted by the Android app link intent filter
- `FIREBASE_ANDROID_PACKAGE_NAME`: `com.quran.tathbeet`
- `FIREBASE_EMAIL_LINK_URL`: full HTTPS URL used as the email link continue URL

## 8. Verify Firestore Security Rules

The implementation will assume rules that enforce:

- only authenticated users can read synced profile data
- only profile members can read a profile, plan, and tasks
- only owners can manage invites and remove other members
- editors may remove only themselves
- owners cannot remove themselves while editors still exist

Tomorrow, review the final rules before production use.

Use this companion checklist while writing and deploying the rules:

- `docs/manual/firestore-rules-checklist.md`

Current implementation assumptions:

- `profiles/{profileId}/members/{normalizedEmail}` stores owner and editor membership
- `users/{uid}/accessibleProfiles/{profileId}` stores the lightweight profile list for the signed-in adult
- the app claims memberships after sign-in by matching the Firebase Auth email against `members.email`

## 9. Verify Email-Link Flow On Device

After the real values are in place:

1. Install the debug app.
2. Start email-link sign-in from Settings.
3. Open the email on the same device first.
4. Confirm the app receives the link and finishes sign-in.
5. Confirm the self profile becomes synced immediately.
6. Confirm the same account restores on a second device.

## 10. Share Flow Verification

After auth works:

1. Sign in as owner.
2. Create or use a child profile.
3. Share it with a second email.
4. Sign in with the second email on another device.
5. Open the shared tab and confirm the child profile appears automatically for that second adult.
6. Confirm both devices see the same plan and task updates.

## 11. Do Not Commit

Do not commit:

- real Firebase API keys
- real auth domain values if you keep them in untracked local config
- `google-services.json`

Keep secrets local or in ignored config files.
