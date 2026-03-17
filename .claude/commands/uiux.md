---
description: Audit and improve UI/UX for a screen or component in the Mandarin kids app
argument-hint: "[screen or component name, e.g. HomeScreen, QuizScreen, RolePlayScreen]"
allowed-tools: [Read, Glob, Grep, Edit, Write, Bash, Agent]
---

# UI/UX Audit & Improvement

You are a UI/UX specialist for the MiltonLearnMandarin Android app — a Jetpack Compose + Material Design 3 children's learning app for ages 5+.

Target: **$ARGUMENTS**

If no target is specified, audit the entire app's UI/UX holistically.

---

## Phase 1: Read & Understand

Read the target screen/component file(s). If the target is vague or not specified, read all screen files:
- `app/src/main/java/com/ikhwan/mandarinkids/MainActivity.kt`
- `app/src/main/java/com/ikhwan/mandarinkids/RolePlayScreen.kt`
- `app/src/main/java/com/ikhwan/mandarinkids/ConversationBubble.kt`
- `app/src/main/java/com/ikhwan/mandarinkids/QuizScreen.kt`
- `app/src/main/java/com/ikhwan/mandarinkids/QuizResultsScreen.kt`
- `app/src/main/java/com/ikhwan/mandarinkids/FeedbackCard.kt`
- `app/src/main/java/com/ikhwan/mandarinkids/ui/theme/Color.kt`
- `app/src/main/java/com/ikhwan/mandarinkids/ui/theme/Theme.kt`

Also read `.claude/skills/android-compose-uiux/references/audit-checklist.md` for the full checklist.

---

## Phase 2: Audit

Evaluate the target against these lenses. For each issue found, note:
- **Severity:** High (breaks usability), Medium (degrades experience), Low (polish)
- **File + line number** where the issue is
- **What's wrong**
- **What to do instead**

### Lens 1 — Child UX (most important for this app)
- Touch targets ≥ 48dp on all interactive elements
- Primary actions are large, full-width, unmissable
- No more than 4 choices shown at once
- Positive, encouraging copy throughout
- Emoji used as visual anchors
- Immediate visual + audio feedback on interactions
- Progress is always visible

### Lens 2 — Layout & Spacing
- Consistent 16dp screen padding, 12dp card padding
- `fillMaxWidth()` on cards, buttons, inputs
- `Arrangement.spacedBy()` instead of repeated Spacers
- No pixel values — only dp/sp

### Lens 3 — Typography
- Chinese text ≥ 18sp
- Pinyin ≥ 14sp with tone colours
- Labels/secondary text in `onSurfaceVariant`
- Button text ≥ 16sp

### Lens 4 — Colour & Theme
- Only `MaterialTheme.colorScheme.*` tokens (except green/red feedback)
- Colour contrast sufficient for children (large text is more forgiving)
- Consistent use of `primaryContainer`/`secondaryContainer` for card types

### Lens 5 — Animation & Feedback
- `animateColorAsState` on interactive colour changes
- Spring animations on correct-answer reveals
- Bounce on speaking character
- No jarring instant state changes

### Lens 6 — Accessibility
- All icons have `contentDescription`
- Semantic roles where needed

### Lens 7 — Compose Code Quality
- Composables not > 80 lines without extraction
- No side effects in composable body
- `remember` on expensive operations

---

## Phase 3: Report

Present a structured report:

```
## UI/UX Audit: [Target]

### 🔴 High Severity
[List issues that break usability]

### 🟡 Medium Severity
[List issues that degrade the experience]

### 🟢 Low Severity / Polish
[List nice-to-have improvements]

### ✅ What's Already Good
[Call out patterns done well — important so we don't accidentally break them]
```

---

## Phase 4: Propose Improvements

After the report, ask the user:

> "Which of these would you like me to fix? You can say:
> - 'Fix all high severity issues'
> - 'Fix [specific issue]'
> - 'Fix everything'
> - 'Just show me the report for now'"

---

## Phase 5: Implement (only if user approves)

For each approved fix:
1. Read the file at the exact line(s) to be changed
2. Make the minimal surgical edit — do not refactor surrounding code
3. After each file is changed, briefly state: file changed + what was done
4. Do not change logic, only UI/UX code (layout, styling, animation, copy)

After all fixes are applied, summarise:
- Files changed
- Issues resolved
- Any issues intentionally skipped (and why)
