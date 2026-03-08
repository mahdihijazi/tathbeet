# Tathbeet | تثبيت

Tathbeet is an Android app for Quran revision (`مراجعة حفظ القرآن الكريم`) that helps users keep what they have memorized active through simple, repeatable review schedules.

The core idea is:

- users choose what they have memorized from the Quran
- they set a revision pace, such as `0.5`, `1`, `2`, or `3` juz per day
- the app builds a rotating daily review plan
- the app sends reminders and motivational notifications
- the app works offline for core review and schedule tracking
- the app supports multiple local profiles, such as a parent and children

## Product Direction

Tathbeet is designed for two main use cases:

- an individual who wants to protect and maintain their memorization
- a parent managing revision schedules for children on the same device

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
- completion-rate tracking
- profile-level notification settings
- cloud sync for signed-in users
- shared child profiles so both parents can update schedule and completion state

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

Internally, the scheduler should normalize selections into review segments that are small enough for flexible rotation while still respecting surah boundaries:

- standard unit: `rub al-hizb`
- boundary exception: if a rub crosses outside a selected surah, the app keeps the smaller surah-specific segment instead of forcing the whole rub

This allows the app to support practical pacing such as:

- `0.5 juz/day`
- `1 juz/day`
- `2 juz/day`
- `3 juz/day`

Missed work rolls over to the next day rather than being dropped.

## Core User Flows

### Individual User

1. Create a local profile.
2. Select memorized portions of the Quran.
3. Choose a revision pace.
4. Receive a daily task list.
5. Mark items or sub-items as done.
6. Track completion rate over time.

### Parent Managing Children

1. Create profiles for children on the same device.
2. Configure each child profile separately.
3. Enable or disable notifications per profile.
4. Mark revision as completed for each child.
5. Optionally sign in and sync data.

### Shared Child Profile

Signed-in users should be able to share a child profile with both father and mother so both can update schedule state and completed tasks across devices.

## Notifications

The MVP notification strategy is intentionally simple:

- daily reminder notifications
- motivational messages
- per-profile notification controls in settings
- notifications may include the relevant profile name when multiple profiles exist on one device

## Repository Intent

This repository will hold:

- the Android app source
- product documentation
- planning artifacts for MVP delivery

## Prototype Artifacts

- Mermaid MVP user flow: [docs/mvp-user-flow.md](/Users/mahdi/personal-repos/tathbeet/docs/mvp-user-flow.md)
- Screen inventory: [docs/screen-list.md](/Users/mahdi/personal-repos/tathbeet/docs/screen-list.md)
- Android prototype entry point: [MainActivity.kt](/Users/mahdi/personal-repos/tathbeet/app/src/main/java/com/quran/tathbeet/MainActivity.kt)

The current prototype is:

- high-fidelity
- interactive
- UI-only
- intentionally fake for sync, Firebase, database, and backend behavior
- uses a 3-step schedule wizard
- shows a one-time intro on first app open
- reopens the wizard from محفوظ selection after onboarding
- keeps account creation as a later toolbar action in the prototype
- uses Arabic-only user-facing copy
- renders in RTL for Arabic layout review even on non-Arabic devices
- moves memorized-pool selection into a standalone screen
- uses swipeable tabs in the memorized-pool selector with a fixed top summary area
- loads the selector data from the Quran assets under `app/src/main/assets/quran`
- shows real lists for all four tabs: surahs, juzs, hizbs, and rub al-hizb
- generates fake review tasks from those real selections so the prototype feels closer to the final product
- keeps strings in Android XML resources instead of hardcoded Kotlin literals

Prototype review is part of product definition. If prototype feedback changes scope, flow, or UX expectations, update `README.md` and `PRD.md` before moving into implementation.

To build the prototype locally:

```bash
ANDROID_HOME=$HOME/Library/Android/sdk ANDROID_SDK_ROOT=$HOME/Library/Android/sdk ./gradlew assembleDebug
```

See [PRD.md](/Users/mahdi/personal-repos/tathbeet/PRD.md) for the detailed product requirements.
