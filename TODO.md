# MiltonLearnMandarin — UI/UX Improvement Backlog

Inspired by Duolingo, Drops, and other top language learning apps.
Ordered roughly by impact. Each item includes the target file(s).

---

## 🏠 Home Screen Redesign

### ✅ #1 — Remove badge row from Home *(done)*
**Files:** `home/HomeScreen.kt`

The badge chips clutter the home screen. Badges should live on a dedicated Progress tab.
Remove the `BadgeChip` row and the wrapping card entirely from `HomeScreen`.

---

### ✅ #2 — Convert Word of Day to launch-time popup (not a persistent card) *(done)*
**Files:** `home/HomeScreen.kt`, `MainActivity.kt`

Word of Day should appear once as a `Dialog` / `BottomSheet` when the app opens — then dismiss and never reappear until tomorrow. Remove the always-visible `WordOfDayCard` from the home LazyColumn.
Use a `LaunchedEffect(Unit)` in `MandarinKidsApp` to show it once per session.

---

### ✅ #3 — Add bottom navigation bar (Learn / Flashcard / Progress) *(done)*
**Files:** `navigation/AppNavigation.kt`, `navigation/Routes.kt`

Replace the single-screen home with a 3-tab bottom nav:
- **Learn** — scenario list (current home content, cleaned up)
- **Flashcard** — dedicated flashcard/drill mode with 4-option multiple choice
- **Progress** — XP, streak, stars per scenario, badges, milestone rewards

Use `NavigationBar` + `NavigationBarItem` (MD3). This mirrors Duolingo's tab structure and
clearly separates the two different activity modes the app already has.

---

### ✅ #4 — Add breathing room to scenario cards *(done)*
**Files:** `home/HomeScreen.kt`

- Increase card internal padding 12dp → 16dp
- Add `verticalArrangement = Arrangement.spacedBy(12.dp)` between cards
- Reduce text density on each card: show emoji + title + star rating only
- Move category label to the section header; remove XP number from the card

---

### ✅ #5 — Replace plain section headers with bold category dividers *(done)*
**Files:** `home/HomeScreen.kt`, `home/ScenarioListScreen.kt`

Each category section gets a `MaterialTheme.typography.titleLarge` header with a category emoji
and a `primaryContainer` background strip — not just a plain `Text()` label.

---

## 🎮 Learn vs Practice Split

### ✅ #6 — Dedicated Practice tab screen *(done)*
**Files:** `practice/PracticeScreen.kt`, `practice/PracticeSessionViewModel.kt`

Currently practice mode is a banner buried below scenario content. Give it a proper full screen:
- Top: category/scenario picker or "all words" mode
- Main: one flashcard at a time, large tap targets
- Progress bar showing cards remaining in session

---

### ✅ #7 — Visual distinction between Learn and Practice modes *(done)*
**Files:** `navigation/AppNavigation.kt`, `practice/PracticeScreen.kt`

- **Learn tab** (scenario mode): School icon 🏫, warm `primaryContainer` tone
- **Flashcard tab** (flashcard drill): Style/cards icon 🃏, cool `secondaryContainer` TopAppBar

Make it visually obvious to a 5-year-old that these are different activities.

---

## 💬 RolePlay Screen Polish

### ✅ #8 — Character emoji bounce during speaking *(done)*
**Files:** `ConversationBubble.kt`

Add `infiniteRepeatable` bounce on the character emoji when `isSpeaking = true`.
Use `animateFloatAsState` → `graphicsLayer { translationY = bounce }` with
`spring(DampingRatioMediumBouncy)` for a gentle bob.

---

### ✅ #9 — Animate options panel sliding up from bottom *(done)*
**Files:** `RolePlayScreen.kt`

When `vm.showOptions` becomes true, wrap the options `Surface` in:
```kotlin
AnimatedVisibility(
    visible = vm.showOptions,
    enter = slideInVertically { it } + fadeIn(),
    exit = slideOutVertically { it } + fadeOut()
)
```

---

### ✅ #10 — Larger response option touch targets + bigger Chinese text *(done)*
**Files:** `RolePlayScreen.kt` (`ResponseOptionButton`)

