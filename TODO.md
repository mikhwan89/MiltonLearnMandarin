# Milton Learn Mandarin — Backlog

---

## #1 · Improvement: Child-Friendly Particle Explanations in Flashcards ✅ DONE

**Problem:** Grammar particles like 了 (le), 的 (de), 吗 (ma), 呢 (ne), 把 (bǎ) currently show only "particle" as their English translation. A 5-year-old has no idea what a particle is and gains nothing from that label.

**What to fix:**
- Replace or supplement the bare "particle" translation with a plain-English explanation written at a 5-year-old level.
- The explanation should answer: *what does this word do in a sentence?* and *when would I say it?*
- Where possible, include a short example sentence in Chinese + pinyin + English so the child sees the word in action.

**Suggested explanations per particle (reference for content editors):**

| Word | Pinyin | Current | Child-friendly replacement |
|------|--------|---------|---------------------------|
| 了 | le | particle | "You say 了 after something that just happened. Like 'I ate! 我吃了！'" |
| 的 | de | particle | "的 is like 's in English — it shows something belongs to someone. '我的书' = 'my book'." |
| 吗 | ma | particle | "Put 吗 at the end of a sentence to turn it into a yes/no question. '你好吗？' = 'Are you well?'" |
| 呢 | ne | particle | "呢 means 'And what about…?' You use it to ask the same question back. '你呢？' = 'And you?'" |
| 把 | bǎ | particle | "把 is used before the thing you are doing something to. '把书放下' = 'Put the book down'." |

**How to implement:**
- Add a `note` field (optional `String?`) to the `PinyinWord` data model and JSON schema.
- Populate `note` for all particle words across all scenario JSON files.
- In the flashcard UI, show the `note` below the translation in a slightly smaller, softer style when it is present — no UI change needed for words without a note.
- The `note` field can also be used for non-particle words that benefit from extra context (e.g. measure words, directional complements).

**Files affected:** `data/models/ScenarioModels.kt` (add `note` field), all scenario JSON files (add notes to particle entries), flashcard UI composable (render note when present).

---

## #2 · Bug Fix: Pinyin Text Wrapping ✅ DONE

**Problem:** Pinyin text in the dialogue/flashcard views overflows horizontally — the user has to scroll right to see the full pinyin. Mandarin characters and translation text wrap correctly; pinyin should behave the same way.

**What to fix:**
- Pinyin text must wrap to a new line instead of overflowing off-screen.
- No pinyin should ever be hidden or require horizontal scrolling to reveal.
- Visual behaviour should match the Mandarin character line and the English/Indonesian translation line — all wrap, all fully visible.

**Files likely affected:** `RolePlayScreen.kt`, `FlashcardScreen.kt` (wherever pinyin `Text` composables are rendered) — check for `softWrap = false`, fixed widths, or missing `modifier = Modifier.fillMaxWidth()` on pinyin elements.

---

## #3 · Bug Fix: Pinyin Tone Colour Coding for Multi-Syllable Words ✅ DONE

**Problem:** Colour coding is applied per whole word, so a two-syllable word like "pángbiān" (旁边) renders in a single colour even though each syllable has a different tone. The colour should reflect the tone of each individual syllable.

**What to fix:**
- Split each pinyin word into its constituent syllables before applying colour.
- Assign a tone colour to each syllable independently based on its tone mark (ā/á/ǎ/à = tones 1–4, no mark = neutral).
- Render the syllables inline in a single line using an `AnnotatedString` or a `Row` of coloured `Text` spans so the word reads as one unit with mixed colours.
- Example: "pángbiān" → "páng" in tone-2 colour + "biān" in tone-1 colour, displayed side-by-side with no gap.

**Tone colour reference (existing convention in the app):**
- Tone 1 (ā) — red
- Tone 2 (á) — orange/yellow
- Tone 3 (ǎ) — green
- Tone 4 (à) — blue
- Neutral (a) — grey

**Files likely affected:** Wherever `getToneColor()` or equivalent is called and pinyin colour is applied — likely `RolePlayScreen.kt`, `FlashcardScreen.kt`, possibly a shared utility function.

---

## ✅ #4 · Feature: Flashcard Practice Mode

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
- `navigation/Routes.kt` — add flashcard practice route
- `navigation/AppNavigation.kt` — add the destination

---

## ✅ #5 · Feature: Scenario Categories on Home Screen

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
- Assign a category to each existing scenario in their JSON files.
- `HomeScreen` groups scenarios by category using `groupBy { it.category }` before rendering.

---

## ✅ #6 · Feature: Spaced Repetition for Flashcards

**What:** Instead of showing flashcards in a fixed order, use a simple spaced repetition algorithm (like Leitner boxes) so words Milton struggles with come up more often, and words he knows well appear less frequently.

**Behaviour:**
- Each `PinyinWord` gets a box level (1–5). All words start at box 1.
- "Remember it" promotes the word one box. "Forgot it" drops it back to box 1.
- Higher-box words are shown less often (e.g. box 1 = every session, box 3 = every 3rd session, box 5 = once a week).
- The home screen or flashcard screen can show a count: "X words due for review today."

