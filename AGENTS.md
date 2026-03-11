# Repository Guidelines

## Project Structure & Module Organization
`tathbeet` is a single-module Android app built with Gradle Kotlin DSL. The app code lives in `app/src/main/java/com/quran/tathbeet`.

Current repo structure:

- `app/` for the app container, navigation wiring, and ViewModel factories
- `core/` for cross-cutting abstractions such as time
- `data/local/` for Room entities, DAOs, and database setup
- `data/repository/` for repository implementations
- `domain/model/` for domain models exposed to the app layer
- `domain/planner/` for revision-planning logic
- `quran/` for Quran catalog/reference loading
- `ui/features/` for feature-specific screens and composables such as profiles, schedule, review, progress, shared-profile, and settings
- `ui/components/` for reusable app-level UI building blocks and shell components
- `ui/model/` for transitional app state models and Quran selection models still used by the current app skeleton
- `ui/theme/` for colors, typography, and theme setup
- `app/src/androidTest/test/` for shared UI test infrastructure such as base test classes and helpers

Android resources are in `app/src/main/res`, and user-facing strings should live in XML resources rather than Kotlin files.

Product and planning documents live at the repo root in `README.md` and `PRD.md`, with supporting flow and screen docs under `docs/`.

Testing should primarily live in `app/src/androidTest/...` using black-box UI and instrumentation coverage. Add tests by feature or user flow so they mirror the app structure from the user's perspective.

Follow TDD for feature implementation:

- write or update the failing test first
- implement the smallest slice needed to make it pass
- do not move to adjacent feature work until the relevant test is green

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

Code files must never go beyond 400 lines. This is a hard limit. If a file crosses that limit, refactor it immediately into smaller, modular pieces instead of adding more code to the oversized file. Exception: build files and configuration files may exceed this when needed for setup, reporting, or task wiring, but keep them as small and organized as practical.

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

Any new navigation flow built with Navigation Compose must have black-box UI coverage for:

- the happy path
- back navigation
- reopen or edit paths when the flow can be revisited

Shared instrumentation setup must live in a base test class or dedicated helper package. Do not duplicate in-memory Room, fake time providers, or app-container setup across multiple UI test files.

Default test commands:

- `./gradlew testDebugUnitTest` to run local JVM unit tests
- `./gradlew connectedDebugAndroidTest` to run instrumentation and black-box UI tests on a connected emulator or device
- `./gradlew createDebugUnitTestCoverageReport` or `./gradlew jacocoDebugUnitTestReport` to generate debug unit-test coverage
- `./gradlew createDebugAndroidTestCoverageReport` or `./gradlew jacocoDebugUiTestReport` to generate debug instrumentation/UI coverage
- `./gradlew jacocoDebugCombinedReport` to merge debug unit-test and instrumentation/UI coverage into one JaCoCo report
- `./gradlew :app:reportScreenshotCoverage` to generate the screenshot component/state coverage HTML report

Coverage notes:

- debug coverage is enabled in `app/build.gradle.kts` through `enableUnitTestCoverage` and `enableAndroidTestCoverage`
- UI coverage requires a connected emulator or device; unit coverage does not
- the combined report is only meaningful after both unit tests and instrumentation tests have run successfully
- generated HTML and XML coverage reports live under `app/build/reports/jacoco/`
- if the app does not compile, coverage numbers cannot be produced; fix compilation first, then rerun the relevant coverage task

Use Compose Preview Screenshot Testing for visual regression coverage of important screens and UI states. The standard screenshot workflow is:

- aim for 100% screenshot coverage of screen components
- do not capture the whole screen by default
- break each screen into smaller UI elements that form one meaningful component with one clear purpose, then create screenshot tests for those components
- if a component has multiple variants or changes its UI based on state, capture one screenshot per state or variant
- avoid duplicating screenshot coverage when a reusable component is already covered elsewhere; add new coverage only for missing components or missing states

- `./gradlew updateDebugScreenshotTest` to generate or refresh reference images
- `./gradlew validateDebugScreenshotTest` to compare current renders against the saved references
- `./gradlew :app:reportScreenshotCoverage` to generate the screenshot coverage HTML report

## Commit & Pull Request Guidelines
Each commit must have a clear title that explains what the commit does.

Each commit message must also include a body. Keep the body to 4 lines or fewer. If more explanation seems necessary, ask first. Use everyday language and keep it easy to understand.

Never create a commit, push, or use `git commit --amend` unless the user explicitly asks for that action.

Pull requests should describe the user-facing change, note any Gradle commands run, link the relevant issue or planning doc, and include screenshots for Compose UI changes. Keep PRs focused so review stays tied to one feature or fix.
