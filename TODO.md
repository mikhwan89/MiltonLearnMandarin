# MiltonLearnMandarin — Improvement Backlog

Ordered roughly by impact. Items #1–5 are highest-leverage.

---

## 📖 New Scenarios (Content Expansion)

We have 19 scenarios. Target: 30+. Add in priority order below.

### ✅ #1 — Food & Eating batch (2 new scenarios) *(done)*
**Category:** `FOOD_AND_EATING`

- **场景 A — 水果摊 (At the Fruit Stand):** Buy fruit from a vendor. Vocab: 苹果, 香蕉, 西瓜, 多少钱, 请给我…
- **场景 B — 我会做饭 (Cooking with Mum/Dad):** Help prepare a simple meal. Vocab: 切, 炒, 好吃, 我帮你, 锅

Add as `scenario_20.json` and `scenario_21.json`.

---

### ✅ #2 — Around the House batch (2 new scenarios) *(done)*
**Category:** `HOME` *(new category)*

- **场景 A — 起床啦 (Morning Routine):** Wake up, brush teeth, get dressed. Vocab: 起床, 刷牙, 穿衣服, 快点, 早饭
- **场景 B — 睡觉时间 (Bedtime):** Goodnight routine with family. Vocab: 晚安, 我爱你, 做好梦, 关灯, 讲故事

Add as `scenario_22.json` and `scenario_23.json`.

---

### ✅ #3 — Animals & Nature batch (1 new scenario) *(done)*
**Category:** `ANIMALS` *(new category)*

- **场景 A — 动物园 (At the Zoo):** Visit animals, ask what they are. Vocab: 老虎, 大象, 长颈鹿, 猴子, 我想看
- **场景 B — 我的宠物 (My Pet):** Talk about a pet (dog/cat). Vocab: 狗, 猫, 喂食, 好可爱, 它叫什么名字

Add as `scenario_24.json` and `scenario_25.json`.

---

### ✅ #4 — School Subjects batch (4 new scenarios) *(done)*
**Category:** `SCHOOL_SUBJECTS`

- **场景 A — 美术课 (Art Class):** Draw and describe a picture. Vocab: 画画, 颜色, 红色, 蓝色, 漂亮
- **场景 B — 音乐课 (Music Class):** Sing a simple song, name instruments. Vocab: 唱歌, 钢琴, 鼓, 一起唱, 好听

Add as `scenario_26.json` and `scenario_27.json`.

---

### ✅ #5 — Health & Feelings batch (7 new scenarios) *(done)*
**Category:** `FEELINGS_AND_HEALTH` / `FOOD_AND_EATING`

- **场景 A — 看医生 (At the Doctor):** Describe symptoms, receive reassurance. Vocab: 发烧, 头疼, 肚子疼, 打针, 快好了
- **场景 B — 我好开心！(Happiness):** Gold star day, sharing joy. Vocab: 开心, 高兴, 骄傲, 心情, 表扬, 快乐, 分享
- **场景 C — 我好兴奋！(Excitement):** Amusement park trip announced. Vocab: 兴奋, 期待, 等不及, 游乐园, 过山车, 旋转木马
- **场景 D — 我很难过 (Sadness):** Toy broke / argued with friend. Vocab: 难过, 伤心, 哭, 陪, 安慰, 永远爱你
- **场景 E — 我好无聊 (Boredom):** Nothing to do, go to the park. Vocab: 无聊, 东西, 出去玩, 公园, 点子
- **场景 F — 我饿了！(Hunger):** Hungry at lunchtime, choose food. Vocab: 饿, 肚子, 米饭, 面条, 洗手, 午饭
- **场景 G — 我好害怕！(Scared):** Thunderstorm at night, learning bravery. Vocab: 害怕, 打雷, 声音, 勇敢, 深呼吸, 骄傲

Added as `scenario_30.json` through `scenario_36.json`.

---

### ✅ #6 — Shopping & Errands (1 new scenario) *(done)*
**Category:** `ESSENTIALS`

- **场景 — 去超市 (At the Supermarket):** Help Dad pick items, ask the cashier, receive change. Vocab: 超市, 多少钱, 一共, 找零, 礼貌, 谢谢, 我要买

Added as `scenario_37.json`.

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

## 🃏 Flashcard Practice Improvements

### ✅ #19 — Flashcard correct answer earns +1 XP *(done)*
**Files:** `practice/PracticeSessionViewModel.kt`, `db/ProgressRepository.kt`

Each correct answer in any flashcard practice mode (Default, Listening, Reading) awards +1 XP to the student's total. Call `repository.saveProgress` or a lightweight XP-only increment so it doesn't overwrite scenario stars. Show a small "+1 XP" toast or badge animation on correct answer.

