# Create Account Later

```mermaid
flowchart TD
    A["Use app without signing in"] --> B["Open settings later"]
    B --> C["Start email-link sign-in"]
    C --> D["Return to the app as a signed-in adult"]
    D --> E["Enable cloud sync for your own profile or join a shared learner profile"]
    E --> F["Keep local reminder settings on this device"]
```

Notes:
- Guest usage is valid in MVP.
- Sign-in should unlock cloud sync and shared learner profiles without blocking first use.
- The current prototype simulates sign-in locally from Settings rather than a toolbar action.
