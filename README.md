# Tathbeet | تثبيت

Tathbeet is an Android app for Quran revision (`مراجعة حفظ القرآن الكريم`) that helps users keep what they have memorized active through simple, repeatable review schedules.

The core idea is:

- users choose what they have memorized from the Quran
- they set a revision pace, such as `0.5`, `1`, `2`, or `3` juz per day
- the app builds a rotating daily review plan
- the app sends reminders and motivational notifications
- the app works offline for core review and schedule tracking
- each review task can jump straight into an external Quran reader
- the app supports multiple local profiles, such as one user managing multiple learners in a family or class

## Product Direction

Tathbeet is designed for two main use cases:

- an individual who wants to protect and maintain their memorization
- a person managing revision schedules for multiple learners on the same device, including family or class use cases

The app is Android-only in the MVP and is planned as a native app using:

- Kotlin
- Jetpack Compose
- Kotlin Flow
- Room for local offline storage
- Firebase for optional authentication, sync, and push notifications

## MVP Scope

The MVP supports:

- Android app with Arabic-first RTL UX and English support
- offline-first core flows for creating schedules, viewing daily tasks, and marking revision progress
- external Quran launch from each task row, with direct app opening first and browser fallback if needed
- optional account creation using Google sign-in
- use without an account
- local profiles for multiple users on one device
- one active revision schedule per profile
- Quran selection by:
  - surah
  - juz
  - hizb
  - rub al-hizb
- daily revision targets based on juz-per-day pacing
- daily reminders
- motivational notifications
- a simple Progress screen with today's summary, a weekly rhythm view, and one motivational card
- profile-level notification settings
- cloud sync for signed-in users
- shared learner profiles so more than one manager can update schedule and completion state

The MVP does not include:

- Quran text display
- audio playback
- advanced analytics
- teacher dashboards
- export/import
- social features
- AI recommendations

## Scheduling Model

Users can add memorized content to a rotation pool using `surah`, `juz`, `hizb`, and `rub al-hizb`.

The pool should preserve the user’s original selections exactly as chosen.

Internally, the scheduler should separately compute effective coverage from those selections:

- full containment is resolved automatically by keeping the larger outer selection
- partial overlap is resolved automatically by counting shared coverage once
- daily pace uses approximate Quran size based on real `rub al-hizb` coverage

This allows the app to support practical pacing such as:

- cycle target presets like `1 week`, `2 weeks`, `1 month`, `45 days`, and `2 months`
- manual pace presets from `1 rub/day` up to `5 juz/day`

The default pace setup should be cycle-based:

- user chooses when they want to finish one full revision cycle
- app converts that target into a daily equivalent using real `rub al-hizb` coverage
- app rounds up to the next practical pace milestone
- app still shows the resolved daily equivalent clearly
- manual pace is a separate override mode: once selected, it replaces the cycle picker in the visible step UI until the user switches back

Manual pace remains available as a secondary override.

Missed work rolls over to the next day rather than being dropped.

## Core User Flows

### Individual User

1. Create a local profile.
2. Select memorized portions of the Quran.
3. Choose a revision pace.
4. Receive a daily task list made of smaller execution units, not raw pool labels.
5. Use the daily review tabs to switch between the dated ward view and `كامل المحفوظ`.
6. Mark items or sub-items as done.
7. Track completion rate over time.

### Multi-Learner Management

1. Create profiles for multiple learners on the same device.
2. Configure each learner profile separately.
3. Enable or disable notifications per profile.
4. Mark revision as completed for each learner.
5. Optionally sign in and sync data.

### Shared Learner Profile

Signed-in users should be able to share a learner profile with more than one manager so each person can update schedule state and completed tasks across devices.

## Notifications

The MVP notification strategy is intentionally simple:

- daily reminder notifications
- motivational messages
- per-profile notification controls in settings
- notifications may include the relevant profile name when multiple profiles exist on one device
- notifications should start as local on-device reminders before any auth or cloud-sync work
- the app should use one app-wide reminder time with per-profile on/off controls
- the settings page should use one muted intro line, one reminders section, and one accounts section
- the self profile should default to reminders on once it has a schedule, while additional learner profiles default to off
- missed work should be reflected in the next day's reminder instead of causing repeated same-day nudges

## Repository Intent

This repository will hold:

- the Android app source
- product documentation
- planning artifacts for MVP delivery

## Current App Artifacts

- Mermaid MVP user flow: [docs/mvp-user-flow.md](/Users/mahdi/personal-repos/tathbeet/docs/mvp-user-flow.md)
- Screen inventory: [docs/screen-list.md](/Users/mahdi/personal-repos/tathbeet/docs/screen-list.md)
- Android app entry point: [MainActivity.kt](/Users/mahdi/personal-repos/tathbeet/app/src/main/java/com/quran/tathbeet/MainActivity.kt)

The current app build is:

- high-fidelity
- interactive
- UI-only
- intentionally fake for sync, Firebase, database, and backend behavior
- uses a 3-step schedule wizard
- shows a one-time intro on first app open
- asks for the active profile name on the intro step with a lightweight single input before memorized-pool selection
- reopens the wizard from محفوظ selection after onboarding
- sends users with an existing schedule straight to review on launch
- keeps sign-in and account-mode changes as a later Settings action in the prototype
- uses Arabic-only user-facing copy
- renders in RTL for Arabic layout review even on non-Arabic devices
- moves memorized-pool selection into a standalone screen
- uses swipeable tabs in the memorized-pool selector with a fixed top summary area
- loads the selector data from the Quran assets under `app/src/main/assets/quran`
- shows real lists for all four tabs: surahs, juzs, hizbs, and rub al-hizb
- generates fake review tasks from those real selections so the prototype feels closer to the final product
- keeps exact task start/end ayah references so review rows can open Quran for Android directly or fall back to `quran.com`
- appends the active profile name into the review top bar title so the current learner is always clear on shared devices
- shows the review screen as a two-tab pager with the current dated ward on the right and `كامل المحفوظ` on the left
- keeps the `كامل المحفوظ` tab on the same underlying task state so completion and rating changes reflect in the dated ward immediately
- adds a review top bar sort action for `كامل المحفوظ` with rating, last-memorized, and Quran-order sorting
- appends completed review status directly into the task title text flow for finished rows, with an inline completed icon immediately after the text
- reveals future review days inline as the current visible work is completed
- offers an explicit restart action when the visible cycle is finished
- keeps strings in Android XML resources instead of hardcoded Kotlin literals

Prototype review is part of product definition. If prototype feedback changes scope, flow, or UX expectations, update `README.md` and `PRD.md` before moving into implementation.

To build the app locally:

```bash
ANDROID_HOME=$HOME/Library/Android/sdk ANDROID_SDK_ROOT=$HOME/Library/Android/sdk ./gradlew assembleDebug
```

## Screenshot Testing

The app uses Android's Compose Preview Screenshot Testing for host-side golden screenshots.

- screenshot test sources live under [app/src/screenshotTest](/Users/mahdi/personal-repos/tathbeet/app/src/screenshotTest)
- generated reference images live under [app/src/screenshotTestDebug/reference](/Users/mahdi/personal-repos/tathbeet/app/src/screenshotTestDebug/reference)

Common commands:

```bash
./gradlew updateDebugScreenshotTest
./gradlew validateDebugScreenshotTest
```

The current baseline follows a component-first strategy.

See [PRD.md](/Users/mahdi/personal-repos/tathbeet/PRD.md) for the detailed product requirements.
