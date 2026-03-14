# Shared Learner Account

```mermaid
flowchart TD
    A["Owner opens learner profile"] --> B["Open sharing controls"]
    B --> C["Enter another adult's email"]
    C --> D["Create pending invite"]
    D --> E["Other adult signs in with email link"]
    E --> F["Accept invite"]
    F --> G["Join as editor"]
    G --> H["All members sync schedule, cycle, and task updates"]
```

Notes:
- Shared access is optional.
- The original creator is the only owner in MVP.
- Invited adults join as editors and can fully update the revision plan and daily tasks.
- The current prototype simulates sharing state and sync feedback locally.
