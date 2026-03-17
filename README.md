# Milton Learn Mandarin 🇨🇳

An Android app that teaches Mandarin Chinese to young children (age 5+) through interactive role-play conversations, flashcards, and quizzes. Built by a dad for his son Milton.

---

## What It Does

Milton Learn Mandarin puts a child in real-world school situations and guides them through a Mandarin conversation step by step. Each scenario has three phases:

1. **Flashcards** — learn the new vocabulary words before the conversation starts. Each word shows the Chinese character, pinyin (colour-coded by tone), English, and Indonesian translation. Tap a card to flip it. Mark words as "Got it" or "Still learning".
2. **Role-play** — have a back-and-forth conversation with a character (teacher, classmate, etc.). Tap any pinyin word to see its tone colour, translation, and a child-friendly explanation. Grammar particles like 了, 的, and 吗 include a plain-English tip so a 5-year-old understands what they do.
3. **Quiz** — answer multiple-choice questions about what was just practised. A results screen shows stars earned and XP gained.

Progress (stars, XP, streak) is saved locally and shown on the home screen.

---

## Screenshots

> _Coming soon_

---

## Scenarios

The app currently includes **12 scenarios** covering everyday school life:

| # | Title | Situation |
|---|-------|-----------|
| 1 | 第一天：问候老师 | First day at school — greeting your teacher |
| 2 | 认识新朋友 | Meeting Liu Ming, your new classmate |
| 3 | 零食时间 | Sharing snacks with a friend |
| 4 | 请求帮助 | Asking the teacher for permission politely |
| 5 | 操场游戏 | Joining classmates on the playground |
| 6 | 放学了 | End of the school day — saying goodbye |
| 7 | 借东西 | Borrowing a pencil, eraser, or ruler |
| 8 | 我不舒服 | Telling the teacher you feel unwell |
| 9 | 迷路了 | Getting lost and asking for directions |
| 10 | 解决争吵 | Saying sorry and making up with a friend |
| 11 | 回家了 | Telling your parents about your school day |
| 12 | 举手提问 | Raising your hand to ask and answer questions |

All scenarios include Chinese, pinyin, English, and Indonesian text. Adding a new scenario requires only a single JSON file — no Kotlin changes needed.

---

## Tone Colour System

Pinyin is colour-coded by tone throughout the app — in flashcards, conversation bubbles, and the word detail popup. Multi-syllable words (e.g. **pángbiān** 旁边) show mixed colours, one per syllable:

| Colour | Tone | Mark | Example |
|--------|------|------|---------|
| 🔴 Red | Tone 1 — flat | ā | māo |
| 🟠 Orange | Tone 2 — rising | á | máo |
| 🟢 Green | Tone 3 — dip | ǎ | mǎo |
| 🔵 Blue | Tone 4 — falling | à | mào |
| ⚫ Grey | Neutral | (none) | ma, le, de |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0.0 |
| UI | Jetpack Compose (BOM 2024.12.01), Material Design 3 |
| Navigation | Jetpack Navigation Compose 2.9.7 |
| State | `ViewModel` + `mutableStateOf` |
| Local DB | Room 2.8.4 (progress persistence) |
| Preferences | DataStore Preferences 1.1.1 (speech rate, language toggle) |
| Serialisation | kotlinx.serialization 1.7.3 (JSON scenario loading) |
| TTS | Android `TextToSpeech` with `Locale.CHINESE` |
| Build | AGP 9.0.1, KSP 2.2.10-2.0.2, Java 11 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

---

## Project Structure

```
app/src/main/
├── assets/
│   └── scenarios/          # One JSON file per scenario (scenario_01.json … scenario_12.json)
└── java/com/ikhwan/mandarinkids/
    ├── MainActivity.kt             # Entry point, sets up NavHost
    ├── FlashcardScreen.kt          # Flashcard phase UI
    ├── FlashcardViewModel.kt       # Flashcard state (deck, mastered words)
    ├── RolePlayScreen.kt           # Role-play phase UI + ConversationBubble
    ├── RolePlayViewModel.kt        # Role-play state (step index, score, TTS speed)
    ├── QuizScreen.kt               # Quiz phase UI + results screen
    ├── QuizViewModel.kt            # Quiz state (question index, score, feedback)
    ├── PhrasesScreen.kt            # Standalone phrase browser
    ├── ProgressManager.kt          # Pure functions: calculateStars(), getLevel()
    ├── ToneUtils.kt                # Tone detection, colour mapping, syllable splitter,
    │                               #   AnnotatedString builder for mixed-tone pinyin
    ├── data/
    │   ├── models/
    │   │   └── ScenarioModels.kt   # All @Serializable data classes (see Data Model below)
    │   └── scenarios/
    │       ├── ScenarioRepository.kt      # interface ScenarioRepository
    │       └── JsonScenarioRepository.kt  # Loads + deserialises JSON assets
    ├── db/
    │   ├── AppDatabase.kt              # Room database singleton
    │   ├── ProgressDao.kt             # @Dao: upsert, getById, getTotalXp
    │   ├── ProgressRepository.kt      # saveProgress(), getTotalXp(), getStars(), streak
    │   └── ScenarioProgressEntity.kt  # @Entity: scenarioId, stars, xp, lastPlayedAt
    ├── home/
    │   └── HomeScreen.kt           # Home screen with scenario list, XP, level, streak
    ├── navigation/
    │   ├── AppNavigation.kt        # NavHost with all composable destinations
    │   └── Routes.kt               # Route constants and URL builders
    ├── preferences/
    │   └── UserPreferencesRepository.kt  # DataStore: speechRate, showIndonesian
    ├── tts/
    │   └── TtsManager.kt           # Shared TTS wrapper, rememberTtsManager()
    └── ui/theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Data Model

```
Scenario
├── id, title, description, characterName, characterEmoji, characterRole
├── dialogues: List<DialogueStep>
│   ├── id, speaker (CHARACTER | STUDENT)
│   ├── textChinese, textPinyin, textEnglish, textIndonesian
│   ├── responseType (LISTEN_ONLY | SINGLE_CHOICE | MULTIPLE_OPTIONS | TEXT_INPUT)
│   ├── options: List<ResponseOption>
│   │   ├── chinese, pinyin, english, indonesian, isCorrect
│   │   └── pinyinWords: List<PinyinWord>
│   └── pinyinWords: List<PinyinWord>
│       ├── chinese, pinyin, english, indonesian
│       └── note: String?   ← child-friendly grammar tip (particles, measure words, etc.)
└── quizQuestions: List<QuizQuestion>
    ├── direction (CHINESE_TO_TRANSLATION | TRANSLATION_TO_CHINESE)
    ├── questionText, questionChinese, questionPinyin
    ├── options: List<QuizOption>  (chinese, pinyin, translation)
    ├── correctAnswerIndex
    └── explanation
