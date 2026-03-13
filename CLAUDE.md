# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MiltonLearnMandarin is an Android app that teaches Mandarin to young children (age 5+) through interactive role-play scenarios and quizzes. Built for the developer's son Milton.

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
**State management:** Compose `remember`/`mutableStateOf` — no ViewModel layer
**Navigation:** Manual sealed class pattern (no Jetpack Navigation library)
**Data:** Static in-memory data, no database or remote API

### Screen Flow

```
Home Screen → RolePlay Screen → Quiz Screen → Results
     ↑                                           |
     └───────────────── retry ──────────────────┘
```

Navigation sealed class (defined in `MainActivity.kt`):
```kotlin
sealed class Screen {
    object Home : Screen()
    data class RolePlay(val scenario: Scenario) : Screen()
    data class Quiz(val scenario: Scenario, val rolePlayScore: Int) : Screen()
}
```

### Key Source Files

All under `app/src/main/java/com/ikhwan/mandarinkids/`:

- `MainActivity.kt` — ComponentActivity + `MandarinKidsApp` composable + `HomeScreen` + `ScenarioCard`
- `RolePlayScreen.kt` — Interactive dialogue flow with TTS, step progression, score tracking
- `QuizScreen.kt` — Quiz, `QuizOptionButton`, `FeedbackCard`, `QuizResultsScreen`
- `PhrasesScreen.kt` — Standalone phrase browser with TTS, `PhraseCard`, category lists
- `data/models/ScenarioModels.kt` — All data classes
- `data/scenarios/ScenarioRepository.kt` — Aggregates all 6 scenarios
- `data/scenarios/Scenario1_GreetingTeacher.kt` through `Scenario6_Goodbye.kt`
- `ui/theme/` — Theme.kt, Color.kt, Type.kt (Material 3)

### Data Model Hierarchy

```
Scenario
├── dialogues: List<DialogueStep>
│   ├── speaker: Speaker (CHARACTER | STUDENT)
│   ├── responseType: ResponseType (LISTEN_ONLY | SINGLE_CHOICE | MULTIPLE_OPTIONS | TEXT_INPUT)
│   ├── options: List<ResponseOption>
│   └── pinyinWords: List<PinyinWord>
└── quizQuestions: List<QuizQuestion>
    ├── direction: QuizDirection (CHINESE_TO_TRANSLATION | TRANSLATION_TO_CHINESE)
    └── options: List<QuizOption>
```

### Learning Content

6 scenarios covering real-world child situations:
1. Greeting Teacher (王老师)
2. Meeting Ming (peer intro)
3. Snack Time (food vocab)
4. Asking to use the Bathroom (classroom request)
5. Playground (play vocab)
6. Goodbye

Each scenario has: Chinese + pinyin + English + Indonesian text, TTS audio, and 3+ quiz questions.

### Text-to-Speech

- `Locale.CHINESE` used for Chinese character/pinyin playback
- Speech rate configurable (0.7x slow / 1.0x normal) via toggle in RolePlayScreen
- TTS initialized with `LaunchedEffect(Unit)` and shut down with `DisposableEffect(Unit)`

## SDK & Dependencies

- Min SDK: 24, Target/Compile SDK: 36
- AGP: 9.0.1, Kotlin: 2.0.0, Java: 11
- Compose BOM: 2024.12.01
- `material-icons-extended` included for icon variety
- Test: JUnit 4, Espresso, Compose UI Test

## Important Notes

- App is **offline-first** — all content is hardcoded in Kotlin data files, no network calls.
- Both English and Indonesian translations are in all content — maintain this when adding scenarios.
- To add a new scenario: create `ScenarioN_Name.kt`, implement the function, add it to `ScenarioRepository.getAllScenarios()`.
