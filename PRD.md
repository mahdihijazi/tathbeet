# PRD: Tathbeet | تثبيت

## 1. Overview

### Product Name

- English: `Tathbeet`
- Arabic: `تثبيت`

### Product Summary

Tathbeet is an Android app for Quran memorization revision. It helps users maintain what they have already memorized by generating a manageable rotating review schedule, sending reminders, and tracking whether daily revision is being completed.

The product is for:

- individuals reviewing their own memorization
- people managing revision schedules for other learners, such as family members, teachers, or class managers

The app should be usable offline for core schedule and review workflows, while optional online features support authentication, sync, and shared access.

## 2. Problem Statement

People who memorize Quran often struggle with consistency in revision. After memorizing new content, older memorization can weaken without a simple, sustainable review system. Existing approaches are often manual, inconsistent, or hard to manage across multiple learners.

Tathbeet should make revision:

- structured
- realistic
- motivating
- family-friendly
- available even when offline

## 3. Goals

### Primary Goals

- help users maintain memorized Quran through recurring revision schedules
- reduce the mental overhead of deciding what to review each day
- encourage consistency through reminders and motivation
- support multiple profiles on one device
- allow optional account-based sync and backup

### MVP Goals

- ship an Android-only MVP
- validate product flow and UX through a high-fidelity interactive prototype before real backend implementation
- allow schedule creation from selected Quran portions
- generate daily review tasks from an active schedule
- support offline usage for core flows
- track daily completion
- support optional Google sign-in
- support notifications with per-profile controls
- support sync across devices for signed-in users
- support shared learner profiles for collaborative management

## 4. Non-Goals

The MVP will not include:

- full Quran text reading experience
- audio playback or recitation tools
- tajweed assistance
- teacher or coach dashboards
- advanced analytics beyond core completion rate
- export/import
- social sharing
- AI-generated recommendations
- manual custom ayah-range selection by the user

## 5. Target Users

### User Type A: Individual Reviewer

A person who has memorized some portion of the Quran and wants a simple system to maintain it.

### User Type B: Multi-Learner Manager

A person using one device to manage revision schedules for other learners, such as family members or a Quran class, including tracking completion and controlling reminders.

### User Type C: Co-Managers

Two people who want shared visibility and edit access for the same learner profile through optional cloud-backed sync.

## 6. Platforms and Technical Direction

### MVP Platform

- Android only

### App Architecture Direction

- native Android
- Kotlin
- Jetpack Compose
- Kotlin Flow

### UX and Localization Direction

- the product is Arabic-first and RTL-first
- English remains supported in the planned MVP product
- the current prototype should use Arabic only and should not mix Arabic and English in the same UI
- the prototype should render in RTL regardless of the device language so layout review is accurate during validation

### Data and Services

- local database: `Room`
- auth and sync: `Firebase`
- push notifications: `Firebase Cloud Messaging`
- local reminders: Android local notification scheduling

## 7. Core Product Decisions

### 7.1 Offline-First Behavior

Core schedule and review functionality must work offline:

- create and edit local profiles
- create and edit a schedule
- view today’s review tasks
- mark tasks as completed
- launch the assigned Quran portion in an external reader if one is installed locally
- view completion rate
- manage notification settings already stored on device

Internet is only required for:

- Google sign-in
- cloud sync
- shared profile collaboration across devices
- remote push delivery where applicable
- opening a web fallback for Quran reading when no supported Quran app is installed

### 7.2 Account Model

Accounts are optional in MVP.

- users can use the app without creating an account
- a guest/local user can later sign in with Google
- when they sign in, local data should be preserved and synced to their account
- account creation should not block first-run schedule setup
- the first-run experience should use a short one-time intro before the setup wizard, then proceed directly into schedule setup

### 7.3 Profile Model

The app supports multiple profiles on one device.

Each profile has:

- its own name
- its own memorized-content pool
- its own single active schedule
- its own daily tasks
- its own completion history
- its own notification settings

### 7.4 Schedule Model

Each profile can have only one active schedule in MVP.

