# Daily Ward And Review

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
