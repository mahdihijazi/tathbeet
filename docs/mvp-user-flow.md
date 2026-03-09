# Tathbeet MVP User Flows

This document splits the MVP into separate user journeys so each flow stays focused and reviewable.

## 1. Onboarding And First Schedule Setup

```mermaid
flowchart TD
    A["Open Tathbeet for the first time"] --> B["Show one-time intro screen"]
    B --> C["Open محفوظ selection"]
    C --> D["Browse tabs:<br/>surah / juz / hizb / rub al-hizb"]
    D --> E["Select memorized items"]
    E --> F["Continue to الورد اليومي"]
    F --> G["Choose target cycle:<br/>1 week / 2 weeks / 1 month / 45 days / 2 months"]
    G --> H["Show resolved daily equivalent"]
    H --> I["Optional manual pace bottom sheet"]
    I --> J["Review lightweight preview"]
    J --> K["Save schedule"]
    K --> L["Open today's review directly"]
```

Notes:
- The intro screen appears only once.
- Account creation should not block this flow.
- The pool should display what the user selected, while overlap handling stays internal to the scheduler.
- The pace step should default to cycle-based setup rather than manual daily-unit selection.

## 2. Reopening Schedule Setup Later

```mermaid
flowchart TD
    A["Open schedule setup again"] --> B["Skip intro"]
    B --> C["Open محفوظ selection directly"]
    C --> D["Update memorized pool"]
    D --> E["Continue to الورد اليومي"]
    E --> F["Adjust target cycle or open manual pace sheet"]
    F --> G["Save updated schedule"]
    G --> H["Return to today's review"]
```

Notes:
- After the first run, setup should always start from محفوظ selection.
- The daily-ward screen should stay simple and focused.

## 3. Daily Ward And Review

```mermaid
flowchart TD
    A["Open today's review"] --> B["See assigned review units"]
    B --> C{"Finish all assigned units?"}
    C -- "No" --> D["Keep unfinished units for rollover"]
    C -- "Yes" --> E["Mark day complete"]
    D --> F["Show next reminder state"]
    E --> F["Show next reminder state"]
```

Notes:
- Review is intentionally simple in MVP.
- The engine should count partial overlap once when building effective coverage.
- Missed work rolls over instead of being dropped.

## 4. Add And Manage Profiles

```mermaid
flowchart TD
    A["Open profiles"] --> B["View all local profiles"]
    B --> C["Add learner profile"]
    B --> D["Switch active profile"]
    C --> E["Configure profile locally"]
    E --> F["Create schedule for that profile"]
    D --> G["Open review, progress, or settings"]
```

Notes:
- Profiles should support self and additional learners.
- This flow should work offline.

## 5. Shared Learner Profile

```mermaid
flowchart TD
    A["Open shared profile"] --> B{"Signed in?"}
    B -- "No" --> C["Keep profile local only"]
    B -- "Yes" --> D["Enable shared learner profile"]
    D --> E["Allow more than one manager"]
    E --> F["Sync schedule and task state across devices"]
```

Notes:
- Shared access is optional.
- Shared profile wording should stay role-neutral and support family or class use cases.

## 6. Create Account Later

```mermaid
flowchart TD
    A["Use app as guest"] --> B["Open toolbar account action later"]
    B --> C["Sign in with Google"]
    C --> D["Preserve local data"]
    D --> E["Sync profiles and schedules"]
    E --> F["Unlock shared profile support"]
```

Notes:
- Guest usage is valid in MVP.
- Sign-in should enhance the product, not block first use.

## Flow Rules

- The prototype should use Arabic-only visible copy.
- The prototype should render in RTL even on non-Arabic devices.
- User-facing strings should live in Android XML resources.
- The محفوظ selection screen should keep its top area fixed and scroll only the list below.
- Category switching in محفوظ selection should support both tab taps and horizontal swiping.
- The pool keeps exact user selections.
- Full containment should be resolved internally by keeping the larger effective coverage.
- Partial overlap should be resolved internally by counting shared coverage once.
- The pace step should default to target cycle presets and keep manual pace in a secondary bottom sheet.
- Manual pace and target-cycle pace should behave as two exclusive step states, not two visible sections at once.
