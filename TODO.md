# Milton Learn Mandarin — Improvement Plan

This document outlines planned improvements and new features for the app.

---

## ✅ 1. Flashcard Phase Before Each Scenario ~~(HIGH PRIORITY)~~ — DONE

**Your idea:** Before the role-play conversation begins, introduce each vocabulary word used in the scenario through a flashcard session so Milton can memorize the words first.

**Proposed flow:**
```
Flashcard Session → Role-Play Conversation → Quiz
```

**Flashcard features to implement:**
- Show one word at a time: Chinese character → tap to reveal pinyin + English + Indonesian
- Text-to-Speech plays the word automatically when card is shown
- "I know this" / "Still learning" buttons to filter words for review
- Progress bar showing how many cards remain
- Option to replay all cards before moving on
- Swipe gesture to flip/advance cards

**Data changes needed:**
- Each `DialogueStep` already has `pinyinWords` — extract unique words per scenario to auto-generate the flashcard deck
- Add a `FlashcardScreen.kt` composable
- Update `Screen` sealed class to add `Flashcard` state

---

## ✅ 2. New Scenarios for Elementary School ~~(HIGH PRIORITY)~~ — DONE

Current scenarios (6) cover basic playground and classroom situations. Expand to cover more realistic daily interactions for a 6-year-old entering school.

### Proposed New Scenarios

| # | Title | Character | Situation |
|---|-------|-----------|-----------|
| 7 | 第一天上学 | 班主任老师 👩‍🏫 | First day of school — introduce yourself to the class |
| 8 | 找新朋友 | 同班同学 🧒 | Making a new friend — asking name, age, where they're from |
| 9 | 分享零食 | 朋友小华 🧒 | Sharing snacks — offering, accepting, saying thank you |
| 10 | 借东西 | 同学 🧒 | Borrowing things — pencil, eraser, ruler |
| 11 | 我不舒服 | 老师 👩‍🏫 | Feeling unwell — telling teacher you have a stomachache/headache |
| 12 | 体育课 | 体育老师 🧑‍🏫 | PE class — understanding instructions, asking to sit out |
| 13 | 午餐时间 | 食堂阿姨 👩‍🍳 | Lunch time — ordering food, saying what you like/don't like |
| 14 | 迷路了 | 陌生老师 👨‍🏫 | Getting lost — asking for directions to class/toilet/office |
| 15 | 解决争吵 | 朋友 🧒 | Resolving a disagreement — saying sorry, making up |
| 16 | 回家了 | 妈妈/爸爸 👨‍👩‍👦 | End of school day — telling parents what you learned/did |

---

## 🎮 3. Gamification & Motivation (MEDIUM PRIORITY)

Keep Milton engaged and coming back daily.

- **Star rating per scenario** — 1–3 stars based on combined role-play + quiz score
- **Progress tracking** — show which scenarios are completed on the home screen (lock icon for incomplete)
- **Daily streak counter** — reward for opening the app consecutive days
- **Sticker/badge collection** — earn badges like "First Quiz!", "Perfect Score!", "Playground Master!"
- **Total XP system** — accumulate points across all scenarios, show level (e.g. 初学者 → 小达人 → 中文小明星)
- **Celebratory animation on perfect score** — confetti, fireworks, or bouncing emojis

---

## 🔊 4. Audio & Pronunciation Improvements (MEDIUM PRIORITY)

- **Word-level TTS on tap** — already partially implemented; ensure every Chinese word in flashcards and conversation can be tapped to hear pronunciation
- **Tone marks colour coding** — colour each pinyin syllable by its tone (tone 1 = red, tone 2 = orange, tone 3 = green, tone 4 = blue, neutral = grey) to visually teach tones
- **Slow mode per word** — long-press any word to hear it at 0.5x speed
- **Record & compare** — let Milton record himself saying a phrase and play it back next to the correct TTS pronunciation (requires microphone permission)

---

## 🎨 5. UI & Kid-Friendliness (MEDIUM PRIORITY)

The current UI is functional but designed for adults. Optimise for a 6-year-old.

- **Larger touch targets** — buttons should be at least 64dp tall
- **Friendly cartoon characters** — replace emoji characters with simple illustrated avatars for each scenario character (Teacher Wang, Ming, etc.)
- **Animated character reactions** — character bounces or waves when speaking
- **Fun colour theme** — bright, warm, child-appropriate colour palette instead of default Material purple
- **Custom app icon** — a fun icon (e.g. a panda holding a Chinese lantern) instead of the default Android icon
- **Font** — use a rounder, more child-friendly font (e.g. Nunito or Baloo)

---

## 🏗️ 6. Architecture Improvements (LOW PRIORITY)

Technical improvements to make the codebase easier to maintain and extend.

- **ViewModel layer** — move state out of composables into ViewModels for better lifecycle handling
- **Room database** — persist user progress, scores, and streak data across app restarts
- **DataStore** — save user preferences (speech speed, display language)
- **Jetpack Navigation** — replace manual `Screen` sealed class with Navigation Compose for cleaner back-stack handling
- **Scenario loading from JSON** — move scenario data from hardcoded Kotlin files to JSON assets so content can be updated without a code change
- **Unit tests for scenarios** — ensure all scenarios have valid quiz questions and complete dialogue chains

---

## 🌐 7. Language Options (LOW PRIORITY)

- **Toggle Indonesian translations on/off** — some parents may want English-only subtitles
- **Parent mode** — a settings screen (PIN protected) where parents can configure difficulty, enable/disable features, and view progress reports
- **Simplified vs Traditional Chinese** — currently uses Simplified; add option for Traditional for families from Taiwan/HK

---

## 🐛 Known Issues to Fix

- [ ] Old `spellingchampion` test files still present in git history — clean up
- [ ] `Divider()` composable is deprecated in newer Material3 — replace with `HorizontalDivider()`
- [ ] Quiz results screen can overflow on small screens — add scrolling
- [ ] No empty state shown when a scenario has 0 quiz questions

---

## 📋 Implementation Priority Order

1. 🃏 Flashcard phase (biggest learning impact)
2. 📚 New scenarios 7–10 (more content)
3. 🎮 Star rating + completion tracking (motivation)
4. 🎨 Kid-friendly UI refresh (engagement)
5. 📚 New scenarios 11–16 (more content)
6. 🔊 Tone colour coding (pronunciation)
7. 🏗️ Architecture (maintainability)