The schedule is defined by:

- selected Quran content
- a target pace expressed in juz-per-day
- notification preferences

The stored pool should preserve the user’s original selections as chosen in the UI.

If the user misses work on a given day, unfinished work rolls over to the next day.

### 7.5 Prototype Validation Rules

Before implementation of real backend, database, or sync logic, the team should validate the product through a UI-only interactive prototype.

Prototype rules:

- all backend behavior may be faked locally in UI state during validation
- no network, Firebase, or Room implementation is required during prototype validation
- user-facing strings should come from Android XML resources rather than hardcoded Kotlin strings
- Arabic UI review should happen in RTL layouts
- prototype feedback must be reflected back into this PRD and the README when it changes UX, scope, or flow expectations

### 7.6 Implementation and Testing Rules

Real feature implementation should follow test-driven development:

- define the test first
- implement the smallest feature slice needed to satisfy the test
- make the test pass before moving to the next slice
- a feature is not complete until its relevant tests pass

Testing strategy should prefer:

- screenshot tests for reusable visual components and important visual states
- black-box Android UI/instrumentation tests for user journeys and screen behavior
- JVM unit tests only where pure logic is better tested outside instrumentation

Hard testing rules:

- do not use mocking libraries
- test doubles should be handwritten fakes, stubs, or no-op implementations
- prefer in-memory Room and real repositories for integration-style tests where practical

## 8. Quran Content Model

### 8.1 User-Selectable Units

Users can build a revision pool by selecting:

- surah
- juz
- hizb
- rub al-hizb

### 8.2 Pool Preservation Rule

The memorized-content pool should preserve the user’s original selections exactly as entered.

Examples:

- if the user selects a surah, the pool contains that surah
- if the user selects a surah and also selects a `rub al-hizb` that extends beyond that surah, the pool contains both items

The pool UI should reflect what the user explicitly selected, without rewriting those picks into smaller internal units.

### 8.3 Overlap Resolution Rule

The scheduling engine should resolve overlap automatically without asking the user.

Rules:

- if one selected item is fully contained inside another selected item, effective scheduling should keep only the larger outer coverage
- if two selected items overlap only partially, effective scheduling should merge the shared coverage and count that overlapping part once
- this behavior must be independent of selection order

Examples:

- `surah` + `rub al-hizb` fully inside that surah: effective coverage keeps the surah only
- `juz` + `surah` fully inside that juz: effective coverage keeps the juz only
- `surah` + `rub al-hizb` that starts inside the surah and extends into the next surah: effective coverage keeps both selections but counts the shared part once

### 8.4 Internal Scheduling Unit

For implementation, the scheduler should normalize effective coverage into `rub-equivalent` review size using real Quran reference data.

This means:

- the pool stores exact user selections
- the engine computes effective non-duplicated coverage from those selections
- daily pace calculations use approximate Quran size derived from real `rub al-hizb` coverage

This approach preserves:

- predictable daily pacing
- support for mixed selection types
- automatic handling of full containment
- automatic handling of partial overlap without duplicate workload
- separation between user-visible selections and internal scheduling logic

## 9. Rotation and Daily Planning

### 9.1 Rotation Objective

The app should support two ways to define revision pace:

- primary method: finish the full revision pool within a target cycle duration
- secondary method: choose a fixed manual daily pace

The default method in MVP should be target cycle duration because it is simpler and more outcome-oriented.

Supported cycle presets:

- `1 week`
- `2 weeks`
- `1 month`
- `45 days`
- `2 months`

For cycle-based setup:

- `1 month` should mean `30 days`
- weeks and months should convert internally to days
- the engine should calculate the needed daily load from real `rub-equivalent` coverage
- the generated assignments should be spread across the full selected duration rather than front-loading early days
- cycle-target mode should use the selected target duration as the real assignment-distribution window, not just as a rounded manual-pace recommendation
- if the exact pace is awkward, the app should round up to the next practical pace milestone

Supported manual pace milestones:

