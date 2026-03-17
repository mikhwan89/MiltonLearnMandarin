# MiltonLearnMandarin — Improvement Backlog

Ordered roughly by impact. Items #1–5 are highest-leverage.

---

## 📖 New Scenarios (Content Expansion)

We have 19 scenarios. Target: 30+. Add in priority order below.

### #1 — Food & Eating batch (2 new scenarios)
**Category:** `FOOD_AND_EATING`

- **场景 A — 水果摊 (At the Fruit Stand):** Buy fruit from a vendor. Vocab: 苹果, 香蕉, 西瓜, 多少钱, 请给我…
- **场景 B — 我会做饭 (Cooking with Mum/Dad):** Help prepare a simple meal. Vocab: 切, 炒, 好吃, 我帮你, 锅

Add as `scenario_20.json` and `scenario_21.json`.

---

### #2 — Around the House batch (2 new scenarios)
**Category:** `HOME` *(new category)*

- **场景 A — 起床啦 (Morning Routine):** Wake up, brush teeth, get dressed. Vocab: 起床, 刷牙, 穿衣服, 快点, 早饭
- **场景 B — 睡觉时间 (Bedtime):** Goodnight routine with family. Vocab: 晚安, 我爱你, 做好梦, 关灯, 讲故事

Add as `scenario_22.json` and `scenario_23.json`.

---

### #3 — Animals & Nature batch (2 new scenarios)
**Category:** `ANIMALS` *(new category)*

- **场景 A — 动物园 (At the Zoo):** Visit animals, ask what they are. Vocab: 老虎, 大象, 长颈鹿, 猴子, 我想看
- **场景 B — 我的宠物 (My Pet):** Talk about a pet (dog/cat). Vocab: 狗, 猫, 喂食, 好可爱, 它叫什么名字

Add as `scenario_24.json` and `scenario_25.json`.

---

### #4 — School Subjects batch (2 new scenarios)
**Category:** `SCHOOL_SUBJECTS`

- **场景 A — 美术课 (Art Class):** Draw and describe a picture. Vocab: 画画, 颜色, 红色, 蓝色, 漂亮
- **场景 B — 音乐课 (Music Class):** Sing a simple song, name instruments. Vocab: 唱歌, 钢琴, 鼓, 一起唱, 好听

Add as `scenario_26.json` and `scenario_27.json`.

---

### #5 — Health & Feelings batch (1 new scenario)
**Category:** `FEELINGS_AND_HEALTH`

- **场景 — 看医生 (At the Doctor):** Describe symptoms, receive reassurance. Vocab: 发烧, 头疼, 肚子疼, 打针, 快好了

Add as `scenario_28.json`.

---

### #6 — Shopping & Errands (1 new scenario)
**Category:** `ESSENTIALS`

- **场景 — 去超市 (At the Supermarket):** Help parent pick items, handle the cashier. Vocab: 多少钱, 找零, 谢谢, 不客气, 我要买

Add as `scenario_29.json`.

---

## 🎮 New Game Modes

### #7 — Listening Challenge mode
**Files:** new `practice/ListeningChallengeScreen.kt`

A reverse of the flashcard: play TTS audio only (no Chinese text shown), then pick the correct English meaning from 4 options. Tests listening comprehension without reading support. Add as a second mode inside the Flashcard tab — toggle between "Read → Choose" and "Listen → Choose".

---

### #8 — Tone Trainer mode
**Files:** new `practice/ToneTrainerScreen.kt`

Show a syllable in pinyin WITHOUT the tone mark (e.g. "ma"), play audio, and ask the child to pick the correct tone: 1st 2nd 3rd 4th Neutral. Color-coded buttons matching `ToneUtils` colours. Great for ear training. Add as a third mode option in the Flashcard tab.

---

### #9 — Sentence Builder mini-game
**Files:** new `practice/SentenceBuilderScreen.kt`

Given a reference English sentence and a shuffled set of Chinese word tiles, the child drags/taps tiles into the correct order. Draw sentences from existing scenario dialogues so no new content is needed. Add as optional activity at the end of each scenario (alongside the existing quiz).

---

## 🏅 Engagement & Gamification

### #10 — Level-up full-screen celebration
**Files:** `home/ProgressScreen.kt`, `navigation/AppNavigation.kt`

When total XP crosses a `ProgressManager` level threshold, show a full-screen animated celebration (confetti + big level badge) before returning to the home tab. Currently level-up is silent. One `LaunchedEffect` comparing old vs new level after any XP change.

---

### #11 — Unlock scenario characters as collectibles
**Files:** `home/ProgressScreen.kt`, `data/models/ScenarioModels.kt`

When a scenario is 3-starred, its character emoji is "unlocked" and shown in a special Collected Characters grid on the Progress screen. Locked characters appear as ❓. Creates a clear visual collection goal for a 5-year-old.

---

### #12 — Daily practice goal & home screen ring
**Files:** `home/HomeScreen.kt`, `db/ProgressRepository.kt`

Show a circular progress ring on the Home tab header: "Today: 3 / 5 words practised". Track a simple daily word-review count in `ProgressRepository`. Resets at midnight. Gives Milton a tangible daily target beyond streaks.

---

## 🔔 Notifications & Parent Tools

### #13 — Daily reminder notification
**Files:** new `notifications/ReminderWorker.kt`

Use `WorkManager` with a `PeriodicWorkRequest` (daily, at a parent-configurable time set in Parent Dashboard) to fire a local notification: "Time to practise with Milton! 🐼". Opt-in, configured from the Parent Dashboard.

---

### #14 — Weekly progress summary for parent
**Files:** `parent/ParentDashboardScreen.kt`

A "This Week" card showing: scenarios completed, words reviewed, XP gained, streak status. Data already exists in Room — just aggregate over the last 7 days. Gives the parent visibility without needing to dig through the Progress tab.

---

## 🖼️ Content Quality

### #15 — Cultural notes on scenario completion
**Files:** `data/models/ScenarioModels.kt`, `QuizResultsScreen.kt`

Add an optional `culturalNote: String?` field to `Scenario`. Display it as a fun fact card on the Results screen after completing a scenario (e.g. "In China, students greet teachers by standing up — 起立!"). Write one note per scenario.

---

### #16 — Vocabulary browser (all words by category)
**Files:** new `home/VocabBrowserScreen.kt`

A searchable, scrollable list of every `PinyinWord` across all scenarios, grouped by scenario category. Tapping a word opens the existing `WordDetailDialog`. Accessible from the Learn tab as a secondary entry point. Useful for revision without replaying full scenarios.

---

## 🛠️ Technical Improvements

### #17 — Tablet / large screen two-pane layout
**Files:** `navigation/AppNavigation.kt`, `home/HomeScreen.kt`

On screens wider than 600dp (detected via `LocalConfiguration`), show the scenario list on the left and the active scenario/roleplay on the right in a two-pane layout. No new screens needed — just conditional layout wrapping.

---

### #18 — Progress data export for parent
**Files:** `parent/ParentDashboardScreen.kt`

Add a "Share Progress" button that generates a plain-text or simple HTML summary of Milton's stats (XP, streak, stars per scenario, mastered words) and shares it via the Android share sheet (`Intent.ACTION_SEND`). No backend needed.

---

## Notes

- New scenarios must include: Chinese title, English title, `characterEmoji`, `characterName`, dialogues with `pinyinWords`, and at least 3 quiz questions. Both English and Indonesian translations required in all content.
- Run `/uiux [ScreenName]` before implementing any new screen.
- Test each feature: *Can a 5-year-old use this without frustration? Is reward feedback immediate?*
