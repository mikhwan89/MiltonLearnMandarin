# Milton Learn Mandarin — Architecture Roadmap

The goal of this roadmap is to make the codebase manageable for small, targeted changes — whether made by a human or an AI assistant. Each item is designed so that a single focused task touches as few files as possible and carries a low risk of breaking unrelated things.

---

## 1. Scenarios as JSON Assets ✅ DONE

**Why:** The single biggest manageability win. Currently adding or editing a scenario requires writing/editing Kotlin files, which risks compilation errors and requires understanding the full data model. Moving content to JSON means:
- A new scenario = one new JSON file in `assets/scenarios/`, no Kotlin changes
- Content edits (fix a translation, add a quiz question) are isolated to one JSON file
- Claude or a human can write a new scenario as a JSON blob without touching logic code
- A bad edit corrupts one file, not the whole build

**What to do:**
- Define a JSON schema mirroring the existing data model (`Scenario`, `DialogueStep`, `QuizQuestion`, etc.)
- Write one JSON file per scenario in `app/src/main/assets/scenarios/`
- Write a `JsonScenarioLoader` that reads and deserializes these files using `kotlinx.serialization` (already in the Kotlin ecosystem, no extra dependency needed beyond the plugin)
- Update `ScenarioRepository` to call `JsonScenarioLoader` instead of importing hardcoded functions
- Delete `Scenario1_*.kt` through `Scenario16_*.kt` once all scenarios are ported to JSON

**Files affected:** `ScenarioRepository.kt`, new `JsonScenarioLoader.kt`, new `assets/scenarios/*.json`. Screens are untouched.

---

## 2. Split MainActivity.kt ✅ DONE

**Why:** `MainActivity.kt` currently contains four unrelated things: the `ComponentActivity` setup, the `Screen` sealed class, the top-level `MandarinKidsApp` navigation composable, and the entire `HomeScreen` + `ScenarioCard` UI. Any change to navigation or the home screen requires opening this one large file, increasing the chance of accidental edits to unrelated code.

**What to do:**
- `navigation/Screen.kt` — move the `Screen` sealed class here
- `navigation/AppNavigation.kt` — move the `when (currentScreen)` composable routing here
- `home/HomeScreen.kt` — move `HomeScreen` and `ScenarioCard` here
- `MainActivity.kt` — keep only `ComponentActivity`, `setContent {}`, and the call to `AppNavigation`

**Files affected:** Only splitting one file into four. All composable signatures stay the same.

---

## 3. Centralize TTS into a Shared Manager ✅ DONE

**Why:** TTS initialization (`LaunchedEffect`, `DisposableEffect`, language setup) is copy-pasted across `RolePlayScreen.kt`, `QuizScreen.kt`, and `PhrasesScreen.kt`. Any TTS change (add a language, change speech rate default, handle initialization failure) must be made in three places. One missed file = inconsistent behavior.

**What to do:**
- Create `tts/TtsManager.kt` — a class that wraps `TextToSpeech`, handles init/shutdown, and exposes a `speak(text, locale, rate)` function
- Expose it as a `@Composable` via `rememberTtsManager()` so lifecycle is still tied to composition
- Replace the three copies of TTS boilerplate with a single `val tts = rememberTtsManager()`

**Files affected:** New `TtsManager.kt`, minor edits to three screen files to replace boilerplate.

---

## 4. ViewModel for RolePlayScreen ✅ DONE

**Why:** `RolePlayScreen.kt` manages all its state via `remember`/`mutableStateOf` directly in the composable. This means state logic and UI rendering are interleaved, making it hard to change one without risking the other. `QuizScreen` already has a `QuizViewModel` — applying the same pattern to `RolePlayScreen` makes the codebase consistent and predictable.

**What to do:**
- Create `RolePlayViewModel.kt` modeled after `QuizViewModel.kt`
- Move all `var`/`remember` state (current step index, score, showHint, etc.) into the ViewModel
- `RolePlayScreen` composable becomes a pure rendering function that reads from `vm.*` and calls `vm.*()` on user actions

**Files affected:** New `RolePlayViewModel.kt`, `RolePlayScreen.kt` (state wiring only — UI layout unchanged).