---

### ✅ #20 — Weak Word / Maintain mode shows only relevant star chips *(done)*
**Files:** `practice/PracticeScreen.kt`

When "Weak Words" mode is active, the star-rating distribution row should only display the star levels actually in the current pool (the 3 lowest). When "Maintain" is active, only show ★4+ levels in the pool. In "All Words" mode show all levels as today. Avoids confusion about stars that aren't being practised right now.

---

### ✅ #21 — Progress tab: word mastery counts per practice mode *(done)*
**Files:** `home/HomeScreen.kt` or `home/ProgressScreen.kt`, `db/ProgressRepository.kt`

In the Progress tab, alongside total mastered word count, show a breakdown:
- 🔊字 Default: X words at ★7+
- 🔊 Listening: X words at ★7+
- 字 Reading: X words at ★7+

Query using `getAllMasteredWords(type).filter { boxLevel >= 7 }` per `PracticeType`. Gives the parent and child a clear picture of multi-modal mastery.

---

### ✅ #22 — Scrollable multiple-choice answers in scenario conversation *(done)*
**Files:** `RolePlayScreen.kt`

When a dialogue step has many response options (4+), the options panel can overflow the screen. Wrap the options list in a `verticalScroll` so all choices are reachable. Also ensure a minimum touch-target height per button so small fingers can tap accurately.

---

### #23 — Scenario option wording audit: avoid negative/sad framing
**Files:** `app/src/main/assets/scenarios/*.json`

Review all `responseOption` text for unintentionally negative framing. Examples: "only have Papa" / "only have Mama" implies loneliness — rephrase to "just Papa and me" / "just Mama and me" or give the family a warm tone. Run a systematic pass over family/feelings scenarios. Goal: every option should feel positive or at least neutral for a 4–8 year old.

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

### #24 — Milestone reward: mastery targets per practice mode
**Files:** `parent/ParentDashboardScreen.kt`, `db/MilestoneReward.kt`, `db/ProgressRepository.kt`

Extend the Milestone Reward system so parents can set targets based on high-mastery (★10) word counts separately per mode:
- X words at ★10 in Default mode
- X words at ★10 in Listening mode
- X words at ★10 in Reading mode

Add new `MilestoneType` values: `MASTERY_DEFAULT`, `MASTERY_LISTENING`, `MASTERY_READING`.

---

### #25 — More badge / medal ideas
**Files:** `db/Badge.kt`

New badge concepts to implement:
- **Perfect Session** — score 100/100 correct in a single flashcard practice session
- **Listening Master** — reach ★7 in Listening mode for 10+ words
- **Reading Master** — reach ★7 in Reading mode for 10+ words
- **Triple Crown** — same word reaches ★7 in all 3 practice modes
- **Speed Learner** — complete 3 scenarios in a single day
- **Comeback Kid** — demoted a word then later promoted it back to its previous level
- **Consistency** — practice flashcards 5 days in a row

---

### #26 — Milestone rewards based on badges earned
**Files:** `parent/ParentDashboardScreen.kt`, `db/MilestoneReward.kt`

Add `MilestoneType.BADGE_COUNT` so parents can set a reward for earning N total badges (e.g. "Earn 5 badges → ice cream"). Display current badge count vs target in the reward card.

---

### #27 — Milestone rewards based on total XP
**Files:** `parent/ParentDashboardScreen.kt`, `db/MilestoneReward.kt`

Add `MilestoneType.TOTAL_XP` so parents can set XP thresholds as reward triggers (e.g. "Reach 500 XP → get a toy"). XP is already tracked — just add the new milestone type and check it after any XP gain.

---

### #28 — Milestone reward AND / OR condition logic
**Files:** `db/MilestoneReward.kt`, `parent/ParentDashboardScreen.kt`

Allow a reward to require multiple conditions to be met simultaneously (AND) or at least one condition from a set (OR). Examples:
- AND: "Earn 300 XP **and** complete 10 scenarios"
- OR: "Reach ★7 in Listening **or** Reading mode for 20 words"

Schema change: replace single `targetValue` + `milestoneType` with a `conditions: List<MilestoneCondition>` and a `logic: AND | OR` field. Requires DB migration.

---

### #29 — Navigate to scenario from Progress tab star list
**Files:** `home/HomeScreen.kt` (or `home/ProgressScreen.kt`), `navigation/AppNavigation.kt`

In the Progress tab where scenario stars are listed, make each scenario row tappable. Tapping navigates directly to that scenario's RolePlay screen (same as tapping it from the Learn tab). Useful for replaying a specific scenario to improve its star rating without hunting through categories.

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
