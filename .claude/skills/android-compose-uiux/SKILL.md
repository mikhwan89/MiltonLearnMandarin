---
name: android-compose-uiux
description: This skill should be used when the user asks to "improve the UI", "fix the design", "make it look better", "improve UX", "improve the layout", "review the screen", "audit the interface", "make it more child-friendly", "improve accessibility", "fix spacing", "add animation", "improve colors", "redesign", "polish the app", or discusses visual design, user experience, Jetpack Compose styling, Material Design 3, component design, touch targets, readability, or anything related to how the app looks and feels.
version: 1.0.0
---

# Android Compose UI/UX Skill

This skill guides all UI/UX work in the MiltonLearnMandarin Android app — a Jetpack Compose + Material Design 3 app built for children aged 5 and up.

## App Context

- **Audience:** Children aged 5+. Primary user is Milton (developer's son). Parents may also interact.
- **Purpose:** Interactive Mandarin learning via role-play scenarios and quizzes.
- **UI stack:** 100% Jetpack Compose, Material Design 3, no XML layouts.
- **Key screens:** HomeScreen, RolePlayScreen, QuizScreen, PhrasesScreen, QuizResultsScreen.
- **Key components:** ScenarioCard, ConversationBubble, ResponseOptionButton, QuizOptionButton, FeedbackCard.

---

## Design Principles for This App

### 1. Child-First Design
- **Touch targets:** Minimum 48×48dp for all tappable elements. Prefer 56dp+ for primary actions.
- **Typography:** Use large, bold text. Characters/Chinese text at 18sp+. Pinyin/labels at 14sp minimum.
- **Simplicity:** One primary action per screen. No information overload.
- **Delight:** Animations, emoji, colours, and sound feedback reward interactions.
- **Language clarity:** Every Chinese string must be accompanied by pinyin and an English/Indonesian translation.

### 2. Material Design 3 in Compose
- Use `MaterialTheme.colorScheme.*` tokens — never hardcode colours except for semantic feedback (green for correct, red for wrong).
- Use `MaterialTheme.typography.*` for text styles.
- Prefer `Card`, `Surface`, `Button`, `OutlinedButton`, `IconButton` from Material 3.
- Use `shape = RoundedCornerShape(...)` consistently — prefer 12–16dp for cards, 50% for badges/pills.
- Use `ElevatedCard` or `shadowElevation` to create visual hierarchy.

### 3. Colour Usage
- Correct answers / success: `Color(0xFF4CAF50)` green.
- Wrong answers / error: `Color(0xFFF44336)` red.
- Stars: `Color(0xFFFFC107)` amber, `Color(0xFFBDBDBD)` grey for empty.
- XP gain: `Color(0xFF4CAF50)` green, bold.
- Tone colours (pinyin): defined in `ToneUtils` — respect these for consistency.
- Everything else: use `MaterialTheme.colorScheme.*`.

### 4. Animation & Feedback
- Use `animateFloatAsState` / `animateColorAsState` for smooth state transitions.
- Bounce animations (`infiniteRepeatable` + `RepeatMode.Reverse`) are used for speaking character emoji and perfect-score results — keep this pattern.
- `spring(dampingRatio = Spring.DampingRatioMediumBouncy)` for "pop" effects on correct answers.
- Sound feedback: `playSuccessSound()` for correct answers, `playWrongSound()` for wrong.

### 5. Spacing & Layout
- Consistent padding: `16.dp` for screen edges, `12.dp` inside cards, `8.dp` between elements.
- Use `Arrangement.spacedBy(...)` in `Column`/`Row` instead of individual `Spacer`s where items repeat.
- `fillMaxWidth()` on all cards and buttons.
- `Box + Alignment.BottomCenter` for overlay panels (options panel over conversation).

---

## Screen-by-Screen Guidelines

### HomeScreen
- Scenario cards must be visually distinct per category (use `characterEmoji` prominently).
- Show star rating and category label on each card.
- Clear visual hierarchy: category header → cards in that category.

### RolePlayScreen
- Character emoji bounces while speaking (`isSpeaking` animation).
- Character bubbles: `secondaryContainer` colour, left-aligned.
- Student bubbles: `primaryContainer` colour, right-aligned.
- Pinyin words rendered as tappable coloured pills (tone colours from `ToneUtils`).
- Options panel docked at bottom with `shadowElevation = 8.dp`.
- Speed toggle (🐢/⚡) always visible in top bar.

### QuizScreen
- Question card in `primaryContainer`.
- Option buttons: clear number badge, large Chinese or translation text.
- Correct = green highlight + ✅ + scale pop animation.
- Wrong = red highlight + ❌ on selected, green highlight on correct.
- `FeedbackCard` appears with explanation after any selection.

### QuizResultsScreen
- Trophy/emoji at top, animated bounce for perfect score.
- Stars (★★★) prominent — children love seeing stars.
- XP gain in bold green.
- "Try Again" and "Back to Home" both prominent.
- Confetti (`ConfettiEffect`) for perfect scores only.

---

## Common UI/UX Improvement Patterns

When reviewing or improving screens, check for these:

### Accessibility
- [ ] All icons have `contentDescription`.
- [ ] Touch targets ≥ 48dp.
- [ ] Text contrast ratio ≥ 4.5:1 (MD3 handles most of this with `colorScheme` tokens).
- [ ] Chinese text always accompanied by pinyin.

### Visual Polish
- [ ] Consistent corner radii (12–16dp cards, 50% badges).
- [ ] `animateColorAsState` on interactive elements instead of instant colour change.
- [ ] Loading/transition states (if async) have `CircularProgressIndicator`.
- [ ] Empty states have an emoji + friendly message.

### Child UX
- [ ] Primary CTA button is `height = 56.dp`, `fillMaxWidth`, prominent colour.
- [ ] No more than 3–4 options on any screen at once.
- [ ] Positive reinforcement copy ("Great job!", "完美！", "Keep practicing!", "加油！").
- [ ] Emoji used liberally as visual anchors.
- [ ] Animations on success/completion moments.

### Compose Best Practices
- [ ] `remember` expensive computations.
- [ ] `key = ...` on `LazyColumn` items for stable identity.
- [ ] Avoid deep nesting — extract composables when nesting exceeds 4 levels.
- [ ] Use `Modifier.padding` before `Modifier.fillMaxSize` (order matters).
- [ ] `fillMaxWidth()` on all form-like elements.

---

## How to Approach a UI/UX Task

1. **Read the target screen's Kotlin file first** — understand current state before suggesting changes.
2. **Identify the specific composables** that need changes.
3. **Reference existing patterns** in the codebase (e.g., how `QuizOptionButton` handles colour animation) before introducing new ones.
4. **Check references/** folder in this skill for detailed checklists and patterns.
5. **Propose changes incrementally** — don't rewrite a whole screen unless asked.
6. **Test mentally**: for each change, imagine a 5-year-old using it. Is it obvious? Is the tap target big enough? Is the feedback immediate?

---

## Files to Read for Context

When working on UI/UX, the most relevant files are:

| File | What it contains |
|------|-----------------|
| `app/src/main/java/com/ikhwan/mandarinkids/MainActivity.kt` | HomeScreen, ScenarioCard, navigation |
| `app/src/main/java/com/ikhwan/mandarinkids/RolePlayScreen.kt` | Conversation flow, options panel |
| `app/src/main/java/com/ikhwan/mandarinkids/ConversationBubble.kt` | Chat bubble, word detail dialog |
| `app/src/main/java/com/ikhwan/mandarinkids/QuizScreen.kt` | Quiz layout, QuizOptionButton |
| `app/src/main/java/com/ikhwan/mandarinkids/FeedbackCard.kt` | Answer feedback |
| `app/src/main/java/com/ikhwan/mandarinkids/QuizResultsScreen.kt` | Results, stars, XP, confetti |
| `app/src/main/java/com/ikhwan/mandarinkids/ui/theme/Theme.kt` | MD3 theme setup |
| `app/src/main/java/com/ikhwan/mandarinkids/ui/theme/Color.kt` | Colour palette |
| `app/src/main/java/com/ikhwan/mandarinkids/ToneUtils.kt` | Pinyin tone colour logic |