- `1 rub/day`
- `1 hizb/day`
- `1 juz/day`
- `1.5 juz/day`
- `2 juz/day`
- `2.5 juz/day`
- `3 juz/day`
- `4 juz/day`
- `5 juz/day`

### 9.2 Recommended Scheduling Logic

For MVP, the scheduler should:

- convert the selected memorization pool into normalized review segments
- calculate the required daily load from either the selected cycle target or the selected manual pace
- use real `rub-equivalent` coverage for that calculation
- expose the resolved daily equivalent to the user even when they chose a cycle target
- treat cycle-target mode and manual-pace mode as mutually exclusive UI states
- when manual pace is chosen, hide the cycle-target picker and show the selected manual pace clearly in the step UI
- create a daily task list from execution units rather than raw pool item labels
- use `rub-equivalent` execution units while keeping task labels friendly and Quran-aware
- continue from where the last completed task left off
- roll incomplete tasks into the next day before assigning new work

### 9.3 Task Completion

Daily completion should happen at the individual review-task level rather than through a single "mark the whole day done" action.

This is important because a selected surah or juz may be completed in smaller pieces even though the original selection was not entered as rub-level items.

For MVP reporting, the visible outcome is still simple:

- day status: done or not done

Internally, task completion determines whether the visible day becomes fully done.

Completing a task in MVP should also capture a simple retention rating:

- when the user taps `أنهيت المراجعة`, the task should be marked complete immediately
- rating uses a 1 to 5 star scale shown inline in the completed task row rather than in a dialog
- first-time completion should default to 3 stars
- if the same task was rated before in a previous cycle, the app should reuse the previous rating as the default/current value
- completed task rows should render completion status inline as part of the title text flow itself, in the form `عنوان المهمة - تمت المراجعة` with a small inline completed icon immediately after the text, instead of a detached check icon or a separate status bubble
- completed tasks should always show the saved rating inline
- users should be able to edit the rating later by tapping the inline stars directly

The review experience should also support early completion beyond today:

- the review screen should show the current cycle in one continuous list from the start
- that list should include:
  - carried-over work from previous days
  - today’s work
  - tomorrow’s work
  - all later dates until the end of the current revision cycle
- future dated ward should appear inline in the same list rather than through tabs or a date picker in MVP
- tomorrow should use the relative label `ورد الغد`
- dates after tomorrow should use explicit calendar dates
- if the user completes tomorrow's ward today, tomorrow should appear already complete on its actual date
- early completion should still count as work done on the day the user actually performed it
- progress and history should distinguish scheduled-day completion from actual completion date
- once the user finishes the whole current cycle, the app should offer a clear way to start the cycle again
- restarting the cycle may use a confirmation dialog in MVP, as long as the action is explicit and intentional

## 10. Notifications

### 10.1 MVP Notification Types

- daily reminder
- motivational reminder

For the offline single-device MVP, notifications should stay intentionally calm:

- use local Android notifications only
- do not depend on authentication, cloud sync, or push delivery
- send at most one scheduled reminder per enabled profile per day
- do not send repeated same-day nudges for skipped work
- reflect overdue work in the next day's reminder instead of escalating notification volume

### 10.2 Notification Controls

Notification settings must be configurable from the settings page.

Controls should allow:

- enable/disable notifications globally
- enable/disable notifications per profile
- configure preferred reminder time

MVP settings behavior:

- use one app-wide preferred reminder time rather than separate times per profile
- keep per-profile control as a simple enable/disable toggle
- save settings locally on device
- allow the user to configure reminder settings even if Android notification permission has not yet been granted
- show a clear permission prompt entry point in Settings when notification permission is missing

### 10.3 Multi-Profile Notifications

If multiple profiles exist on one device, notifications may include the profile name to avoid ambiguity.

MVP defaults for multi-profile reminders:

- the self/main profile should start with reminders enabled once it has a schedule
- newly created additional learner profiles should start with reminders disabled
- users can enable reminders for additional profiles individually
- notification taps should open the app directly to the review screen for the relevant profile
- if overdue work exists, the reminder copy should mention that today's work includes carried-over tasks

