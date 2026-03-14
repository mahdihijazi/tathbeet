# Cloud Sync System Plan

## 1. Goal

Build the smallest cloud sync system that satisfies these product requirements:

- a signed-in adult can keep their own profile synced across devices
- a shared learner profile can be updated by multiple adults on different devices
- all adults with access can edit the full revision plan, including cycle edits and cycle restart
- the original creator remains the only owner
- the system does not require a custom backend service
- local reminder settings stay local to each device

## 2. Chosen Approach

- `Firebase Auth` with email-link sign-in for adult identity
- `Cloud Firestore` as the shared source of truth for learner profiles, revision plans, cycles, and daily tasks
- `Room` and local Android storage for device-local settings and cached app state
- no Google sign-in in MVP
- no custom backend service in MVP
- no import/export in MVP
- no audit log in MVP

This keeps the implementation small while still supporting real collaborative editing.

The same cloud model should support two states:

- solo synced profile: one owner, no editors
- shared learner profile: one owner, one or more editors

## 3. Scope Split

### Shared in Firestore

- learner profile metadata
- owner and editor membership
- memorized-content pool selections
- plan pacing settings
- active cycle metadata
- generated daily tasks
- completion and rating state

### Local Only

- app notification toggles
- preferred reminder time
- Android notification permission state
- temporary UI state
- Quran reference assets bundled with the app

## 4. Roles

### Owner

- original creator of the cloud-synced profile
- only member for a solo synced profile
- can invite editors
- can remove editors
- can edit all shared learner data
- cannot transfer ownership in MVP

### Editor

- can fully edit the revision plan
- can add or edit cycles
- can restart the cycle
- can update task completion and ratings
- cannot invite or remove other adults
- cannot change owner-only fields

## 5. Firestore Data Model

The cloud-sync model should avoid one giant schedule document. Firestore handles concurrent edits more safely when profile state is split across smaller records.

### Collections

- `users/{uid}`
- `profiles/{profileId}`
- `profiles/{profileId}/members/{uid}`
- `profiles/{profileId}/invites/{inviteId}`
- `profiles/{profileId}/plan/meta`
- `profiles/{profileId}/plan/cycles/{cycleId}`
- `profiles/{profileId}/tasks/{taskId}`

### `users/{uid}`

Purpose:

- minimal adult identity record
- display name if needed later
- normalized email reference

Suggested fields:

- `email`
- `normalizedEmail`
- `createdAt`
- `lastSeenAt`

### `profiles/{profileId}`

Purpose:

- top-level learner profile record

Suggested fields:

- `displayName`
- `ownerUid`
- `status`
- `createdAt`
- `updatedAt`
- `updatedBy`

### `profiles/{profileId}/members/{uid}`

Purpose:

- membership and ownership for one cloud-synced profile

Suggested fields:

- `role` with values `owner` or `editor`
- `joinedAt`
- `addedBy`

### `profiles/{profileId}/invites/{inviteId}`

Purpose:

- pending invite before the invited adult accepts access

Suggested fields:

- `normalizedEmail`
- `role`
- `status` with values `pending`, `accepted`, `revoked`
- `createdAt`
- `createdBy`
- `acceptedAt`
- `acceptedBy`

### `profiles/{profileId}/plan/meta`

Purpose:

- stable plan-level settings

Suggested fields:

- `selectionPool`
- `paceMode`
- `targetCycleDays`
- `manualPace`
- `activeCycleId`
- `planVersion`
- `updatedAt`
- `updatedBy`

### `profiles/{profileId}/plan/cycles/{cycleId}`

Purpose:

- one concrete cycle instance

Suggested fields:

- `state` with values like `active`, `completed`, `archived`
- `startsOn`
- `endsOn`
- `planVersion`
- `createdAt`
- `createdBy`
- `restartedFromCycleId`

### `profiles/{profileId}/tasks/{taskId}`

Purpose:

- one execution unit for one cycle

Suggested fields:

- `cycleId`
- `taskOrder`
- `scheduledDate`
- `status`
- `rangeStart`
- `rangeEnd`
- `label`
- `rating`
- `completedAt`
- `completedBy`
- `updatedAt`
- `updatedBy`

## 6. Sync Rules

- Firestore is the source of truth for every cloud-synced profile
- the Android app reads from Firestore with offline persistence enabled
- writes made offline are queued locally and synced when connectivity returns
- if two adults edit the exact same field on the exact same document at nearly the same time, last write wins is acceptable in MVP
- to reduce collisions, cycle and task data must be stored as separate documents instead of embedding the whole plan in one record

### Plan Edit Rule

When an adult edits the revision plan:

- update `plan/meta`
- create a new active cycle document
- generate a fresh set of task documents for that cycle
- mark the old active cycle as archived or superseded

This should happen in one transaction or one batched write so all devices observe one coherent plan version.

### Cycle Restart Rule

When an adult restarts the cycle:

- create a new active cycle document
- generate a fresh task set for the new cycle
- keep the previous cycle document as completed or archived
- point `plan/meta.activeCycleId` to the new cycle

This avoids mutating the old cycle in place and keeps collaborative sync simpler.

## 7. Invite Flow

The invite flow should avoid custom backend infrastructure.

Solo synced profiles do not need invites. They only need the owner membership record.

### Owner Invite Flow

1. Owner opens the learner profile sharing screen.
2. Owner enters the other adult's email address.
3. App normalizes the email and creates a pending invite document.
4. App can show a system share action with a simple message telling the other adult to install the app and sign in with that same email address.

### Invite Acceptance Flow

1. Invited adult signs in with Firebase email-link using the invited email address.
2. App checks for pending invites where `normalizedEmail` matches the signed-in email.
3. Adult accepts the invite.
4. App writes `members/{uid}` with role `editor`.
5. App marks the invite as accepted.

### Member Removal Flow

1. Owner opens the sharing screen.
2. Owner removes an editor.
3. App deletes or disables that member record.
4. Removed adults lose future shared access after sync refresh.

## 8. Security Rules Direction

The MVP should rely on Firestore Security Rules instead of a custom backend.

Rules should enforce:

- only authenticated adults can access cloud-synced learner data
- only members of a learner profile can read that profile, its plan, and its tasks
- only the owner can create invites or remove members
- editors can update plan, cycle, and task data
- editors cannot update `ownerUid` or membership documents
- invite acceptance is allowed only when the signed-in email matches the invited email

## 9. Android App Boundaries

Recommended repository split:

- auth layer for Firebase email-link session handling
- cloud-profile repository backed by Firestore
- local settings repository backed by Room or DataStore
- planner layer remains the source of task generation logic

The planner should stay deterministic and local to the app. Firestore stores the resulting synced state, not the scheduling algorithm itself.

## 10. Delivery Slices

### Slice 1: Auth Bootstrap

- email-link sign-in
- signed-in session restore
- minimal user document creation

### Slice 2: Owner-Created Synced Profile

- create a cloud-synced profile with owner membership
- restore the owner's synced profile on another signed-in device
- read synced profile data from Firestore

### Slice 3: Shared Profile Invite And Acceptance

- owner creates pending invite
- invited adult signs in and accepts
- owner can remove editor

### Slice 4: Collaborative Plan Editing

- edit plan metadata
- rebuild active cycle
- sync daily task edits across devices
- restart cycle from any editor account

### Slice 5: Polish

- better empty states around invites and sync
- clearer sync status feedback
- conflict handling UX if last-write-wins proves too confusing

## 11. Explicit Non-Goals For This Plan

- Google sign-in
- backup export/import
- custom backend service
- ownership transfer
- viewer-only role
- audit trail of every edit
- push-based collaboration alerts
