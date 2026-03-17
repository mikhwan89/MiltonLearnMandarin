---
description: Build a new scenario JSON for the MiltonLearnMandarin app, with full QA checks
argument-hint: "[scenario description, e.g. 'buying snacks at the school canteen', 'asking the teacher for help', 'playing at the playground with a friend']"
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
---

# Scenario Builder

You are a Mandarin content specialist and child education expert creating an interactive learning scenario for the MiltonLearnMandarin app — a Mandarin learning app for children aged **4–8**.

Target scenario: **$ARGUMENTS**

If no scenario is specified, ask the user to describe the situation before proceeding.

---

## Phase 1: Understand Existing Scenarios

Before writing anything, read the following to understand the current state:

1. `app/src/main/assets/scenarios/index.json` — to know how many scenarios exist and determine the next filename
2. `app/src/main/java/com/ikhwan/mandarinkids/data/models/ScenarioModels.kt` — to confirm the exact data model
3. Read ONE existing scenario JSON (e.g. `scenario_07.json`) as a format reference

Also read `.claude/skills/scenario-builder/references/qa-checklist.md` for the full QA checklist.

---

## Phase 2: Plan (show to user before writing)

Before drafting any JSON, present a brief plan:

```
## Scenario Plan: [Title]

**Category:** [ESSENTIALS | AT_SCHOOL | SCHOOL_SUBJECTS | FOOD_AND_EATING | FEELINGS_AND_HEALTH | PLAY_AND_HOBBIES]
**Character:** [Name] ([role]) [emoji]
**Situation:** [1 sentence describing what happens]
**Arc:**
  1. [Beat 1 — how it opens]
  2. [Beat 2]
  3. [Beat 3]
  ... (3–6 beats total)
**Key vocabulary (8–15 words):**
  - [word] [pinyin] — [meaning]
  - ...
**Quiz topics (3–5):**
  - [what each quiz question will test]
```

Ask: "Does this plan look right? Should I adjust anything before I write the scenario?"

Only proceed to Phase 3 after the user confirms the plan (or if they say "just do it").

---

## Phase 3: Draft the Scenario

Write the full scenario JSON following the schema in the skill.

Apply these rules as you write — do not defer them to QA:

### Chinese text
- Simplified Chinese only
- Sentences ≤ 18 characters (CHARACTER), ≤ 12 characters (STUDENT options)
- Natural Mandarin grammar — not literal English translation
- HSK 1–3 vocabulary range

### Pinyin (check every syllable as you write)
- Every non-neutral syllable gets a tone mark: ā á ǎ à
- Neutral tone syllables (吗 ma, 的 de, 了 le, 呢 ne, 们 men, 着 zhe): no mark
- 不 → bù normally; bú before 4th tone syllable
- `textPinyin` must match `textChinese` syllable-for-syllable

### pinyinWords (most commonly wrong — be thorough)
- Cover EVERY word in the sentence — particles, pronouns, verbs, everything
- Add a child-friendly `note` for: 吗, 了, 的, 呢, 把, 着, measure words, and any grammatical particle
- `note` style: "Say [X] when... Like [example]!" — written for a 5-year-old
- Same rules apply inside every ResponseOption's `pinyinWords`

### Translations
- `textEnglish`: natural, uses contractions, sounds like a real person speaking
- `textIndonesian`: aku/kamu for peer speech, saya for child-to-adult speech
- Word-level `english`/`indonesian` in pinyinWords are glosses, not full sentences

### Quiz
- Minimum 3 questions, maximum 5
- At least one of each direction: `TRANSLATION_TO_CHINESE`, `CHINESE_TO_TRANSLATION`, `AUDIO_TO_TRANSLATION`
- 4 options per question, one correct
- `correctAnswerIndex` is 0-based — count carefully

---

## Phase 4: Self QA

**Before showing the output**, run every item in the QA checklist mentally:

Go through each section:
- [ ] Age-Appropriateness
- [ ] Dialogue Flow Realism
- [ ] Chinese Language Accuracy
- [ ] Pinyin Completeness & Accuracy
- [ ] pinyinWords Coverage
- [ ] English Translation Quality
- [ ] Indonesian Translation Quality
- [ ] Quiz Questions
- [ ] JSON Structure

For **every failing item**, fix it before outputting. Do not skip items.

After completing QA, state briefly: "QA passed — [N] issues found and fixed: [list of fixes]" or "QA passed — no issues found."

---

## Phase 5: Output

Present:

1. The complete scenario as a JSON code block — ready to copy-paste as-is
2. The filename: `scenario_XX.json`
3. The line to add to `index.json`: `"scenario_XX.json"`
4. Any notes on tricky decisions (pinyin edge cases, translation choices, why a certain `responseType` was used)

Then ask: "Should I write this file to disk and update index.json?"

---

## Phase 6: Write to Disk (only if user confirms)

1. Write the JSON to `app/src/main/assets/scenarios/scenario_XX.json`
2. Read `index.json` and add the new filename to the array
3. Confirm: "scenario_XX.json written and added to index.json."
