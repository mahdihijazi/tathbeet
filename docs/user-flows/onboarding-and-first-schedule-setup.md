# Onboarding And First Schedule Setup

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