## 11. Motivation and Progress

### 11.1 Motivation

The app should send both:

- generic encouraging messages
- Islamic motivational reminders

The tone should support consistency without guilt-heavy wording.

### 11.2 Progress Tracking

The MVP should track:

- completion rate

The MVP will not include:

- streak systems unless added later
- calendar/history views
- advanced graphs

## 12. Sharing and Collaboration

### 12.1 Managed Profiles

A user should be able to create and edit additional learner profiles locally on one device.

### 12.2 Shared Learner Profile

The MVP should support a shared learner profile so more than one manager can update the same learner profile and mark tasks as done from different devices.

This requires:

- authenticated accounts
- cloud-backed profile ownership and permissions
- synchronized schedule and task state

The data model should support shared ownership and conflict-safe synchronization from the beginning.

## 13. Functional Requirements

### 13.1 Profile Management

- users can create multiple profiles on one device
- users can edit profile names
- users can delete profiles
- each profile has independent schedule and settings

### 13.2 Schedule Creation

- user can create one active schedule per profile
- schedule setup should use a wizard flow rather than a single crowded screen
- the wizard should show a one-time intro screen on first app open
- the intro step should ask for the active profile name with a lightweight single input before the user moves into memorized-content selection
- user can choose memorized content using surah, juz, hizb, or rub al-hizb
- memorized-content selection should use a dedicated selection screen rather than a compact inline control
- the memorized-content screen should keep the top summary and navigation area fixed while only the item list scrolls
- category switching in the memorized-content screen should use swipeable tabs rather than button-like chips
- the user must select at least one memorized-content item before moving from the memorized-content step to the daily-ward step
- the next action on the memorized-content step should stay disabled until at least one item is selected
- after the first intro has been seen, reopening schedule setup should start from the memorized-content selection step
- daily-ward setup should default to choosing when the user wants to finish one full revision cycle
- when the user first reaches the daily-ward step, it should open in target-cycle mode rather than manual-pace mode
- daily-ward setup should also allow a manual pace override through a secondary bottom-sheet flow
- user can review a lightweight summary before saving, focused on pool size, target cycle, resolved daily equivalent, and estimated rotation length
- saving the wizard should take the user directly to today’s review screen
- the daily-ward screen should stay visually focused and avoid noisy helper blocks that do not help the user complete setup

### 13.3 Daily Review

- app shows the full current revision cycle in one unified list
- the list begins with carried-over overdue work, then today’s work, then future dated work until the end of the current cycle
- tomorrow should be labeled `ورد الغد`
- days after tomorrow should use explicit date labels
- once the full current cycle is complete, the user should be offered an explicit action to restart the cycle and begin again from the start
- the review screen top app bar should also allow the user to reset the current cycle at any time
- resetting the current cycle is destructive and must show a warning confirmation before removing current-cycle progress
- the review screen should show a compact top progress summary card for today’s work scope
- the review screen top bar should clearly show which local profile is currently active so the visible plan is not ambiguous on shared devices
- that top summary should count:
  - carried-over overdue work
  - today’s assigned work
- the top summary should not represent the whole cycle
- that top summary should focus on completed count, remaining count, and clear progress through today’s work scope
- review items should be execution units, not raw pool labels like `الجزء 30`
- if the user explicitly selected a surah and that surah is small enough to fit within the size of one `rub al-hizb`, it should appear as its own separate task in the review list
- if a selected surah is larger than one `rub al-hizb`, it should be split into rub-based tasks
- if the user selected a `juz` or `hizb`, the engine should split into surah tasks only when a surah is fully contained inside that selected scope and small enough to fit within the size of one `rub al-hizb`
- explicit `rub al-hizb` selections should remain rub-based tasks
- when several short surahs qualify, each surah should appear as its own separate task
- if a short surah crosses two consecutive non-explicit `rub al-hizb` boundaries, it should still appear as one surah task rather than two partial rub tasks
- completion granularity should follow the displayed task granularity
- progress in the top summary should be weighted by real review size rather than by raw task count, so short-surah tasks do not count the same as a full rub-based task
- if a task spans multiple short surahs, the main task label should read like `من النبإ إلى المرسلات`
- if a task stays inside one long surah, the labeling should use Quran range detail with ayah numbering
- user completes individual items from the task list
- app can determine when the day is complete
- incomplete items roll over to the next day
- review should support three categories of work:
  - carried-over work from previous days
  - current day assignments
