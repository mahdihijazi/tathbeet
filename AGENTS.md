# Repository Guidelines

## Project Structure & Module Organization
`tathbeet` is a single-module Android app built with Gradle Kotlin DSL. The app code lives in `app/src/main/java/com/quran/tathbeet`.

Current UI structure:

- `ui/features/` contains feature-specific screens and composables such as welcome, profiles, schedule, review, progress, shared-profile, and settings
- `ui/components/` contains reusable app-level UI building blocks and shell components
- `ui/model/` contains app state models, fake interaction state, and Quran selection data used by the current app skeleton
- `ui/theme/` contains colors, typography, and theme setup

Android resources are in `app/src/main/res`, and user-facing strings should live in XML resources rather than Kotlin files.

Product and planning documents live at the repo root in `README.md` and `PRD.md`, with supporting flow and screen docs under `docs/`.

Testing should primarily live in `app/src/androidTest/...` using black-box UI and instrumentation coverage. Add tests by feature or user flow so they mirror the app structure from the user's perspective.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root:

- `./gradlew assembleDebug` builds the local debug APK.
- `./gradlew installDebug` installs the debug build on a connected device or emulator.
- `./gradlew testDebugUnitTest` runs local JVM unit tests.
- `./gradlew connectedDebugAndroidTest` runs device/emulator instrumentation tests.
- `./gradlew lint` runs Android lint checks.
- `./gradlew clean` removes build outputs.

If your SDK is not auto-detected, export `ANDROID_HOME` and `ANDROID_SDK_ROOT` before running Gradle.

## Coding Style & Naming Conventions
Follow Kotlin and Android Studio defaults: 4-space indentation, organized imports, and trailing commas where the codebase already uses them. Use `PascalCase` for classes and composables (`TathbeetApp`), `camelCase` for functions and properties, and keep package names under `com.quran.tathbeet`.

Prefer small composables, clear state names, and RTL-aware UI decisions because the product is Arabic-first. Keep prototype screens and future feature UI grouped by domain instead of growing `MainActivity.kt`.

Code files must never go beyond 400 lines. This is a hard limit. If a file crosses that limit, refactor it immediately into smaller, modular pieces instead of adding more code to the oversized file.

## Localization
Do not hardcode user-facing strings in Kotlin code, even for prototypes. Put all user-facing app strings in Android XML resource files such as `strings.xml` and reference them from code.

All layouts must be RTL-friendly. Build and review screens as Arabic-first layouts rather than treating RTL as a later adaptation.

## Theming
All colors must come from the app theme. No composable UI code should reference raw color values directly, and no composable should depend on a specific hardcoded color.

Theme color names should stay abstract and role-based rather than pigment-based. Do not use names like `red`, `white`, or `blue`. Use names such as `primaryAction`, `secondaryAction`, `negativeAction`, and other semantic roles so the app theme can change without rewriting UI components.

## Prototype Feedback Loop
Treat prototype review as part of product definition, not just UI polish. Any user feedback on the prototype should be reflected back into `PRD.md` and `README.md` when it changes scope, flow, UX expectations, or product wording. The goal is to fully polish the PRD before the implementation phase starts.

## Testing Guidelines
Prefer black-box UI testing from the user's perspective over unit tests. Skip unit tests by default unless there is a very strong reason to add one. Focus on Android UI and instrumentation tests that validate complete feature behavior, flows, and visible outcomes through the app surface rather than internal implementation details.

Use `AndroidJUnitRunner` for instrumentation coverage. Name UI test files clearly by feature or flow, and prioritize end-to-end user scenarios such as onboarding, profile switching, schedule setup, daily review completion, shared-profile interactions, and notification/settings behavior.

## Commit & Pull Request Guidelines
Each commit must have a clear title that explains what the commit does.

Each commit message must also include a body. Keep the body to 4 lines or fewer. If more explanation seems necessary, ask first. Use everyday language and keep it easy to understand.

Pull requests should describe the user-facing change, note any Gradle commands run, link the relevant issue or planning doc, and include screenshots for Compose UI changes. Keep PRs focused so review stays tied to one feature or fix.
