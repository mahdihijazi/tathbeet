# Cloud Sync System Plan

## 1. Goal

Build the smallest cloud sync system that satisfies these product requirements:

- a signed-in adult can keep their own profile synced across devices
- a shared learner profile can be updated by multiple adults on different devices
- all adults with access can edit the full revision plan, including cycle edits and cycle restart
- the original creator remains the only owner
- the system does not require a custom backend service
- local reminder settings stay local to each device

This plan describes the current Firebase-backed adapter, but the app layer is written against app-owned auth and cloud-sync interfaces so another provider can replace Firebase later without refactoring the whole app.

## 2. Chosen Approach

- app-owned cloud auth interface, currently backed by Firebase Auth email-link sign-in
- app-owned cloud sync store interface, currently backed by Cloud Firestore for learner profiles, revision plans, cycles, and daily tasks
- `Room` and local Android storage for device-local settings and cached app state
- no Google sign-in in MVP
- no custom backend service in MVP
- no import/export in MVP
- no audit log in MVP

This keeps the implementation small while still supporting real collaborative editing.

The same cloud model should support two states:

- solo synced profile: one owner, no editors
- shared learner profile: one owner, one or more editors

Each signed-in adult account should have exactly one solo synced personal profile in MVP.
That personal profile should become cloud-synced immediately when the adult signs in.

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
- cannot leave or delete a shared profile while any editors still remain

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
- `users/{uid}/accessibleProfiles/{profileId}`
- `profiles/{profileId}/members/{normalizedEmail}`
- `profiles/{profileId}/plan/meta`
- `profiles/{profileId}/reviewDays/{date}`
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

### `users/{uid}/accessibleProfiles/{profileId}`

Purpose:

- small membership summary for the signed-in adult
- quick list/query source without loading full profile documents

Suggested fields:

- `displayName`
- `ownerEmail`
- `syncMode`
- `role`
- `updatedAt`

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

### `profiles/{profileId}/members/{normalizedEmail}`

Purpose:

- membership and ownership for one cloud-synced profile keyed by normalized email

Suggested fields:

- `email`
- `userId` once that adult signs in on a device
- `role` with values `owner` or `editor`
- `invitedBy`
- `updatedAt`

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

### `profiles/{profileId}/reviewDays/{date}`

Purpose:

- lightweight day summary keyed by assigned date

Suggested fields:

- `assignedForDate`
- `completionRate`
- `updatedAt`

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
- regenerate the `reviewDays` summaries
- regenerate the task documents that belong to the active cycle

This should happen in one transaction or one batched write so all devices observe one coherent plan version.

### Cycle Restart Rule

When an adult restarts the cycle:

- keep the same learner profile and plan metadata
- regenerate `reviewDays`
- generate a fresh task set for the restarted cycle window
- preserve completed ratings locally where the review engine already supports that behavior

This keeps collaborative sync simpler because all active review state still lives in `reviewDays` and `tasks`.

## 7. Invite Flow

The invite flow should avoid custom backend infrastructure.

Solo synced profiles do not need invites. They only need the owner membership record.

### Owner Invite Flow

1. Owner opens the learner profile sharing screen.
2. Owner enters the other adult's email address.
3. App normalizes the email and creates `members/{normalizedEmail}` with role `editor`.
4. App can show a simple share message telling the other adult to install the app and sign in with that same email address.

### Invite Acceptance Flow

1. Invited adult signs in with the configured cloud auth provider using the invited email address.
2. App queries the `members` collection group for documents where `email` matches the signed-in email.
3. App claims those profiles into `users/{uid}/accessibleProfiles`.
4. App writes the signed-in `userId` back into the matching membership document.

### Member Removal Flow

1. Owner opens the sharing screen.
2. Owner removes an editor.
3. App deletes that member record.
4. Removed adults lose future shared access after sync refresh.

### Editor Leave Flow

1. Editor opens the shared learner profile.
2. Editor chooses to leave that shared profile.
3. App removes that editor membership.
4. The editor loses access immediately after sync refresh.

### Profile Removal Rule

- editors may remove themselves at any time
- removing one editor membership must not delete the profile data while other memberships remain
- the owner must not be allowed to remove their own membership while any editors still remain
- the owner must remove all editors before deleting a shared profile
- deleting a shared profile should delete its Firestore data only after no editor memberships remain
- deleting a solo synced profile should delete it because no other memberships exist

## 8. Security Rules Direction

The MVP should rely on Firestore Security Rules instead of a custom backend.

Rules should enforce:

- only authenticated adults can access cloud-synced learner data
- only members of a learner profile can read that profile, its plan, and its tasks
- only the owner can create invites or remove members
- editors may delete only their own membership
- the owner may not delete their own membership from a shared profile while other member documents still exist
- editors can update plan, cycle, and task data
- editors cannot update `ownerUid` or membership documents
- invite acceptance is allowed only when the signed-in email matches the invited email

## 9. Android App Boundaries

Recommended repository split:

- auth layer for the configured cloud auth provider
- cloud-profile repository backed by the configured cloud sync provider
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
- convert the local self profile into the synced personal profile immediately after sign-in
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

## 11. Estimated Firebase Cost

These estimates are based on the current official Firebase and Firestore pricing documentation and on the current Tathbeet sync design.

Important setup note:

- this plan should use the `Blaze` plan from the beginning
- the reason is not expected spend, but email-link auth limits
- Firebase Auth currently allows only `5 email link sign-in emails/day` on Spark and `25,000/day` on Blaze, which makes Spark too restrictive for real testing and launch

### Pricing Basis