```

---

## Screen Flow

```
Home Screen
    │
    ├──▶ Flashcard Screen  ──▶  Role-Play Screen  ──▶  Quiz Screen  ──▶  Results
    │                                                                        │
    │                               ◀──────────── retry (back to Flashcard) ┘
    │
    └──▶ Phrases Screen  (standalone vocabulary browser)
```

---

## Adding a New Scenario

No Kotlin needed. Create a JSON file in `app/src/main/assets/scenarios/` following this structure:

```json
{
  "id": "scene13_your_id",
  "title": "场景标题",
  "description": "One-line description in English",
  "characterName": "角色名",
  "characterEmoji": "👩‍🏫",
  "characterRole": "Teacher",
  "dialogues": [
    {
      "id": 1,
      "speaker": "CHARACTER",
      "textChinese": "你好！",
      "textPinyin": "Nǐ hǎo!",
      "textEnglish": "Hello!",
      "textIndonesian": "Halo!",
      "responseType": "LISTEN_ONLY",
      "pinyinWords": [
        { "pinyin": "Nǐ", "chinese": "你", "english": "you", "indonesian": "kamu" },
        { "pinyin": "hǎo", "chinese": "好", "english": "good", "indonesian": "baik" }
      ]
    }
  ],
  "quizQuestions": [
    {
      "direction": "CHINESE_TO_TRANSLATION",
      "questionText": "What does 你好 mean?",
      "options": [
        { "chinese": "你好", "pinyin": "Nǐ hǎo", "translation": "Hello" },
        { "chinese": "再见", "pinyin": "Zài jiàn", "translation": "Goodbye" }
      ],
      "correctAnswerIndex": 0,
      "explanation": "你好 means Hello"
    }
  ]
}
```

Then register the file in `JsonScenarioRepository` so it is included when the app loads.

**Tips:**
- `speaker` must be `"CHARACTER"` or `"STUDENT"`
- `responseType` options: `"LISTEN_ONLY"`, `"SINGLE_CHOICE"`, `"MULTIPLE_OPTIONS"`, `"TEXT_INPUT"`
- Add a `"note"` field to any `pinyinWord` entry that needs a child-friendly grammar explanation (especially particles like 了, 的, 吗)
- Both English and Indonesian translations are required on every text field

---

## Building & Running

```bash
# Clone
git clone https://github.com/mikhwan89/MiltonLearnMandarin.git
cd MiltonLearnMandarin

# Run from Android Studio terminal (uses bundled JDK)
./gradlew assembleDebug       # build debug APK
./gradlew assembleRelease     # build release APK
./gradlew test                # run unit tests (JVM, no device needed)
./gradlew connectedAndroidTest # run instrumented tests (requires device/emulator)
./gradlew lint                # lint check
./gradlew clean               # clean build outputs
```

Requires **Android Studio Hedgehog or newer**. The project uses AGP 9.0.1 with the bundled Kotlin plugin — no separate Kotlin plugin installation needed.

---

## Unit Tests

Pure JVM tests live in `app/src/test/` — no emulator or Robolectric required:

| Test file | What it covers |
|-----------|---------------|
| `ProgressManagerTest` | `calculateStars` thresholds, `getLevel` / `getLevelLabel` boundaries |
| `QuizViewModelTest` | `selectAnswer` scoring, guard against double-answer, `advanceQuestion` state transitions |
| `RolePlayViewModelTest` | Step progression, `submitName`, `selectOption`, speech speed toggle |
| `JsonParsingTest` | Scenario JSON parsing, optional field defaults, unknown field tolerance |
| `TestFixtures` | Shared test data builders (`testScenario`, `testDialogueStep`, etc.) |

---

## Offline First

The app works entirely offline. All scenario content is bundled as JSON assets. TTS uses the Android system TTS engine. No network calls are made.

---

## License

This project is released into the public domain under the [MIT License](LICENSE). Feel free to copy, fork, modify, and use it for anything — no permission needed. Built with ❤️ for Milton.