---

## 5. Repository Interface + Abstraction ✅ DONE

**Why:** `ScenarioRepository` currently returns a hardcoded list. Screens call it directly with no abstraction. Once scenarios load from JSON (item 1), the repository will do real I/O. Defining an interface means the source (JSON assets, Room cache, future remote API) can be swapped without touching any screen or ViewModel.

**What to do:**
- Define `interface ScenarioRepository` with a single method `fun getAll(): List<Scenario>`
- Rename the current implementation to `JsonScenarioRepository : ScenarioRepository`
- ViewModels accept `ScenarioRepository` (the interface), not the concrete class
- This sets up clean testing: a `FakeScenarioRepository` can inject test data without touching assets

**Files affected:** Rename/restructure `ScenarioRepository.kt`, minor ViewModel constructor changes.

---

## 6. Room Database for Progress Persistence ✅ DONE

**Why:** `ProgressManager` currently reads/writes `SharedPreferences` directly inside composables and the results screen. This makes it hard to query progress in new ways (e.g. "show all scenarios where stars < 3") without rewriting string key logic. Room gives typed, queryable persistence.

**What to do:**
- Add Room dependency to `build.gradle`
- Define `ScenarioProgressEntity` (scenarioId, stars, xp, lastPlayedAt)
- Define `ProgressDao` with `upsert`, `getById`, `getAll`
- Define `AppDatabase`
- Replace `ProgressManager` SharedPreferences calls with `ProgressDao` calls via a `ProgressRepository`

**Files affected:** New `db/` package with 4 files, `ProgressManager.kt` replaced, `QuizResultsScreen` updated to call `ProgressRepository`.

---

## 7. DataStore for User Preferences ✅ DONE

**Why:** Any user-facing settings (TTS speech rate, display language toggle, slow-mode default) currently have no persistent storage — they reset on every launch. DataStore provides type-safe, async key-value storage that integrates cleanly with Compose via `collectAsState`.

**What to do:**
- Add DataStore dependency
- Define `UserPreferencesRepository` with typed fields: `speechRate: Float`, `showIndonesian: Boolean`
- Expose preferences as `StateFlow` so composables observe them reactively
- Wire the speech rate toggle in `RolePlayScreen` to persist via `UserPreferencesRepository`

**Files affected:** New `preferences/UserPreferencesRepository.kt`, `RolePlayScreen.kt` (wire toggle to persist).

---

## 8. Jetpack Navigation (LOW PRIORITY)

**Why:** The manual `Screen` sealed class + `currentScreen` state in `MandarinKidsApp` works today, but adding a new screen requires editing `AppNavigation.kt` (the routing `when` block) and passing lambdas through multiple levels. Jetpack Navigation gives each screen its own route and removes the need for deeply threaded `onNavigate` callbacks.

**What to do:**
- Add `androidx.navigation:navigation-compose` dependency
- Define a `Routes` object with string constants for each screen
- Replace the `when (currentScreen)` block with a `NavHost`
- Each screen navigates by calling `navController.navigate(Routes.X)` — no lambda threading

**Files affected:** `AppNavigation.kt` (rewrite routing), minor edits to each screen to accept `navController` instead of `onNavigate` lambdas.

---

## 9. Unit Tests for Core Logic (LOW PRIORITY)

**Why:** There are currently no unit tests. This means any refactor has no safety net. The highest-value tests to write are the ones that catch data/logic bugs before they reach the UI.

**Priority test targets:**
- `JsonScenarioLoader` — assert all scenario JSON files parse without error and have required fields
- `QuizViewModel` — assert score increments correctly, `advanceQuestion` transitions state correctly
- `RolePlayViewModel` — assert step progression and score tracking
- `ProgressRepository` — assert star calculation logic (`calculateStars`)

**Files affected:** New files under `src/test/` only. No production code changes.

---

## Suggested Implementation Order

