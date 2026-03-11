# Shared Learner Account

```mermaid
flowchart TD
    A["Open learner profile"] --> B["Open sharing controls"]
    B --> C["Choose which managers can update the learner profile"]
    C --> D{"Signed in?"}
    D -- "No" --> E["Keep the profile in local-only or sync-pending state"]
    D -- "Yes" --> F["Show shared state as sync-ready"]
    E --> G["Continue using the learner profile on this device"]
    F --> H["Sync schedule and task state across devices"]
```

Notes:
- Shared access is optional.
- Shared account wording should stay role-neutral and support family or class use cases.
- The current prototype simulates sharing state and sync feedback locally.