- Min height: 72dp → **80dp**
- Number badge: 36dp → **40dp**
- Chinese text: 16sp → **18sp**
- `animateColorAsState` on `isPressed` container colour (replace instant switch):
```kotlin
val containerColor by animateColorAsState(
    if (isPressed) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface
)
```

---

## 🧠 Quiz Screen Polish

### ✅ #11 — Scale pop animation on correct answer *(done)*
**Files:** `QuizScreen.kt` (`QuizOptionButton`)

On correct selection, animate scale 1.0 → 1.08 → 1.0:
```kotlin
val scale by animateFloatAsState(
    if (isCorrectAndSelected) 1.08f else 1.0f,
    animationSpec = spring(Spring.DampingRatioMediumBouncy)
)
Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
```

---

### ✅ #12 — Enforce min height on quiz option buttons + larger Chinese text *(done)*
**Files:** `QuizScreen.kt` (`QuizOptionButton`)

Add `Modifier.heightIn(min = 72.dp)` on each option button.
Chinese text in options: minimum **18sp**.

---

## 🏆 Results Screen Polish

### ✅ #13 — Bounce animation on trophy/emoji for any result (not just confetti) *(done)*
**Files:** `QuizResultsScreen.kt`

Confetti already fires on perfect score. For non-perfect results, add a gentle
`infiniteRepeatable` bounce on the result emoji so the screen never feels static.

---

### ✅ #14 — CTA button sizing on results screen *(done)*
**Files:** `QuizResultsScreen.kt`

- Primary CTA (Continue / Back to Home): `height = 56.dp`, `fillMaxWidth`, `Button`
- Secondary CTA (Try Again): `OutlinedButton`, same width, below primary
- Ensure 12dp gap between the two buttons

---

## 🎨 Global Visual Polish

### #15 — Audit all Icon composables for missing contentDescription
**Files:** all screen files

Every `Icon` must have a non-null descriptive `contentDescription`.
Known gaps to check: Speed toggle icon, Play buttons in `ConversationBubble`, Back arrow.
Run `/uiux` for a full pass.

---

### #16 — Replace Spacer chains with Arrangement.spacedBy
**Files:** `RolePlayScreen.kt`, `QuizScreen.kt`, `home/HomeScreen.kt`

Anywhere 3+ `Spacer(Modifier.height(X))` appear in a `Column`, switch to
`Column(verticalArrangement = Arrangement.spacedBy(Xdp))`.

---

### #17 — Consistent corner radii across all cards
**Files:** all screen files

- Cards: `RoundedCornerShape(16.dp)`
- Badges / pills: `RoundedCornerShape(50)`
- Buttons: default MD3 (no override needed)

Audit for any cards still using 8dp or 12dp.

---

### #18 — Move XP + streak + badges to dedicated Progress tab
**Files:** new `ProgressScreen.kt`

A dedicated screen:
- Streak counter 🔥 N days — large, prominent
- Total XP — large number
- Per-scenario star grid (all scenarios with ★★★ display)
- Badge collection (currently buried on home)

This clears the remaining clutter off the Learn tab home.

---

## 🔤 Typography Audit

### #19 — Chinese text size audit across all screens
**Files:** all screen files

- Primary Chinese display: minimum **20sp**
- Secondary / option Chinese text: minimum **18sp**
- Pinyin below Chinese: minimum **14sp**
- Run `/uiux` on each screen to catch violations

---

### #20 — Verify pinyin tone colours in ConversationBubble
**Files:** `ConversationBubble.kt`

Confirm word pills use `ToneUtils` colours for each syllable.
Pill padding: `horizontal = 8.dp, vertical = 4.dp` minimum (up from 6/2).

---

## Notes

- Run `/uiux [ScreenName]` to perform a full 7-lens audit before implementing any screen's changes.
- UI-only edits — do not change game logic, data models, or TTS behaviour.
- Test each change mentally: *Can a 5-year-old tap this without frustration? Is feedback immediate?*
- Items #1–3 (bottom nav + home clean-up) are the highest-leverage changes and should be done first.