**Data needed:**
- Add `boxLevel: Int` and `nextReviewDate: Long` to the per-word progress stored in Room.

---

## ✅ #7 · Feature: Word of the Day

**What:** Each day the app surfaces one word Milton hasn't fully mastered yet — shown as a banner or card on the Home screen. Tapping it plays the TTS and shows the flashcard for that word.

**Behaviour:**
- Pick a word randomly from words in box 1–2 (not yet mastered) that hasn't been the word of the day in the last 7 days.
- Banner stays the same word for the whole day (resets at midnight).
- Tapping the banner opens the single flashcard for that word with TTS auto-playing.
- If all words are mastered, show an encouraging message instead.

**Data needed:**
- Store `lastWordOfDayId: String` and `lastWordOfDayDate: Long` in DataStore (user preferences).

---

## ✅ #8 · Feature: Achievements and Badges

**What:** Reward Milton with visual badges for milestones to keep him motivated. Badges appear on the Home screen in a dedicated row and can be tapped to see what they are for.

**Suggested badges:**

| Badge | Trigger |
|-------|---------|
| First Steps | Complete first scenario |
| Perfect Score | Get 3 stars on any scenario |
| Word Collector | Unlock 10 flashcard words |
| Streak Starter | Log in 3 days in a row |
| Streak Champion | Log in 7 days in a row |
| All Stars | Get 3 stars on every scenario |
| Speed Learner | Complete a quiz without using slow mode |

**Data needed:**
- Store a set of earned badge IDs in Room or DataStore. Check and award badges after each scenario completion.

---

## ✅ #9 · Feature: Listening Mode Quiz

**What:** A reverse-quiz mode where TTS plays a Chinese word or sentence and the child picks the correct English/Indonesian meaning from multiple options — training the ear rather than reading recognition.

**Behaviour:**
- TTS plays the audio automatically when the question loads. A speaker button replays it.
- Options show English (or Indonesian) text only — no Chinese characters shown during the question.
- Correct/wrong feedback same as the existing quiz.
- Can be added as a new `QuizDirection` value (e.g. `AUDIO_TO_TRANSLATION`) alongside the existing directions.

**Data needed:**
- No new data — reuses existing `QuizQuestion` structure with a new direction enum value.

---

## ✅ #10 · Feature: Stroke Order Animation for Chinese Characters

**What:** In the flashcard view, show an animated stroke order diagram for each Chinese character so Milton can learn how to write the character correctly, not just recognise it.

**Behaviour:**
- A small "How to write" button appears on each flashcard.
- Tapping it opens a full-screen overlay showing the character being drawn stroke by stroke as an animation.
- Animation can be replayed by tapping the character.
- Uses a stroke order data library or SVG-based stroke data (e.g. the open-source `hanzi-writer` data set ported to Android).

**Implementation note:** This is the highest-effort item on the list. A third-party stroke data library or asset bundle will be needed. Assess feasibility before starting.

---

## ✅ #11 · Improvement: Celebratory Animations on Correct Answers

**What:** Add a brief, fun animation when the child picks the correct answer or finishes a scenario — confetti, a star burst, or a bouncing emoji. Makes correct answers feel rewarding for a 5-year-old.

**Behaviour:**
- Correct answer in quiz/role-play: 0.5–1 second particle burst or emoji pop animation over the selected option.
- Scenario completion (3 stars): full-screen confetti for ~2 seconds before the results card fades in.
- Animations should not block interaction — they play and disappear automatically.

**Implementation note:** Can be done with Compose `AnimatedVisibility` + `Canvas`-based particle system, or a lightweight Lottie animation file.

---

## #12 · Improvement: Parent Dashboard

**What:** A locked section (PIN or hold-to-unlock) on the Home screen that shows a parent-friendly progress summary — which scenarios have been completed, star ratings, XP history, and time spent in the app. Parents can also set custom rewards tied to milestones so the child can see exactly what they are working towards.

**Behaviour:**

*Progress summary (parent view):*
- Accessible via a small settings/parent icon on the Home screen.
- Requires a 4-digit PIN to enter (set on first use).
- Shows: per-scenario star rating, total XP, current level, daily streak, total sessions, total time spent.
- Option to reset all progress (with confirmation).
- Option to toggle Indonesian translations on/off globally (saves to DataStore).

*Milestone rewards (parent sets, child sees):*
- Inside the dashboard, a "Rewards" section lets the parent create reward promises tied to a specific milestone.
- Each reward has: a milestone type (reach XP threshold / earn X stars on a scenario / complete X scenarios / hit a streak), the target value, and a free-text reward description (e.g. "Ice cream at your favourite shop!" or "New toy of your choice!").
- Multiple rewards can be active at the same time.
- Rewards are marked as "claimed" manually by the parent once delivered — this clears them from the child's view.

