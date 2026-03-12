# Daily Ward And Review

```mermaid
flowchart TD
    A["Open review"] --> B["See rollover and today's assigned execution units"]
    B --> C{"Finish every visible unit?"}
    C -- "No" --> D["Keep unfinished work for rollover"]
    C -- "Yes" --> E["Append the next future-dated day inline"]
    E --> F{"Reached the end of the current cycle?"}
    F -- "No" --> B
    F -- "Yes" --> G["Offer explicit restart-cycle action"]
    G --> H["Start the next cycle from the beginning"]
    D --> I["Leave remaining work for the next day"]
```

Notes:
- Review should stay structurally simple even when future days are appended inline.
- The active local profile should be visible in the review top bar title.
- The review screen should also expose plan editing from the top app bar.
- Missed work rolls over instead of being dropped.
