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
- view completion rate
- manage notification settings already stored on device

Internet is only required for:

- Google sign-in
- cloud sync
- shared profile collaboration across devices
- remote push delivery where applicable

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

Daily completion should support both:

- marking the whole day as done
- marking individual review segments as done

This is important because a selected surah or juz may be completed in smaller pieces even though the original selection was not entered as rub-level items.

For MVP reporting, the visible outcome is still simple:

- day status: done or not done

Internally, partial segment completion can be used to determine whether the day becomes fully done.

## 10. Notifications

### 10.1 MVP Notification Types

- daily reminder
- motivational reminder

### 10.2 Notification Controls

Notification settings must be configurable from the settings page.

Controls should allow:

- enable/disable notifications globally
- enable/disable notifications per profile
- configure preferred reminder time

### 10.3 Multi-Profile Notifications

If multiple profiles exist on one device, notifications may include the profile name to avoid ambiguity.

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
- user can choose memorized content using surah, juz, hizb, or rub al-hizb
- memorized-content selection should use a dedicated selection screen rather than a compact inline control
- the memorized-content screen should keep the top summary and navigation area fixed while only the item list scrolls
- category switching in the memorized-content screen should use swipeable tabs rather than button-like chips
- after the first intro has been seen, reopening schedule setup should start from the memorized-content selection step
- daily-ward setup should default to choosing when the user wants to finish one full revision cycle
- daily-ward setup should also allow a manual pace override through a secondary bottom-sheet flow
- user can review a lightweight summary before saving, focused on pool size, target cycle, resolved daily equivalent, and estimated rotation length
- saving the wizard should take the user directly to today’s review screen
- the daily-ward screen should stay visually focused and avoid noisy helper blocks that do not help the user complete setup

### 13.3 Daily Review

- app shows today’s assigned review items
- review items should be execution units, not raw pool labels like `الجزء 30`
- if a task spans multiple short surahs, the main task label should read like `من النبأ إلى المرسلات`
- if a task stays inside one long surah, the labeling should use Quran range detail with ayah numbering
- user can mark individual items done
- app can determine when the day is complete
- incomplete items roll over to the next day
- the review screen should stay structurally simple: compact summary, task list, and status-only bottom card
- editing the revision plan should be available from the review screen top app bar rather than from the review body

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

### 13.7 Localization and UI Composition

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