- Firestore no-cost tier: `50,000 reads/day`, `20,000 writes/day`, `20,000 deletes/day`, `1 GiB` storage, `10 GiB/month` outbound data
- Firestore operation rates used for this estimate: `$0.06 / 100,000 reads`, `$0.18 / 100,000 writes`, `$0.02 / 100,000 deletes`
- Firebase Auth with Identity Platform pricing on Blaze starts charging after `50,000 MAUs`, so auth cost should remain effectively `$0` for MVP-scale usage

Sources:

- [Firebase pricing](https://firebase.google.com/pricing)
- [Firebase Authentication](https://firebase.google.com/docs/auth/)
- [Firebase Authentication limits](https://firebase.google.com/docs/auth/limits)
- [Firestore pricing](https://firebase.google.com/docs/firestore/pricing)
- [Firestore Native billing example](https://cloud.google.com/firestore/native/docs/billing-example)

### Tathbeet Usage Assumptions

These are product-specific assumptions for the current app design:

- one signed-in active profile opens synced review data `2` times per active day
- each open loads about `63` documents:
  - `1` profile document
  - `1` membership or ownership document
  - `1` plan metadata document
  - `60` task or cycle-visible documents
- this gives roughly `126 reads/day` per active signed-in profile
- each active signed-in profile performs about `8 writes/day`
  - task completion updates
  - rating edits
  - occasional plan or cycle metadata updates averaged into the estimate
- average retained storage per synced profile is about `0.25 MB`
- these estimates assume moderate collaboration, not continuous long-lived listeners between several adults all day

If shared profiles become the dominant use case and several adults keep the same learner profile open at the same time, add about `15% to 25%` to the read and write budget.

### Cost Per Active Signed-In Profile

Using the assumptions above:

- reads: about `126/day`, or `3,780/month`
- writes: about `8/day`, or `240/month`
- estimated Firestore operations cost per fully active signed-in profile is about `$0.0027/month` before free-tier offsets

This means the sync architecture is operationally cheap as long as reads stay bounded to the active profile and active cycle.

### Monthly Scenario Estimates

These scenarios use:

- the assumptions above
- the Firestore free tier
- storage estimates based on `0.25 MB` per synced profile
- auth cost assumed to remain `$0` at MVP scale
- network egress excluded from the main total because it is highly region-dependent after the free tier, though it should stay negligible at pilot scale

| Scenario | DAU | Signed-in MAU | Estimated monthly cost |
| --- | ---: | ---: | ---: |
| Pilot test | 200 | 1,000 | about `$0` |
| Small launch | 1,000 | 5,000 | about `$1.41/month` |
| Growing MVP | 5,000 | 25,000 | about `$12.47/month` |
| Upper MVP range | 10,000 | 50,000 | about `$27.09/month` |

### Optional Egress Add-On

Inference:

- if synced payloads average about `100 KB/day` per active signed-in profile, outbound network usually stays within the free tier at pilot and small-launch scale
- at around `5,000 DAU`, extra outbound traffic may add roughly another `$0.5/month`
- at around `10,000 DAU`, extra outbound traffic may add roughly another `$2 to $3/month`

### Practical Conclusion

For the current Tathbeet design:

- Firebase is financially reasonable for MVP
- Blaze is required because of email-link sign-in limits, not because the app is expected to be expensive
- with efficient queries, the likely monthly bill should stay near `$0` in pilot and low-single-digits in a small launch
- the biggest cost risk is unnecessary Firestore reads, especially loading too many task documents or using broad listeners too often

### Cost-Control Implementation Rules

These implementation rules should be treated as part of the sync design, not optional optimizations.

- only listen to the active profile, never all profiles at once
- only query the active cycle and the visible task window for the current screen
- do not load full completion history on app launch
- paginate or defer older tasks and previous cycles behind explicit user actions
- avoid long-lived listeners for screens the user is not actively viewing
- prefer one-shot reads for low-change screens like sharing settings or membership lists
- keep `plan/meta`, cycle records, and task records separate so small edits do not force rereading a large schedule blob
- update task documents directly instead of rewriting the whole plan after each completion
- denormalize small UI-critical fields when it avoids fan-out reads, for example the active cycle id or lightweight profile summary counts
- cache stable Quran reference data locally and never store or fetch that catalog from Firestore
- keep local device settings entirely outside Firestore
- avoid collection scans by always querying tasks through indexed fields such as `cycleId`, `scheduledDate`, and status
- do not attach listeners for profiles the signed-in user cannot currently open from the visible UI
- when reopening the app, restore from local cache first and let Firestore refresh in the background
- keep invite documents short-lived and delete or archive them after acceptance or revocation
- if a screen only needs counts or status chips, store lightweight derived summary fields instead of recomputing them from many task reads every time

### Query Design Tips

- the review screen should read only:
  - the profile document
  - the caller's membership document
  - `plan/meta`
  - tasks for the active cycle and current visible range
- the profiles list should show lightweight summary cards and should not subscribe to every task collection for every profile
- the shared-profile screen should read members and pending invites only while that screen is open
- progress should start with a small recent window, such as the last `7` days, instead of the full history

### Data Retention Tips

- keep only the active cycle hot in the default queries
- archive older cycles out of the default UI path
- consider pruning or compacting old per-task history later if history volume grows faster than expected

## 12. Explicit Non-Goals For This Plan

- Google sign-in
- backup export/import
- custom backend service
- ownership transfer
- viewer-only role
- audit trail of every edit
- push-based collaboration alerts

## 13. Post-MVP Firebase Optimization Backlog

If the MVP ships before the sync layer is fully optimized, defer the remaining improvements to:

- [Firebase Sync Follow-Ups](/Users/mahdi/personal-repos/tathbeet/docs/firebase-sync-followups.md)

That backlog should stay separate from the MVP path so the current sync implementation remains simple until the main product flow is complete.
