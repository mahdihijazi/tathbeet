# Tathbeet MVP User Flow

This flow focuses on the Android MVP that you asked for: offline-first review, optional Google sign-in, multiple profiles, shared child profiles, and an Arabic-first RTL prototype.

```mermaid
flowchart TD
    A["Open Tathbeet"] --> B{"First app open?"}
    B -- "Yes" --> C["Show one-time wizard intro"]
    B -- "No" --> D["Open محفوظ selection directly"]
    C --> D
    D --> E["Switch between tabs:<br/>surah / juz / hizb / rub al-hizb"]
    E --> F["Add selected items to the pool"]
    F --> G["Move to daily ward screen"]
    G --> H["Choose daily pace<br/>0.5 / 1 / 2 / 3 juz per day"]
    H --> I["Show lightweight preview<br/>pool size + cycle length"]
    I --> J["Save active schedule"]
    J --> K["Open today's review directly"]
    K --> L{"Finish all assigned segments?"}
    L -- "No" --> M["Keep unfinished segments in rollover queue"]
    L -- "Yes" --> N["Mark day done"]
    M --> O["Show motivation and next reminder state"]
    N --> O
    O --> P["Open profiles or settings as needed"]
    P --> Q["Optional toolbar action: create account later"]
    P --> R["Optional shared child profile flow"]
    R --> S["Sync child profile across devices"]
```

## Flow Notes

- The first app open shows a one-time wizard intro screen, then moves into the schedule setup flow.
- After that first intro has been seen, reopening the setup flow should start directly on the محفوظ selection screen.
- Account creation should not block the first-run flow; it can be offered later from the toolbar.
- The setup wizard should have three screens: intro once, محفوظ selection, then daily ward.
- Memorized-pool selection should happen in its own dedicated screen, not inside the daily-ward screen.
- The daily-ward screen should stay lightweight and answer only: how large is the pool, and how long is one full rotation.
- The daily-ward screen should stay visually focused and avoid extra blocks that do not help the user finish setup.
- Guests can use the app fully for local profile management and offline revision.
- Signed-in users unlock sync and shared child profile management.
- Daily review is intentionally simple: see due items, mark segments done, and let missed work roll over.
- The prototype should use Arabic-only visible copy and should render in RTL even when tested on a non-Arabic device.
- Prototype strings should live in Android XML resources so copy review and later localization stay manageable.
