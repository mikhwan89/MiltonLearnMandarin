# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Mandarinku** is an Android app that teaches Mandarin to young children (age 4–8) through interactive role-play scenarios, flashcard drills, tone training, and sentence-building games.

**GitHub:** https://github.com/mikhwan89/MiltonLearnMandarin
**Package:** `com.ikhwan.mandarinkids`

## Build Commands

```bash
# Build debug APK (requires Android Studio's JDK — run from Android Studio terminal)
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint check
./gradlew lint

# Clean build
./gradlew clean
```

## Architecture

**UI:** 100% Jetpack Compose with Material Design 3
**State management:** ViewModels (`RolePlayViewModel`, `QuizViewModel`, `FlashcardViewModel`, `PracticeSessionViewModel`) + Compose `collectAsState()`
**Navigation:** Jetpack Navigation Compose (`NavHostController`), string routes defined in `navigation/Routes.kt`
**Data storage:** Room database v8 + DataStore preferences
**Content:** JSON files in `app/src/main/assets/scenarios/` (53 scenarios across 8 categories), loaded at runtime via `JsonScenarioLoader`

### Screen Flow

```
HomeScreen (tab: Roleplay)
  └── ScenarioListScreen (by category)
        └── RolePlayScreen → QuizScreen → QuizResultsScreen
                                               └── retry → QuizScreen

HomeScreen (tab: Practice)
  └── PracticeScreen (cross-scenario flashcard drill)
        └── FlashcardScreen (per-scenario flashcards)

HomeScreen (tab: Tone Trainer)
  └── ToneTrainerScreen

HomeScreen (tab: Sentence Builder)
  └── SentenceBuilderScreen

HomeScreen (tab: Progress)
  └── ProgressScreen
        └── PinScreen → ParentDashboardScreen
```

### Navigation Routes (`navigation/Routes.kt`)

```kotlin
Routes.HOME              // "home"
Routes.PRACTICE          // "practice"
Routes.TONE_TRAINER      // "tone_trainer"
Routes.SENTENCE_BUILDER  // "sentence_builder"
Routes.PROGRESS          // "progress"
Routes.PARENT_DASHBOARD  // "parent_dashboard"
Routes.PIN               // "pin"
Routes.CATEGORY          // "category/{categoryName}"
Routes.FLASHCARD         // "flashcard/{scenarioId}"
Routes.ROLEPLAY          // "roleplay/{scenarioId}"
Routes.QUIZ              // "quiz/{scenarioId}/{rolePlayScore}/{level}"
Routes.SENTENCE_QUIZ     // "sentence_quiz/{scenarioId}/{level}"
```

Navigation is wired in `navigation/AppNavigation.kt`. The bottom nav shows 5 tabs (HOME, PRACTICE, TONE_TRAINER, SENTENCE_BUILDER, PROGRESS) and hides on child routes (PIN, PARENT_DASHBOARD, CATEGORY, FLASHCARD, ROLEPLAY, QUIZ). Individual tabs can be disabled via parental controls.

### Key Source Files

All under `app/src/main/java/com/ikhwan/mandarinkids/`:

#### Screens & ViewModels
| File | Contents |
|------|----------|
| `MainActivity.kt` | `ComponentActivity`, entry point, `MandarinKidsApp` host composable |
| `home/HomeScreen.kt` | Roleplay home — word-of-day popup, category grid, XP/streak display, theme toggle |
| `home/ScenarioListScreen.kt` | Scenario cards filtered by category |
| `home/ProgressScreen.kt` | Progress tab — XP, badges, mastered words, parent dashboard entry, theme toggle |
| `onboarding/InteractiveOnboardingOverlay.kt` | 10-step spotlight tour — `BlendMode.Clear` cutouts, pulsing ring, tooltip bubble, background nav |
| `onboarding/LocalOnboardingCoords.kt` | `LocalOnboardingCoords` CompositionLocal + `OnboardingKey` string constants |
| `RolePlayScreen.kt` | Dialogue flow, `ResponseOptionButton`, `NameInputSection` |
| `RolePlayViewModel.kt` | Step progression, option selection, TTS coordination, score tracking |
| `ConversationBubble.kt` | Chat bubble composable with pinyin pills, bounce animation, word detail dialog |
| `QuizScreen.kt` | Quiz layout, `QuizOptionButton` |
| `QuizViewModel.kt` | Quiz state — question index, answer selection, scoring |
| `QuizResultsScreen.kt` | Stars, XP, confetti, try-again / home CTAs |
| `FeedbackCard.kt` | Correct/wrong feedback with explanation text |
| `FlashcardScreen.kt` | Per-scenario flashcard review |
| `FlashcardViewModel.kt` | Flashcard state — flip, got-it/skip |
| `practice/PracticeScreen.kt` | Cross-scenario flashcard drill mode |
| `practice/PracticeSessionViewModel.kt` | Practice session state |
| `practice/ToneTrainerScreen.kt` | Tone recognition game — hear a word, identify its tone; info dialog on first launch |
| `practice/SentenceBuilderScreen.kt` | Sentence assembly game — arrange pinyin tiles in correct order; SVO info dialog |
| `practice/PracticeMode.kt` | Enum/data class for practice modalities |
| `PhrasesScreen.kt` | Standalone phrase browser with TTS |
| `parent/ParentDashboardScreen.kt` | Parent progress view and milestone rewards manager |
| `parent/PinScreen.kt` | PIN entry gate for parent dashboard |

#### Data & Persistence
| File | Contents |
|------|----------|
| `data/models/ScenarioModels.kt` | All data classes and enums (`Scenario`, `DialogueStep`, `ResponseOption`, `PinyinWord`, `QuizQuestion`, `QuizOption`, `ConversationMessage`, `Speaker`, `ResponseType`, `QuizDirection`, `ScenarioCategory`) |
| `data/scenarios/JsonScenarioLoader.kt` | Loads and deserialises scenario JSON from assets |
| `data/scenarios/JsonScenarioRepository.kt` | In-memory cache of all loaded scenarios |
| `data/scenarios/ScenarioRepository.kt` | Public facade — `getAllScenarios()`, `getById()` |
| `db/AppDatabase.kt` | Room database v8, three entities, migrations 1→8 |
| `db/ScenarioProgressEntity.kt` | Stars, XP, lastPlayedAt, speechRateOverride per scenario |
| `db/MasteredWordEntity.kt` | Per-word progress with spaced-repetition fields (`boxLevel`, `nextReviewDate`) and `practiceType` |
| `db/PracticeType.kt` | Enum: `DEFAULT`, `LISTENING`, `READING` — tracks mastery separately per modality |
| `db/MilestoneReward.kt` | Parent-defined reward targets (multi-condition: `conditionsJson` + `logic` AND/OR) |
| `db/ProgressDao.kt` | Room DAO for scenario progress |
| `db/MasteredWordDao.kt` | Room DAO for mastered words |
| `db/MilestoneRewardDao.kt` | Room DAO for milestone rewards |
| `db/ProgressRepository.kt` | Wraps DAO — star/XP saves, speech rate override, streak |
| `db/Badge.kt` | Badge definitions and unlock logic |
| `navigation/AppNavigation.kt` | `NavHost`, 5-tab bottom nav, `LocalOnboardingCoords` provider, `InteractiveOnboardingOverlay` host |
| `preferences/UserPreferencesRepository.kt` | DataStore — global speech rate, colorThemeIndex, onboarding flag, showIndonesian, disabledTabs, disabledCategories, disabledScenarios |
| `ProgressManager.kt` | Star calculation, XP award, badge unlock coordination |

#### Utilities & UI
| File | Contents |
|------|----------|
| `tts/TtsManager.kt` | `TtsManager` class — `speak()`, `speakAndAwait()` (coroutine-based), `rememberTtsManager()` composable |
| `ToneUtils.kt` | Pinyin tone detection → `Color` for each tone (1–4 + neutral) |
| `ui/ConfettiEffect.kt` | Canvas-based confetti particle animation |
| `ui/StrokeOrderSheet.kt` | Stroke order bottom sheet for Chinese characters |
| `ui/theme/Color.kt` | Base Material 3 colour palette |
| `ui/theme/Theme.kt` | `MandarinKidsTheme` composable — applies active `AppThemeVariant` |
| `ui/theme/AppColorPalettes.kt` | 10 theme variants (`AppThemes` list) — 5 light, 5 dark; `AppThemeVariant(index, name, emoji, palette, md3ColorScheme, isDark)` |
| `ui/theme/AppColorScheme.kt` | Semantic colour tokens (`actionPositive`, `actionNegative`, `tileAmber`, `tilePurple`, `tileGrey`, `masteryGradient`, etc.) + `LocalAppColors` CompositionLocal |
| `ui/theme/Type.kt` | Typography scale |

