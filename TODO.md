# Milton Learn Mandarin — Feature Backlog

## Improvement: Child-Friendly Particle Explanations in Flashcards

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

## Bug Fix: Pinyin Text Wrapping

**Problem:** Pinyin text in the dialogue/flashcard views overflows horizontally — the user has to scroll right to see the full pinyin. Mandarin characters and translation text wrap correctly; pinyin should behave the same way.

**What to fix:**
- Pinyin text must wrap to a new line instead of overflowing off-screen.
- No pinyin should ever be hidden or require horizontal scrolling to reveal.
- Visual behaviour should match the Mandarin character line and the English/Indonesian translation line — all wrap, all fully visible.

**Files likely affected:** `RolePlayScreen.kt`, `FlashcardScreen.kt` (wherever pinyin `Text` composables are rendered) — check for `softWrap = false`, fixed widths, or missing `modifier = Modifier.fillMaxWidth()` on pinyin elements.

---

## Bug Fix: Pinyin Tone Colour Coding for Multi-Syllable Words

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
