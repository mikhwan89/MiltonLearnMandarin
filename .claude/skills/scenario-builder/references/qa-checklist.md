# Scenario QA Checklist — MiltonLearnMandarin

Run every check below before finalising any scenario JSON.
A scenario must pass ALL items before being written to a file.

---

## 🧒 Age-Appropriateness

- [ ] The situation is realistic for a child aged 4–8 (school, home, playground, simple errands)
- [ ] No scary, violent, adult, or distressing content
- [ ] Vocabulary is within everyday child range — no abstract concepts, no formal/bureaucratic language
- [ ] Each CHARACTER line is ≤ 18 Chinese characters
- [ ] Each STUDENT response option is ≤ 12 Chinese characters
- [ ] Total dialogue steps: between 3 and 6 (inclusive)
- [ ] Number of new vocabulary words introduced: between 8 and 15
- [ ] Scenario ends positively — child succeeds, is thanked, or gets what they needed

---

## 🗣️ Dialogue Flow Realism

- [ ] Opening line sets the scene naturally — starts with a greeting or natural question, not a lecture
- [ ] Each CHARACTER line flows naturally from the previous line — the conversation makes logical sense
- [ ] STUDENT responses are phrased as a real child would speak (not formal adult Mandarin)
- [ ] No two `LISTEN_ONLY` steps in a row — each listen step is followed by a STUDENT turn
- [ ] All options in `MULTIPLE_OPTIONS` steps are genuinely valid responses in the situation
- [ ] `SINGLE_CHOICE` wrong options are plausible but clearly less appropriate (not nonsensical)
- [ ] Final dialogue step closes the conversation naturally (farewell, thank you, warm wrap-up)
- [ ] `userNameInput: true` appears on at most ONE step in the entire scenario

---

## 🀄 Chinese Language Accuracy

- [ ] All characters are Simplified Chinese (no Traditional forms)
- [ ] Grammar is correct Mandarin — not a literal word-for-word translation from English
- [ ] Vocabulary is appropriate for HSK 1–3 range (elementary level)
- [ ] No chengyu, literary phrases, or adult idioms
- [ ] Measure words used correctly where applicable (一本书, not 一个书)
- [ ] 的/地/得 used correctly according to their grammatical function

---

## 🔤 Pinyin Completeness & Accuracy (most critical)

- [ ] `textPinyin` covers every character in `textChinese` — count the syllables, they must match
- [ ] Every non-neutral syllable has a tone mark (ā á ǎ à)
- [ ] Neutral-tone syllables (吗 ma, 的 de, 了 le, 呢 ne, 们 men, 着 zhe, 嘛 ma) have NO tone mark ✓
- [ ] Tone sandhi applied correctly where needed:
  - 不 is bù by default; becomes bú before a 4th-tone syllable (e.g. 不是 bú shì)
  - 一 is yī by default; becomes yí before 4th tone, yì before 1st/2nd/3rd tone
  - Two consecutive 3rd tones: the first is spoken as 2nd tone (e.g. 你好 written nǐ hǎo, spoken as ní hǎo — keep written form nǐ hǎo in pinyin)
- [ ] Every `pinyinWords` entry has tone marks matching `textPinyin`
- [ ] The `pinyin` field in each `pinyinWord` matches the corresponding segment of `textPinyin` exactly
- [ ] `pinyinWords` covers **every word** in the sentence — nothing is skipped
- [ ] Same rules apply to `pinyinWords` inside every `ResponseOption`

---

## 📝 pinyinWords Coverage Check

For each DialogueStep and each ResponseOption, verify:

- [ ] Count of words in `pinyinWords` ≥ count of distinct words in `textChinese`
- [ ] Particles present: every 吗、了、的、呢、把、着、嘛 has its own entry
- [ ] Particles have a `note` field with a child-friendly explanation (not null)
- [ ] Measure words have a `note` field explaining their usage
- [ ] No entry has empty `pinyin`, `chinese`, `english`, or `indonesian` strings
- [ ] Multi-character words are grouped sensibly (同学 → one entry, not 同 + 学 separately)

---

## 🇬🇧 English Translation Quality

- [ ] `textEnglish` reads as natural, conversational English — not a literal gloss
- [ ] Contractions used where a native speaker would (it's, you're, I'm, don't)
- [ ] `pinyinWord.english` entries are word-level glosses (shorter, more literal than `textEnglish`)
- [ ] No unnatural phrasings (e.g. "I am called [name]" — prefer "My name is [name]")

---

## 🇮🇩 Indonesian Translation Quality

- [ ] `textIndonesian` reads as natural conversational Bahasa Indonesia
- [ ] Correct register: child-to-child uses aku/kamu; child-to-teacher/adult uses saya/Bapak/Ibu
- [ ] `pinyinWord.indonesian` entries are word-level glosses (not full sentence translations)
- [ ] No overly formal written Indonesian (avoid: saya/Anda in child-to-child speech)
- [ ] Cultural adaptations are natural — e.g. 老师 → "guru" (not "Mr/Ms" unless it fits)

---

## 🧩 Quiz Questions

- [ ] Minimum 3 quiz questions, maximum 5
- [ ] At least one `TRANSLATION_TO_CHINESE` question
- [ ] At least one `CHINESE_TO_TRANSLATION` question
- [ ] At least one `AUDIO_TO_TRANSLATION` question
- [ ] All quiz words/phrases appeared in the scenario dialogues — no surprise vocabulary
- [ ] Every question has exactly 4 options
- [ ] `correctAnswerIndex` is correct (0-based — recount manually)
- [ ] Distractors are plausible (same topic, similar register) — not obviously wrong
- [ ] `explanation` adds context beyond just restating the answer ("You say this when...", "This is the polite way to...")
- [ ] For `CHINESE_TO_TRANSLATION` + `AUDIO_TO_TRANSLATION` options: `chinese` and `pinyin` are `""`, only `translation` is filled
- [ ] `questionChinese` and `questionPinyin` filled for `CHINESE_TO_TRANSLATION` and `AUDIO_TO_TRANSLATION`
- [ ] `questionChinese` and `questionPinyin` are `""` for `TRANSLATION_TO_CHINESE`

---

## 🗂️ JSON Structure

- [ ] Top-level `id` follows pattern `scene{N}_{snake_case_description}` (e.g. `scene20_buying_snacks`)
- [ ] `category` is a valid enum value: `ESSENTIALS`, `AT_SCHOOL`, `SCHOOL_SUBJECTS`, `FOOD_AND_EATING`, `FEELINGS_AND_HEALTH`, `PLAY_AND_HOBBIES`
- [ ] `characterRole` is a lowercase string (teacher, classmate, parent, friend, shopkeeper, etc.)
- [ ] `characterEmoji` is a single emoji
- [ ] All `DialogueStep.id` values are sequential integers starting from 1
- [ ] No trailing commas (valid JSON)
- [ ] No fields with `null` except `PinyinWord.note` which may be `null`
- [ ] `options` array is `[]` (empty array, not absent) on `LISTEN_ONLY` steps
- [ ] `userNameInput` present on every dialogue step (default `false`)
- [ ] `isCorrect` present on every `ResponseOption`

---

## ✅ Final Registration

- [ ] Filename follows pattern `scenario_XX.json` (XX = zero-padded next number)
- [ ] Filename added to `app/src/main/assets/scenarios/index.json`
- [ ] Scenario `id` is unique — does not clash with any existing scenario ID
