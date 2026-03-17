# UI/UX Audit Checklist — MiltonLearnMandarin

Use this checklist when running `/uiux` or auditing any screen.

## Layout & Spacing
- [ ] Screen-edge padding is 16dp consistently
- [ ] Card internal padding is 12dp or 16dp (not mixed)
- [ ] Elements separated by 8dp or 12dp (use `Arrangement.spacedBy`)
- [ ] No hard-coded pixel values — only `dp` and `sp`
- [ ] `fillMaxWidth()` on all cards, buttons, input fields

## Typography
- [ ] Chinese characters: minimum 18sp (prefer 20–28sp for primary content)
- [ ] Pinyin: 14sp minimum, coloured by tone via ToneUtils
- [ ] English/Indonesian labels: 12–14sp, `onSurfaceVariant` colour
- [ ] Section headers: `MaterialTheme.typography.titleMedium` or larger
- [ ] Button text: minimum 16sp

## Touch Targets
- [ ] All `IconButton`: minimum `Modifier.size(48.dp)`
- [ ] All `Button`: minimum `height(48.dp)`, prefer `height(56.dp)` for primary
- [ ] All `Card` with `onClick`: minimum 72dp height
- [ ] Pinyin word pills: `padding(horizontal = 6.dp, vertical = 2.dp)` minimum

## Colours
- [ ] Only use `MaterialTheme.colorScheme.*` tokens (except feedback colours)
- [ ] Correct feedback: `Color(0xFF4CAF50)` green
- [ ] Wrong feedback: `Color(0xFFF44336)` red
- [ ] Disabled state: `onSurface.copy(alpha = 0.38f)`
- [ ] No raw `Color.White` or `Color.Black` except as overlays

## Animation & Interaction
- [ ] State colour changes use `animateColorAsState` (not instant)
- [ ] Correct answer gets scale pop: `spring(DampingRatioMediumBouncy)`
- [ ] Speaking character has bounce animation
- [ ] Success result has emoji animation + optional confetti
- [ ] Button press has visual feedback (ripple from Material 3 by default)

## Accessibility
- [ ] Every `Icon` has a non-null `contentDescription`
- [ ] `Image` composables have descriptive `contentDescription`
- [ ] Semantic role set where needed (`Modifier.semantics { role = Role.Button }`)
- [ ] Screen titles in TopAppBar are descriptive

## Child-Specific UX
- [ ] Primary CTA is unmissable: large, coloured, full-width
- [ ] No more than 3 interactive choices visible at once (4 max for quiz options)
- [ ] Success feedback is immediate (<100ms visual, <300ms audio)
- [ ] Error feedback is non-punishing (gentle red, no harsh sounds... well, buzzer is OK)
- [ ] Emoji used as visual anchors on all major content blocks
- [ ] Text is positive and encouraging throughout
- [ ] Progress is visible (LinearProgressIndicator or step counter)

## Compose Code Quality
- [ ] Composables extracted if >80 lines or used in 2+ places
- [ ] No side effects inside composable body (use `LaunchedEffect`/`SideEffect`)
- [ ] `remember` wraps expensive computations
- [ ] `key` used on `LazyColumn`/`LazyRow` items
- [ ] `@Preview` annotation present on main composables (nice-to-have)

## Common Issues to Look For
- Missing `contentDescription` on icon buttons (Play, Back, Speed)
- Hardcoded colours instead of theme tokens
- Missing `fillMaxWidth` causing narrow layouts on large screens
- `Spacer(Modifier.height(X))` overuse — use `Arrangement.spacedBy` instead
- Text not adapting to system font scale (`sp` used, not `dp` for text)
- Dialog dismiss not handled (missing `onDismissRequest`)
- `LazyColumn` items without `key` — causes recomposition issues
