---
name: scenario-builder
description: This skill should be used when the user asks to "add a new scenario", "create a scenario", "write a scenario", "build a new lesson", "add new content", "write new dialogues", "create a new role-play", "add a new topic", "build a lesson about [topic]", or anything involving creating or editing scenario JSON files for the MiltonLearnMandarin app.
version: 1.0.0
---

# Scenario Builder Skill

This skill guides all scenario creation for the MiltonLearnMandarin app — an interactive Mandarin learning app for children aged **4–8**, primarily for Milton (the developer's son).

A "scenario" is a short role-play conversation + quiz that teaches a real-world Mandarin exchange a child would have at school or in daily life.

---

## App Context

- **Learner:** Children aged 4–8 (primary user is ~5 years old)
- **Learning pair:** Chinese characters + pinyin + English + Indonesian translation for every string
- **Format:** JSON files in `app/src/main/assets/scenarios/`
- **Index:** `app/src/main/assets/scenarios/index.json` — new scenario filename must be added here
- **Naming:** Files are `scenario_XX.json` (next number after highest existing)
- **Data models:** `app/src/main/java/com/ikhwan/mandarinkids/data/models/ScenarioModels.kt`

---

## JSON Schema

Every scenario file must conform exactly to this shape:

```json
{
  "id": "scene{N}_{snake_case_title}",
  "title": "Chinese title (with English subtitle optional)",
  "description": "One sentence in English describing the scenario",
  "characterName": "Character's display name",
  "characterEmoji": "Single emoji representing the character",
  "characterRole": "teacher | classmate | parent | shopkeeper | friend | etc.",
  "category": "ESSENTIALS | AT_SCHOOL | SCHOOL_SUBJECTS | FOOD_AND_EATING | FEELINGS_AND_HEALTH | PLAY_AND_HOBBIES",
  "dialogues": [ ...DialogueStep ],
  "quizQuestions": [ ...QuizQuestion ]
}
```

### DialogueStep shape

```json
{
  "id": 1,
  "speaker": "CHARACTER | STUDENT",
  "textChinese": "Full Chinese sentence",
  "textPinyin": "Full pinyin with tone marks for every syllable",
  "textEnglish": "Natural English translation",
  "textIndonesian": "Natural Indonesian translation",
  "pinyinWords": [ ...PinyinWord ],
  "responseType": "LISTEN_ONLY | SINGLE_CHOICE | MULTIPLE_OPTIONS | TEXT_INPUT",
  "options": [ ...ResponseOption ],
  "userNameInput": false
}
```

- `LISTEN_ONLY` — CHARACTER speaks, no response needed, auto-advances after a pause
- `SINGLE_CHOICE` — exactly one correct option; one wrong option optional
- `MULTIPLE_OPTIONS` — 2–3 options that are ALL correct (child chooses freely, any is valid)
- `TEXT_INPUT` — child types a free response (use sparingly; only for name input)
- `userNameInput: true` — only when the STUDENT must say their own name; only ONE step per scenario should ever have this

### PinyinWord shape

```json
{
  "pinyin": "tóngxué",
  "chinese": "同学",
  "english": "classmate",
  "indonesian": "teman sekelas",
  "note": null
}
```

- `note` — optional string. Use for particles (吗, 了, 的, 呢, 把), measure words, or any word a child might find confusing. Write the note in plain English a 5-year-old can understand. Leave `null` for regular vocabulary words.

### ResponseOption shape

```json
{
  "chinese": "谢谢老师！",
  "pinyin": "Xièxie lǎoshī!",
  "english": "Thank you, teacher!",
  "indonesian": "Terima kasih, guru!",
  "pinyinWords": [ ...PinyinWord ],
  "isCorrect": true
}
```

- In `MULTIPLE_OPTIONS`, ALL options should have `isCorrect: true` (they're all acceptable responses)
- In `SINGLE_CHOICE`, one option has `isCorrect: true`, others have `isCorrect: false`
- Every option must have its own `pinyinWords` array — same rules as the parent dialogue step

### QuizQuestion shape

```json
{
  "direction": "TRANSLATION_TO_CHINESE | CHINESE_TO_TRANSLATION | AUDIO_TO_TRANSLATION",
  "questionText": "The prompt shown to the child",
  "questionChinese": "",
  "questionPinyin": "",
  "options": [ ...QuizOption ],
  "correctAnswerIndex": 0,
  "explanation": "Brief child-friendly explanation of the correct answer"
}
```

- `TRANSLATION_TO_CHINESE` — English/Indonesian phrase shown → pick the correct Chinese+pinyin
- `CHINESE_TO_TRANSLATION` — Chinese+pinyin shown → pick the correct English translation. Set `questionChinese` and `questionPinyin`; set `questionText` to "What does this mean?"
- `AUDIO_TO_TRANSLATION` — TTS plays the Chinese → pick the correct English meaning. Set `questionChinese` (the audio source) and `questionPinyin`; set `questionText` to "Listen and choose the correct meaning!"
- Always 4 options per question. `correctAnswerIndex` is 0-based.
- For `CHINESE_TO_TRANSLATION` and `AUDIO_TO_TRANSLATION` options: only `translation` is filled, `chinese` and `pinyin` are `""`

### QuizOption shape

```json
{
  "chinese": "早上好，老师",
  "pinyin": "Zǎoshang hǎo, lǎoshī",
  "translation": "Good morning, teacher"
}
```

---

## Category Enum Reference

| Value | Display | Emoji | Use for |
|-------|---------|-------|---------|
| `ESSENTIALS` | Essentials | 👋 | Greetings, introductions, farewells, basic social phrases |
| `AT_SCHOOL` | At School | 🏫 | Classroom requests, asking teacher, borrowing things, getting help |
| `SCHOOL_SUBJECTS` | School Subjects | 📖 | Maths, science, Chinese class, answering questions in class |
| `FOOD_AND_EATING` | Food & Eating | 🍎 | Canteen, snacks, sharing food, ordering, food preferences |
| `FEELINGS_AND_HEALTH` | Feelings & Health | 💗 | Emotions, illness, telling an adult how you feel |
| `PLAY_AND_HOBBIES` | Play & Hobbies | ⚽ | Playground, sports, inviting friends, talking about hobbies |

---

## Design Rules for Scenarios

### 1. Age-Appropriateness (most important)

- **Vocabulary level:** Words a 5–8 year old would genuinely need. No abstract concepts, no adult topics.
- **Sentence length:** CHARACTER lines: maximum 15–18 Chinese characters. STUDENT response options: maximum 12 characters. Long sentences overwhelm young learners and TTS takes too long.
- **Number of dialogue steps:** 3–6 steps total. Under 4 feels too short; over 6 feels too long for a child.
- **Number of quiz questions:** Minimum 3, maximum 5. Always include at least one of each: `TRANSLATION_TO_CHINESE`, `CHINESE_TO_TRANSLATION`, and `AUDIO_TO_TRANSLATION`.
- **Vocabulary introduced:** Aim for 8–15 unique new words per scenario. Don't introduce 30 new words — children can't absorb that.
- **No scary, violent, or distressing content** — even mild (e.g. "I got hurt badly") should be handled gently ("My tummy hurts a little").

### 2. Dialogue Flow Realism

- The scenario must mirror a situation a child aged 4–8 would **actually encounter** — at school, at home, at a playground, or a shop.
- The opening CHARACTER line sets the scene naturally. Don't start with a lecture — start with a greeting or a question.
- Responses must be things a **child would actually say**, not adult-style formal Chinese.
- The conversation should resolve positively — the child helps someone, learns something, or gets what they needed.
- If there is a `LISTEN_ONLY` step, follow it immediately with a meaningful STUDENT turn (don't chain two LISTEN_ONLY steps back-to-back).
- `MULTIPLE_OPTIONS` steps: offer 2–3 responses that are all socially valid in the situation. Don't include wrong or rude options unless it is a `SINGLE_CHOICE` quiz-style step.
- The final step should feel like a natural close — a farewell, a thank-you, or a warm wrap-up line.

### 3. Chinese Language Quality

- Use **Simplified Chinese** characters only (no Traditional).
- Use vocabulary appropriate for elementary Mandarin (HSK 1–3 range is ideal for most words).
- Avoid chengyu, literary phrases, or adult idioms.
- The dialogue must be grammatically correct Mandarin — not literal translations from English.
- Common natural phrases preferred: 好的、没关系、谢谢、不客气、对不起、可以吗、我想要

### 4. Pinyin Quality (critical)

- **Every syllable must have a tone mark.** No unmarked syllables except for explicitly neutral-tone syllables (e.g. 吗 ma, 呢 ne, 的 de, 了 le, 着 zhe, 们 men).
- Tone marks: ā á ǎ à (tone 1–4), no mark = neutral/tone 5.
- The full sentence `textPinyin` must match `textChinese` exactly — every character in the Chinese maps to a syllable in the pinyin, in order.
- **pinyinWords must cover every word in the sentence** — do not skip words. Function words (是、在、了) count too.
- Pinyin in `pinyinWords` should match the corresponding segment in `textPinyin` exactly (same tone marks, same spacing).
- Multi-character words can be grouped as one `pinyinWord` entry (e.g. `"同学"` → `"tóngxué"`).
- Common tone reference:
  - 你 nǐ (3rd), 好 hǎo (3rd), 我 wǒ (3rd), 是 shì (4th), 不 bù (4th, or bú before 4th tone), 老师 lǎoshī, 同学 tóngxué, 谢谢 xièxie (4th + neutral), 什么 shénme (2nd + neutral), 名字 míngzi (2nd + neutral), 可以 kěyǐ (3rd + 3rd), 早上 zǎoshang (3rd + neutral), 高兴 gāoxìng (1st + 4th), 认识 rènshi (4th + neutral)

### 5. Translation Quality

- **English:** Natural, conversational — what a native English speaker would actually say, not a word-for-word gloss. Use contractions (it's, you're, I'm) as a native speaker would.
- **Indonesian:** Natural Bahasa Indonesia — not formal written Indonesian. Children use informal/conversational register (aku/kamu, not saya/Anda in child-to-child speech; saya/Bapak/Ibu in child-to-teacher speech).
- `pinyinWords` entries: the `english` and `indonesian` fields are **glosses** (word-level meanings), not full sentence translations. They will be shorter and more literal than `textEnglish` / `textIndonesian`.
- Don't translate cultural references out of existence — keep 老师 as "teacher" (not localised to a Western title).

### 6. pinyinWords Completeness

This is the most commonly missed requirement. Every `pinyinWords` array must:
- Cover **every word** in the corresponding Chinese sentence (including particles, pronouns, verbs, conjunctions)
- Have the **same tone marks** as in `textPinyin`
- Have a `note` for: 吗、了、的、呢、把、着、嘛、呗 and any measure word or grammatical particle
- Not have duplicate entries for the same word unless it appears twice in the sentence
- Match the order of words as they appear in the sentence (left to right)

---

## Step-by-Step Process for Building a Scenario

When asked to create a scenario, follow these phases:

### Phase 1 — Plan
1. Decide the situation (what real-world moment does this capture?)
2. Decide the character (who is the child speaking to? teacher, friend, parent, stranger?)
3. Decide the category
4. Sketch 3–6 dialogue exchanges — a natural arc: open → exchange → close
5. List 8–15 key vocabulary words the scenario will teach
6. Confirm vocabulary is within HSK 1–3 range and age-appropriate

### Phase 2 — Draft the Dialogues
1. Write `textChinese` for each step — keep sentences short and natural
2. Write `textPinyin` — add correct tone marks to every syllable
3. Write `textEnglish` — natural English
4. Write `textIndonesian` — natural conversational Indonesian
5. Assign `responseType` logically:
   - CHARACTER speaks with no action needed → `LISTEN_ONLY`
   - The child must choose one specific polite response → `SINGLE_CHOICE`
   - Multiple responses all make sense → `MULTIPLE_OPTIONS`
6. Write `pinyinWords` for every step and every option — cover every word, no gaps
7. Add `note` fields for all particles and confusing grammar words

### Phase 3 — Draft the Quiz Questions
1. Write 3–5 questions testing the scenario's key phrases
2. Mix directions: at least one of each (`TRANSLATION_TO_CHINESE`, `CHINESE_TO_TRANSLATION`, `AUDIO_TO_TRANSLATION`)
3. Write 4 options per question — one correct, three plausible distractors
4. Set `correctAnswerIndex` carefully (0-based, double-check)
5. Write a short child-friendly `explanation` for each correct answer

### Phase 4 — Self QA (always run before outputting the file)
Run every check in `.claude/skills/scenario-builder/references/qa-checklist.md`.
Do not output the JSON until all checklist items pass.

### Phase 5 — Output and Register
1. Output the complete JSON as a code block
2. State the filename (`scenario_XX.json`)
3. State the line to add to `index.json`
4. Note any words that were especially tricky (pinyin tone edge cases, translation choices)

---

## Common Mistakes to Avoid

| Mistake | Correct approach |
|---------|-----------------|
| Missing tone mark on a syllable | Every syllable gets a mark except neutral-tone ones |
| `textPinyin` doesn't match `textChinese` character count | Count syllables — one per character |
| `pinyinWords` skips particles (是、在、了、吗) | Every word in the sentence must appear in `pinyinWords` |
| Using formal Indonesian (saya/Anda in child-to-child) | aku/kamu for peer speech, saya for child-to-adult |
| Sentence too long (>18 chars) | Split into two dialogue steps |
| All quiz questions same direction | Mix all three `QuizDirection` types |
| `correctAnswerIndex` wrong | Re-count 0-based index after writing options |
| `MULTIPLE_OPTIONS` with one wrong option | All options in `MULTIPLE_OPTIONS` must be valid |
| Realistic but depressing scenario | Always ends warmly and positively |
| Quiz explanation is just a repeat of the answer | Explain *why* or give context ("You say this when...") |
