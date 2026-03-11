# Add And Manage Profiles

```mermaid
flowchart TD
    A["Open profiles"] --> B["View all local profiles"]
    B --> C["Switch active profile"]
    B --> D["Add learner profile inline"]
    C --> E["Open review, progress, or settings for the active profile"]
    D --> F["Make the new learner profile active"]
    F --> G["Open schedule setup for that profile"]
    F --> H["Open sharing controls for learner profiles"]
```

Notes:
- Profiles should support self and additional learners on one device.
- This flow should work offline.
- The current prototype keeps profile creation inline from the Profiles screen.
- Edit and delete profile flows remain PRD requirements, but they are not yet represented as dedicated prototype screens.
