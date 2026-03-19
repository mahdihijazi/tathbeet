# Firestore Rules Checklist

This document is the manual checklist for writing and deploying Firestore rules that match the current sync implementation.

Do this after the Firebase project exists and Firestore is enabled.

## Collections Covered

The current code reads and writes these paths:

- `users/{uid}/accessibleProfiles/{profileId}`
- `profiles/{profileId}`
- `profiles/{profileId}/members/{normalizedEmail}`
- `profiles/{profileId}/plan/meta`
- `profiles/{profileId}/reviewDays/{date}`
- `profiles/{profileId}/tasks/{taskId}`

## Minimum Rule Intent

The rules should enforce:

- only signed-in users can touch sync data
- only profile members can read a profile and its subcollections
- only the owner can:
  - update top-level owner fields
  - invite editors by creating editor membership docs
  - remove other editors
  - delete a shared profile
- editors can:
  - read the shared profile
  - update plan, review day, and task data
  - remove only their own membership if you decide to support leave-via-rules directly
- owners cannot remove themselves while any editor memberships still exist

## Practical Checks Before Deploy

1. Sign in as the owner and verify owner reads/writes work for:
   - `profiles/{profileId}`
   - `plan/meta`
   - `reviewDays`
   - `tasks`
2. Sign in as an invited editor and verify:
   - reads work
   - plan/task writes work
   - membership writes do not work except self-leave if you allow it
3. Verify a random signed-in user with no membership cannot read:
   - `profiles/{profileId}`
   - `users/{uid}/accessibleProfiles/{profileId}` for another user
4. Verify the owner cannot delete the shared profile while editor membership docs still exist.
5. Verify removing an editor immediately removes:
   - their member doc
   - their `users/{uid}/accessibleProfiles/{profileId}` doc

## Deployment Steps

1. Write the final `firestore.rules` file locally.
2. Deploy the rules with the Firebase CLI against the Tathbeet project.
3. Re-test owner and editor flows on two devices before relying on production data.

## Open Caution

Because the app claims memberships by matching `request.auth.token.email` against `members.email`, your rules should treat email identity carefully and keep membership writes owner-controlled.
