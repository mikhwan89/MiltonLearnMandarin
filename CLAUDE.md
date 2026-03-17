# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MiltonLearnMandarin is an Android app that teaches Mandarin to young children (age 4–8) through interactive role-play scenarios and quizzes. Built for the developer's son Milton.

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
**Data storage:** Room database v5 + DataStore preferences
**Content:** JSON files in `app/src/main/assets/scenarios/` (19 scenarios), loaded at runtime via `JsonScenarioLoader`

### Screen Flow

```
HomeScreen (tab: Learn)
  └── ScenarioListScreen (by category)
        └── RolePlayScreen → QuizScreen → QuizResultsScreen
                                               └── retry → QuizScreen
HomeScreen (tab: Practice)
  └── PracticeScreen (flashcard drill)
        └── FlashcardScreen (per-scenario flashcards)
HomeScreen (parent icon)
  └── PinScreen → ParentDashboardScreen
```

### Navigation Routes (`navigation/Routes.kt`)

```kotlin
Routes.HOME              // "home"
Routes.PRACTICE          // "practice"
Routes.PARENT_DASHBOARD  // "parent_dashboard"
Routes.CATEGORY          // "category/{categoryName}"
Routes.FLASHCARD         // "flashcard/{scenarioId}"
Routes.ROLEPLAY          // "roleplay/{scenarioId}"
Routes.QUIZ              // "quiz/{scenarioId}/{rolePlayScore}"
```

Navigation is wired in `navigation/AppNavigation.kt`.

### Key Source Files

All under `app/src/main/java/com/ikhwan/mandarinkids/`:

#### Screens & ViewModels
| File | Contents |
|------|----------|
| `MainActivity.kt` | `ComponentActivity`, entry point, `MandarinKidsApp` host composable |
| `home/HomeScreen.kt` | Home screen — progress card, word-of-day, badges, practice banner, category list |
| `home/ScenarioListScreen.kt` | Scenario cards filtered by category |
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
| `db/AppDatabase.kt` | Room database v5, three entities, migrations 1→5 |
| `db/ScenarioProgressEntity.kt` | Stars, XP, lastPlayedAt, speechRateOverride per scenario |
| `db/MasteredWordEntity.kt` | Per-word "got it" status for flashcard spaced repetition |
| `db/MilestoneReward.kt` | Parent-defined reward targets |
| `db/ProgressDao.kt` | Room DAO for scenario progress |
| `db/MasteredWordDao.kt` | Room DAO for mastered words |
| `db/MilestoneRewardDao.kt` | Room DAO for milestone rewards |
| `db/ProgressRepository.kt` | Wraps DAO — star/XP saves, speech rate override, streak |
| `db/Badge.kt` | Badge definitions and unlock logic |
| `preferences/UserPreferencesRepository.kt` | DataStore — global speech rate, streak, onboarding flag |
| `ProgressManager.kt` | Star calculation, XP award, badge unlock coordination |

#### Utilities & UI
| File | Contents |
|------|----------|
| `tts/TtsManager.kt` | `TtsManager` class — `speak()`, `speakAndAwait()` (coroutine-based), `rememberTtsManager()` composable |
| `ToneUtils.kt` | Pinyin tone detection → `Color` for each tone (1–4 + neutral) |
| `ui/ConfettiEffect.kt` | Canvas-based confetti particle animation |
| `ui/StrokeOrderSheet.kt` | Stroke order bottom sheet for Chinese characters |
| `ui/theme/Color.kt` | Material 3 colour palette |
| `ui/theme/Theme.kt` | `MandarinKidsTheme` composable |
| `ui/theme/Type.kt` | Typography scale |

### Data Model Hierarchy

```
Scenario
├── id, title, description, characterName, characterEmoji, characterRole
├── category: ScenarioCategory (ESSENTIALS | AT_SCHOOL | SCHOOL_SUBJECTS |
│             FOOD_AND_EATING | FEELINGS_AND_HEALTH | PLAY_AND_HOBBIES)
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

19 scenarios across 6 categories. All content lives in `app/src/main/assets/scenarios/`.
Each scenario has: Chinese + pinyin + English + Indonesian, dialogue steps, TTS, and 3–5 quiz questions.
The index of loaded files is `app/src/main/assets/scenarios/index.json`.

To add a new scenario: create `scenario_XX.json` in assets, add the filename to `index.json`.
Use the `/scenario` command for guided creation with QA checks.

### Text-to-Speech

- `TtsManager` in `tts/TtsManager.kt` — centralised TTS wrapper
- `speak(text, rate)` — fire-and-forget
- `speakAndAwait(text, utteranceId, rate)` — suspends until audio fully completes (uses `UtteranceProgressListener` + `isSpeaking` poll guard)
- `rememberTtsManager()` — Compose composable that creates and disposes `TtsManager` with the composable lifecycle
- Speech rate: 1.0x (normal) or 0.7x (slow) — toggleable per scenario and persisted via `ProgressRepository.saveSpeechRateForScenario()`
- Global default rate stored in `UserPreferencesRepository` (DataStore)
- `Locale.CHINESE` used for all Chinese text playback

### Room Database

`AppDatabase` (version 5) — three entities:

| Entity | Table | Key columns |
|--------|-------|-------------|
| `ScenarioProgressEntity` | `scenario_progress` | `scenarioId`, `stars`, `xp`, `lastPlayedAt`, `speechRateOverride` |
| `MasteredWordEntity` | `mastered_words` | `scenarioId`, `chinese`, `isMastered` |
| `MilestoneReward` | `milestone_rewards` | `id`, `milestoneType`, `targetValue`, `rewardText`, `isClaimed` |

Migrations tracked: 1→2, 2→3, 3→4, 4→5.

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

## Claude Skills & Commands

Project-level skills and commands live in `.claude/` (gitignored for memory, but skills/commands are tracked):

| Command | Description |
|---------|-------------|
| `/scenario [description]` | Build a new scenario JSON — guided 6-phase workflow with full QA |
| `/uiux [screen name]` | Audit and improve UI/UX for a screen — 7-lens audit + implement |