| # | Item | Effort | Impact |
|---|------|--------|--------|
| 1 | ~~Scenarios as JSON assets~~ ✅ | Large | Highest — unlocks content changes with zero Kotlin |
| 2 | ~~Split MainActivity.kt~~ ✅ | Small | High — reduces blast radius of any nav/home change |
| 3 | ~~Centralize TTS~~ ✅ | Small | High — eliminates 3-way duplication |
| 4 | ~~RolePlayViewModel~~ ✅ | Medium | Medium — consistency with QuizViewModel pattern |
| 5 | ~~Repository interface~~ ✅ | Small | Medium — prerequisite for clean testing |
| 6 | ~~Room for progress~~ ✅ | Medium | Medium — enables richer progress queries |
| 7 | ~~DataStore preferences~~ ✅ | Small | Medium — persists user settings |
| 8 | Jetpack Navigation | Medium | Low — nice to have once screen count grows |
| 9 | Unit tests | Medium | Low now, high long-term |

---

## Feature: Flashcard Practice Mode

**What:** A dedicated flashcard training screen accessible from the Home screen via a flashcard icon button. It pools together every flashcard from every scenario that the user has previously marked "Got it" and lets Milton drill them in a focused, game-like session — separate from going through a full scenario.

**Behaviour:**
- Home screen gets a flashcard icon button (e.g. top-right corner or its own card). Tapping it goes straight to this mode without picking a scenario.
- The deck is built from all `PinyinWord` entries across all scenarios where the user's saved state includes a "got it" mark for that word.
- Session flow: show Chinese character → tap to flip → reveal pinyin + English + Indonesian + TTS plays automatically → user marks "Remember it" or "Forgot it".
- "Forgot it" cards re-enter the queue for the current session; "Remember it" cards are retired for the session.
- End-of-session summary: X cards reviewed, X remembered, X to keep practising.
- If no cards are marked "got it" yet, show a friendly prompt: "Complete some scenario flashcards first to fill your practice deck!"

**Data needed:**
- Persist per-word "got it" status in the same storage used by the scenario flashcard phase (SharedPreferences key or Room row). The practice mode just reads the same flags — no new data structure needed.

**Files to create/edit:**
- New `FlashcardPracticeScreen.kt`
- `HomeScreen.kt` — add the icon button
- `navigation/Screen.kt` — add `FlashcardPractice` object
- `navigation/AppNavigation.kt` — add the route

---

## Feature: Scenario Categories on Home Screen

**What:** Group the scenario cards on the Home screen under category headers instead of a flat list. Makes it easier to find relevant scenarios as the library grows beyond 16.

**Proposed categories (6 total):**

| Category | Scenarios it covers |
|----------|-------------------|
| **Essentials** | Hello/goodbye to teacher, meeting peers, introducing yourself, asking name/age, first day of school intro, end-of-day farewells, resolving a disagreement |
| **At School** | Asking to use the bathroom, borrowing a pencil/eraser, PE class instructions, getting lost on campus |
| **School Subjects** | Being in maths class, science class, Chinese language class, etc. — student learning from a teacher, answering questions, asking for help understanding, describing what they are studying |
| **Food & Eating** | Snack time, sharing food, school canteen lunch, saying what you like/don't like |
| **Feelings & Health** | Describing emotions, telling the teacher you feel unwell, stomachache/headache vocab, expressing happiness/sadness/excitement |
| **Play & Hobbies** | Playground, sports, talking about hobbies, inviting a friend to play |

**Rules for future scenarios:** every new scenario must be assigned exactly one category. If a scenario fits two, pick the most dominant theme.

**UI behaviour:**
- Home screen renders one section header per category, then the scenario cards beneath it.
- Categories with no scenarios yet are hidden (not shown as an empty section).
- Category headers can be tapped to collapse/expand the section (optional — implement later).

**Data changes needed:**
- Add a `category: ScenarioCategory` field to the `Scenario` data class.
- Define a `ScenarioCategory` enum with the 6 values above: `ESSENTIALS`, `AT_SCHOOL`, `SCHOOL_SUBJECTS`, `FOOD_AND_EATING`, `FEELINGS_AND_HEALTH`, `PLAY_AND_HOBBIES`.
- Assign a category to each existing scenario in their JSON files (once JSON migration is done) or in their Kotlin files (before migration).
- `HomeScreen` groups scenarios by category using `groupBy { it.category }` before rendering.
