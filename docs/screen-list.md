# Tathbeet MVP Screen List

## 1. Schedule Builder

- is now a wizard instead of a single screen
- shows a one-time intro on first app open
- starts from محفوظ selection on later visits
- ends with a daily-ward screen that defaults to cycle-target setup
- keeps only a lightweight preview before saving
- should remain visually focused and avoid noisy supporting blocks that do not help schedule setup

## 2. Profiles Home

- shows all local profiles on the device
- lets the user switch between self and learner profiles
- highlights active schedule status, today’s load, and notification state

## 3. Create / Edit Profile

- create a new self or learner profile
- define profile name and sharing state
- later connect a guest profile to a signed-in account

## 4. Memorized Pool Selector

- standalone screen dedicated to pool selection
- contains separate swipeable tabs for `surah`, `juz`, `hizb`, and `rub al-hizb`
- keeps the title, current selection card, and tabs fixed at the top
- only the list below the tabs scrolls vertically
- lets the user select from any tab, add to the pool, and continue to the daily-ward step

## 5. Schedule Intro

- one-time onboarding step for the setup wizard
- explains what the schedule wizard does
- centered body copy with a next button

## 6. Daily Ward

- second functional step after محفوظ selection
- defaults to choosing when the user wants to finish one full revision cycle
- supports presets like `1 week`, `2 weeks`, `1 month`, `45 days`, and `2 months`
- shows the resolved daily equivalent after conversion
- offers manual pace as a secondary bottom-sheet flow
- shows a lightweight preview before saving
- saving takes the user directly to today’s review

## 7. Daily Review

- shows today’s assigned review segments
- shows rollover items first when previous work was missed
- lets the user mark individual segments complete
- marks the day complete when all assigned segments are done

## 8. Progress

- shows completion rate
- keeps analytics intentionally lightweight in MVP

## 9. Shared Learner Profile

- shows who can manage the learner profile
- supports multiple managers updating the same schedule/task state
- communicates sync status and recent shared changes

## 10. Settings

- global and per-profile notification controls
- reminder timing
- motivational message toggle
- language and account state
- account creation can be reached later from the toolbar instead of blocking first launch

## Current App Mapping

The high-fidelity interactive Compose app currently includes:

- Schedule
- Memorized Pool Selector
- Profiles
- Daily Review
- Shared Profile
- Progress
- Settings

Current app behavior:

- all interactions are fake and local to the UI
- first launch opens the one-time schedule intro screen
- later schedule entry opens the محفوظ selection step directly
- the prototype uses Arabic-only user-facing copy
- the prototype is reviewed in RTL layout
- user-facing strings come from Android XML resources
- profile switching is wired
- the schedule wizard is wired across intro, محفوظ selection, and daily ward
- daily review completion is wired
- settings toggles are wired
- shared-profile controls are wired

The dedicated `Create / Edit Profile` flow is still represented as an in-place prototype action from the Profiles screen and should become its own implementation screen when real navigation and persistence are added.