*Child-facing reward banner:*
- On the Home screen, below the XP/level display, a "My Rewards" section shows all active (unclaimed) reward cards.
- Each card shows: the reward description the parent wrote, a progress bar or counter toward the milestone (e.g. "320 / 500 XP"), and a celebratory animation when the milestone is reached.
- When a milestone is hit, the card glows/pulses and shows "You did it! Tell Mum/Dad to unlock your reward!" — the card stays visible until the parent marks it as claimed inside the dashboard.
- Completed-but-unclaimed rewards are visually distinct (e.g. gold border) from in-progress ones.

**Data needed:**
- `MilestoneReward` Room entity: `id`, `milestoneType` (enum), `targetValue`, `rewardText`, `isClaimed`, `createdAt`.
- Session duration needs to be tracked — record session start/end time in Room.
- PIN stored in DataStore (hashed, not plaintext).

---

## #13 · Improvement: More Scenarios

**What:** Expand the scenario library beyond the current 6 to cover more real-world situations a child in school would encounter. Target: 20+ scenarios across all 6 categories.

**High-priority gaps to fill (suggested next 10):**

| # | Title | Category |
|---|-------|----------|
| 7 | First Day of School | Essentials |
| 8 | Introducing Your Family | Essentials |
| 9 | Borrowing a Pencil | At School |
| 10 | PE Class | At School |
| 11 | Maths Class | School Subjects |
| 12 | School Canteen Lunch | Food & Eating |
| 13 | I Don't Feel Well | Feelings & Health |
| 14 | Talking About Feelings | Feelings & Health |
| 15 | Let's Play Together | Play & Hobbies |
| 16 | My Favourite Sport | Play & Hobbies |

**Implementation note:** Each new scenario is a single JSON file in `assets/scenarios/` — no Kotlin changes needed (architecture item 1 is already done).

---

## #14 · Improvement: Slow-Mode TTS Toggle Persists Per-Scenario

**What:** Currently the slow/normal speech toggle resets between sessions. Many children benefit from slow mode being on by default for harder scenarios. Allow the preference to be saved per scenario, not globally.

**Behaviour:**
- When the user toggles slow mode inside a scenario, save that preference keyed to `scenarioId`.
- Next time they open the same scenario, slow mode is restored to where they left it.
- The global default (from DataStore) is still used for scenarios never explicitly toggled.

**Data needed:**
- Add a `speechRateOverride: Float?` column to `ScenarioProgressEntity` in Room. `null` means use global default.

---

## #15 · Improvement: Onboarding Flow for First Launch

**What:** When Milton opens the app for the very first time, show a short 3-screen onboarding sequence that introduces the app, lets him type or pick his name, and picks a favourite character/avatar. Makes the experience feel personal from the start.

**Behaviour:**
- Screen 1: Welcome illustration + "Hi! I'm here to help you learn Mandarin!"
- Screen 2: "What's your name?" — text input, name saved to DataStore (used in scenarios that reference the user's name).
- Screen 3: Pick a favourite colour or avatar (cosmetic, saved to DataStore).
- After onboarding, goes straight to Home. Onboarding never shown again.

**Data needed:**
- `hasCompletedOnboarding: Boolean` and `userName: String` in DataStore.
- `userName` from DataStore should pre-fill the name input in role-play scenarios that ask for the child's name.

---

## #16 · Refactor: Split Large Screen Files into Smaller Composables

**Priority:** Low — nothing is broken, but the files are large enough that finding and editing a specific component requires scrolling through a lot of unrelated code.

**Current problem files:**

| File | Lines | Issue |
|------|-------|-------|
| `QuizScreen.kt` | 738 | `QuizResultsScreen` (226 lines) is a full screen crammed into the same file as `QuizScreen` |
| `RolePlayScreen.kt` | 693 | `ConversationBubble` (250 lines) has a word-detail dialog baked inline; `ConversationMessage` data class lives here instead of in models |

**Specific splits to make:**

*QuizScreen.kt → 3 files:*
- `QuizScreen.kt` — keep only the main `QuizScreen` composable and `QuizOptionButton`
- `FeedbackCard.kt` — move `FeedbackCard` composable here
- `QuizResultsScreen.kt` — move `QuizResultsScreen` composable here (it is effectively a separate screen)
- Move `playWrongSound()` and `playSuccessSound()` into `tts/TtsManager.kt` or a new `util/SoundUtils.kt`

*RolePlayScreen.kt → 2 files:*
- `RolePlayScreen.kt` — keep main screen, `NameInputSection`, `ResponseOptionButton`
- `ConversationBubble.kt` — move `ConversationBubble` composable here; extract the inline word-detail dialog into its own private `WordDetailDialog` composable within the same file
- Move `ConversationMessage` data class to `data/models/ScenarioModels.kt` where the other models live

**What NOT to do:** Do not change any composable signatures, ViewModel logic, or navigation — this is purely a file organisation change. All public function names stay the same.

**Files affected:** `QuizScreen.kt`, `RolePlayScreen.kt`, `data/models/ScenarioModels.kt`, possibly new `FeedbackCard.kt`, `QuizResultsScreen.kt`, `ConversationBubble.kt`.