- future dated assignments already visible inline through the end of the current cycle
- the review screen should stay structurally simple: compact top summary and a unified task list separated by section headers
- each task row should include a secondary action to open that exact Quran range in an external reader
- if Quran for Android is installed, tapping that action should open the task directly with a `quran://surah/ayah` deep link
- if Quran for Android is not installed, the app should show a dialog offering:
  - install Quran for Android from the Play Store
  - open the same range on `quran.com` in the system browser
- editing the revision plan should be available from the review screen top app bar rather than from the review body
- cycle reset should also be available from the review screen top app bar as a separate action from editing the plan
- saving an edited revision plan should rebuild the active visible review cycle from the updated pool so the review list reflects the latest selections immediately

### 13.4 Offline Behavior

- core flows work without internet
- changes made offline sync later if the user has an account

### 13.5 Authentication

- Google sign-in only
- guest users supported
- guest users can later sign in and keep their local data

### 13.6 Notifications

- daily reminders supported
- motivational notifications supported
- notification settings are user-configurable
- notifications use local scheduling before any authentication or cloud sync work is implemented
- reminder scheduling uses one app-wide preferred time with per-profile enable/disable controls
- the app should persist notification settings locally and restore scheduled reminders on device restart

### 13.7 Settings Screen

- the MVP settings screen should focus on local reminder behavior rather than account management
- the screen should expose:
  - app-level notification enable/disable
  - motivational reminder enable/disable
  - one preferred reminder time for the whole app
  - per-profile reminder toggles
- the per-profile list should show profiles directly when the list is short
- if the profile list grows beyond five entries, the extra entries may be collapsed behind an expandable section
- the active profile, self profile, and profiles with reminders already enabled should remain visible before collapsed overflow when practical

### 13.8 Localization and UI Composition

- the product UI should be designed as RTL-first
- screen layouts, navigation placement, chip flow, and action alignment should be reviewed from an Arabic reading direction first
- no mixed Arabic and English copy should appear in the prototype
- user-facing copy should be stored in string resources to support review and later localization

## 14. UX Requirements

### 14.1 Language and Layout

- support Arabic and English
- Arabic should be the primary branding and RTL-first experience
- English name `Tathbeet` should remain visible where useful
- prototype validation should use Arabic-only visible copy
- prototype validation should force RTL rendering even on non-Arabic test devices

### 14.2 UX Principles

- quick setup
- low cognitive load
- calm and motivating presentation
- simple daily action flow
- clear separation between profiles
- prefer focused setup screens over crowded multi-purpose layouts

## 15. Success Criteria for MVP

The MVP is successful if a user can:

1. install the app on Android
2. create one or more profiles
3. define memorized Quran content
4. choose a daily revision pace
5. receive a daily review plan
6. mark revision as completed offline
7. optionally sign in and sync data

## 16. Open Product Questions

These items should be resolved during design and implementation planning:

- exact algorithm for converting juz-per-day targets into mixed review segments
- exact permissions model for shared learner profiles
- whether motivational content is bundled locally, fetched remotely, or both
- how much schedule editing is allowed once a profile has active progress

## 17. Suggested Initial Milestones

### Milestone 1: Offline Single-Device MVP

- profile management
- local schedule creation
- daily task generation
- completion tracking
- local notifications

### Milestone 2: Accounts, Sync, and Shared Profiles

- Google sign-in
- sync local data to Firebase
- restore data across devices
- collaboration permissions for shared profiles
- synchronized updates by multiple managers

### Milestone 3: Product Polish

- motivational content strategy
- schedule editing refinement
- collaboration conflict handling polish
