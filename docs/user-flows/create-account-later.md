# Create Account Later

```mermaid
flowchart TD
    A["Use app without signing in"] --> B["Open settings later"]
    B --> C["Change account mode from guest to signed-in"]
    C --> D["Preserve local profiles and schedules"]
    D --> E["Enable sync-ready shared profile state"]
    E --> F["Continue managing the same learner profiles"]
```

Notes:
- Guest usage is valid in MVP.
- Sign-in should enhance the product, not block first use.
- The current prototype simulates sign-in locally from Settings rather than a toolbar action.
