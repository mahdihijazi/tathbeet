# Reopening Schedule Setup Later

```mermaid
flowchart TD
    A["Need to edit the revision plan later"] --> B{"Where does the user start?"}
    B -- "Review" --> C["Tap تعديل الخطة in the top app bar"]
    B -- "Profiles" --> D["Open schedule for the active profile"]
    C --> E["Skip intro and open محفوظ selection directly"]
    D --> E
    E --> F["Update the memorized pool"]
    F --> G["Continue to الورد اليومي"]
    G --> H["Adjust target cycle or open the manual pace sheet"]
    H --> I["Save the updated schedule"]
    I --> J["Return to review"]
```

Notes:
- After the first run, setup should always start from محفوظ selection.
- The daily-ward screen should stay simple and focused.