### Data Model Hierarchy

```
Scenario
├── id, title, description, characterName, characterEmoji, characterRole
├── category: ScenarioCategory (ESSENTIALS | AT_SCHOOL | SCHOOL_SUBJECTS |
│             FOOD_AND_EATING | FEELINGS_AND_HEALTH | PLAY_AND_HOBBIES |
│             HOME | OUT_AND_ABOUT)
├── dialogues: List<DialogueStep>
│   ├── speaker: Speaker (CHARACTER | STUDENT)
│   ├── textChinese, textPinyin, textEnglish, textIndonesian
│   ├── pinyinWords: List<PinyinWord>   ← per-word breakdown with optional note
│   ├── responseType: ResponseType (LISTEN_ONLY | SINGLE_CHOICE | MULTIPLE_OPTIONS | TEXT_INPUT)
│   ├── options: List<ResponseOption>   ← each has its own pinyinWords
│   └── userNameInput: Boolean
└── quizQuestions: List<QuizQuestion>
    ├── direction: QuizDirection (CHINESE_TO_TRANSLATION | TRANSLATION_TO_CHINESE | AUDIO_TO_TRANSLATION)
    ├── questionText, questionChinese, questionPinyin
    ├── options: List<QuizOption>
    ├── correctAnswerIndex: Int
    └── explanation: String
```

### Learning Content

53 scenarios across 8 categories. All content lives in `app/src/main/assets/scenarios/`.
Each scenario has: Chinese + pinyin + English + Indonesian, dialogue steps, TTS, and 3–5 quiz questions.
The index of loaded files is `app/src/main/assets/scenarios/index.json`.

To add a new scenario: create `scenario_XX.json` in assets, add the filename to `index.json`.
Use the `/scenario` command for guided creation with QA checks.

### Theme System

10 colour themes (5 light, 5 dark) defined in `ui/theme/AppColorPalettes.kt` as `AppThemes: List<AppThemeVariant>`.
- **`AppThemeVariant`** — bundles index, name, emoji, `AppColorScheme` palette, MD3 `ColorScheme`, and `isDark` flag
- **`AppColorScheme`** — semantic tokens consumed throughout the app via `MaterialTheme.appColors`
- **`LocalAppColors`** — CompositionLocal providing the active `AppColorScheme`
- Active theme index is persisted in DataStore via `UserPreferencesRepository.saveColorThemeIndex()`
- Theme toggle button is in the `TopAppBar` of `HomeScreen` and `ProgressScreen`, tagged with `OnboardingKey.THEME_BUTTON` for the onboarding spotlight

### Interactive Onboarding

New users see a 10-step spotlight tour (`InteractiveOnboardingOverlay`) drawn on top of the live app:
- Uses `CompositingStrategy.Offscreen` + `BlendMode.Clear` on a Canvas to cut a transparent hole over the target element
- Element bounds are captured via `onGloballyPositioned { lc -> coords[key] = lc.boundsInRoot() }` and shared through `LocalOnboardingCoords`
- `OnboardingKey` object defines string constants for each tagged element: `THEME_BUTTON`, `STATS_ROW`, `CATEGORY_GRID`, `NAV_BAR`, `NAV_FLASHCARD`, `NAV_TONE`, `NAV_BUILD`, `NAV_PROGRESS`
- Steps 5–8 navigate the background to the actual tab being explained via `onNavigateToRoute` callback
- Onboarding completion state is stored in DataStore (`userPrefs.onboardingCompleted`); overlay shown only when `onboardingCompleted == false`

### Text-to-Speech

