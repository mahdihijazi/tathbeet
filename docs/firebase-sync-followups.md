# Firebase Sync Launch-Critical Fixes

This document captures Firebase sync changes that are required before launch.

The current sync approach is intentionally simple, but the current read/write pattern is too expensive to leave in place for launch:

- the app builds a full cloud snapshot for a profile when it needs to sync
- Firestore writes the full profile tree when that snapshot changes
- Room stays the source of truth on device

That is acceptable only for internal prototyping. In real testing, even a single active user can burn through the Firestore free tier if the app keeps re-reading and rewriting the same tree.

## Why These Items Are Deferred

These changes are still useful, but they are no longer optional cleanup:

- they require diffing local and remote state
- they need more targeted Firestore write logic
- they increase the number of edge cases around partial failures and retries
- they make the sync path harder to reason about while the app is still changing quickly
- they are the difference between a cheap pilot and a billable launch

## Post-MVP Work Items

### Incremental task writes

When only one task rating or completion state changes, update only:

- the affected `tasks/{taskId}` document
- the affected `reviewDays/{date}` summary document if the completion rate changed

Do not rewrite unchanged task documents or unrelated days.

### Diff-based profile sync

Teach the sync coordinator to compare the local profile snapshot with the remote snapshot and only write the fields that actually changed.

This should avoid rewriting:

- `profiles/{profileId}`
- `users/{uid}/accessibleProfiles/{profileId}`
- `plan/meta`
- unchanged `reviewDays`
- unchanged `tasks`

### Narrower listener scope

Only subscribe to the smallest Firestore path needed for the visible screen or active edit flow.

Examples:

- review should watch the active cycle and visible range only
- profiles should read lightweight summary data instead of every task document
- shared-profile settings should load members only while that screen is open

### Derived summary updates

Keep lightweight summary fields up to date without rebuilding the full profile tree.

Examples:

- update `completionRate` only when task completion changes
- update simple counters instead of recomputing them from every task on every sync

### Optional write coalescing

If the user makes several edits quickly, group them into one sync pass where practical.

This can reduce repeated writes without changing the data model.

## Success Criteria

These improvements are done when:

- changing one rating does not rewrite unrelated tasks
- changing one day summary does not touch the entire profile tree
- Firestore Console shows a small, targeted write set for small edits
- the app still works offline-first and remains simple to reason about

## MVP Boundary

Do not treat any of the above as optional after the MVP ships. These changes must be completed before launch or the app can generate avoidable Firestore cost from a single tester.