- `TtsManager` in `tts/TtsManager.kt` — centralised TTS wrapper
- `speak(text, rate)` — fire-and-forget
- `speakAndAwait(text, utteranceId, rate)` — suspends until audio fully completes (uses `UtteranceProgressListener` + `isSpeaking` poll guard)
- `rememberTtsManager()` — Compose composable that creates and disposes `TtsManager` with the composable lifecycle
- Speech rate: 1.0x (normal) or 0.7x (slow) — toggleable per scenario and persisted via `ProgressRepository.saveSpeechRateForScenario()`
- Global default rate stored in `UserPreferencesRepository` (DataStore)
- `Locale.CHINESE` used for all Chinese text playback

### Room Database

`AppDatabase` (version 9) — three entities:

| Entity | Table | Key columns |
|--------|-------|-------------|
| `ScenarioProgressEntity` | `scenario_progress` | `scenarioId`, `stars`, `xp`, `lastPlayedAt`, `speechRateOverride`, `masteryLevel` |
| `MasteredWordEntity` | `mastered_words` | `scenarioId`, `chinese`, `isMastered`, `boxLevel`, `nextReviewDate`, `practiceType` |
| `MilestoneReward` | `milestone_rewards` | `id`, `conditionsJson`, `logic` (AND/OR), `rewardText`, `isClaimed` |

Migrations tracked: 1→2, 2→3, 3→4, 4→5, 5→6, 6→7, 7→8, 8→9.

**Migration notes:**
- **5→6:** Deleted stale mastered_words entries (pre-split flashcards with 5+ CJK characters)
- **6→7:** Added `practiceType` to mastered_words primary key — tracks mastery per modality (DEFAULT, LISTENING, READING)
- **7→8:** Migrated `milestone_rewards` from single `milestoneType+targetValue` to `conditionsJson` + `logic` (AND/OR) for multi-condition reward support
- **8→9:** Added `masteryLevel` column to `scenario_progress` (default 1) for 5-level mastery system

## SDK & Dependencies

- Min SDK: 24, Target/Compile SDK: 36
- AGP: 9.0.1, Kotlin: 2.0.0, Java: 11
- Compose BOM: 2024.12.01
- Jetpack Navigation Compose: 2.9.7
- Room: 2.8.4
- DataStore Preferences: 1.1.1
- kotlinx.serialization.json: 1.7.3
- `material-icons-extended` for icon variety
- Test: JUnit 4, Espresso, Compose UI Test

## Important Notes

- **Offline-first** — all scenario content is JSON in assets, no network calls.
- **Both English and Indonesian translations required** on every Chinese string — maintain this when adding any content.
- **Scenario JSON format** is the source of truth for content. Never hardcode scenarios in Kotlin.
- **pinyinWords must be complete** — every word in every sentence, including particles (吗, 了, 的). Add a `note` for grammar particles.
- **Do not use `private fun`** for sound utilities (`playSuccessSound`, `playWrongSound`) — they are called across files.
- **Room migrations are required** whenever a database entity changes — bump the version and add a migration to `AppDatabase.kt`.
- **Parental controls** (disabledTabs, disabledCategories, disabledScenarios) are stored in DataStore and enforced in navigation and the home screen. Do not bypass these when building new features.
- **MasteredWordEntity primary key** is a composite of `(scenarioId, chinese, practiceType)` — queries must include all three fields.
- **Theme colours** — always use `MaterialTheme.appColors` (the semantic `AppColorScheme`) for UI colours rather than hardcoding. The only exceptions are fixed semantic feedback colours (green correct, red wrong, amber stars).
- **Onboarding coord tags** — when adding new UI elements that the onboarding tour should spotlight, add an `onGloballyPositioned` modifier with a new `OnboardingKey` constant and a matching step in `InteractiveOnboardingOverlay.kt`.

## Claude Skills & Commands

Project-level skills and commands live in `.claude/` (gitignored for memory, but skills/commands are tracked):

| Command | Description |
|---------|-------------|
| `/scenario [description]` | Build a new scenario JSON — guided 6-phase workflow with full QA |
| `/uiux [screen name]` | Audit and improve UI/UX for a screen — 7-lens audit + implement |
