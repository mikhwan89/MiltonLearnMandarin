package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.R
import com.ikhwan.mandarinkids.ProgressManager
import com.ikhwan.mandarinkids.ToneUtils
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.MasteredWordEntity
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.ikhwan.mandarinkids.onboarding.LocalOnboardingCoords
import com.ikhwan.mandarinkids.onboarding.OnboardingKey
import com.ikhwan.mandarinkids.ui.theme.AppThemes
import com.ikhwan.mandarinkids.ui.theme.appColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (ScenarioCategory) -> Unit,
    showWordOfDayOnLaunch: Boolean = false,
    onWordOfDayShown: () -> Unit = {}
) {
    val context = LocalContext.current
    val scenarios = remember { JsonScenarioRepository.getAll() }
    val repo = remember { ProgressRepository.getInstance(context) }
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val tts = rememberTtsManager()
    val disabledCategories by userPrefs.disabledCategories.collectAsState(initial = emptySet())
    val disabledScenarios by userPrefs.disabledScenarios.collectAsState(initial = emptySet())

    val xp by repo.getTotalXp().collectAsState(initial = 0)
    val streak = remember { repo.getStreak() }
    val allMasteredWords by repo.getAllMasteredWords().collectAsState(initial = emptyList())

    val wordOfDay: MasteredWordEntity? = remember(allMasteredWords) {
        if (allMasteredWords.isEmpty()) null
        else repo.getOrPickWordOfDay(allMasteredWords)
    }

    var showWordOfDayDialog by remember { mutableStateOf(false) }

    // Show word-of-day popup automatically once per session
    LaunchedEffect(wordOfDay) {
        if (wordOfDay != null && showWordOfDayOnLaunch) {
            showWordOfDayDialog = true
            onWordOfDayShown()
        }
    }

    val activeCategories = remember(scenarios, disabledCategories, disabledScenarios) {
        ScenarioCategory.entries.filter { cat ->
            cat.name !in disabledCategories &&
            scenarios.any { it.category == cat && it.id !in disabledScenarios }
        }
    }

    val colors = MaterialTheme.appColors
    val scope = rememberCoroutineScope()
    val themeIndex by userPrefs.colorThemeIndex.collectAsState(initial = 0)
    val currentVariant = AppThemes[themeIndex.coerceIn(0, AppThemes.lastIndex)]
    val tourCoords = LocalOnboardingCoords.current

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("学中文", fontSize = 24.sp)
                        Text(
                            "Learn Mandarin for School",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        currentVariant.palette.tileBlue.start,
                                        currentVariant.palette.tileGreen.start
                                    )
                                )
                            )
                            .clickable {
                                val next = (themeIndex + 1) % AppThemes.size
                                scope.launch { userPrefs.saveColorThemeIndex(next) }
                            }
                            .onGloballyPositioned { lc ->
                                tourCoords[OnboardingKey.THEME_BUTTON] = lc.boundsInRoot()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentVariant.emoji, fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Progress summary — two gradient tiles ─────────────────────
            item {
                val streakGradient = colors.tileAmber.asList()
                val xpGradient = colors.tileBlue.asList()
                val statTextColor = colors.onLightTile

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { lc ->
                            tourCoords[OnboardingKey.STATS_ROW] = lc.boundsInRoot()
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val streakShape = RoundedCornerShape(20.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(streakShape)
                            .background(Brush.verticalGradient(streakGradient))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(streakIconRes(streak)),
                                contentDescription = "Day streak",
                                modifier = Modifier.size(52.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$streak",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = statTextColor
                            )
                            Text(
                                "day streak",
                                fontSize = 11.sp,
                                color = statTextColor.copy(alpha = 0.65f)
                            )
                        }
                    }
                    val xpShape = RoundedCornerShape(20.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(xpShape)
                            .background(Brush.verticalGradient(xpGradient))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(xpIconRes(xp)),
                                contentDescription = "XP",
                                modifier = Modifier.size(52.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$xp XP",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = statTextColor
                            )
                            Text(
                                ProgressManager.getLevelLabel(xp),
                                fontSize = 11.sp,
                                color = statTextColor.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            // ── Section header ────────────────────────────────────────────
            item {
                Text(
                    text = "Choose a Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // ── Category grid — 4 top row, 3 bottom row ───────────────────
            item {
                val rows = listOf(
                    activeCategories.take(4),
                    activeCategories.drop(4)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.onGloballyPositioned { lc ->
                        tourCoords[OnboardingKey.CATEGORY_GRID] = lc.boundsInRoot()
                    }
                ) {
                    rows.forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCategories.forEach { category ->
                                CategoryCard(
                                    category = category,
                                    onClick = { onCategoryClick(category) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Pad remaining slots in last row so cells stay same width
                            repeat(4 - rowCategories.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    // ── Word of the Day dialog (launch-time popup) ────────────────────────
    if (showWordOfDayDialog && wordOfDay != null) {
        WordOfDayDialog(
            word = wordOfDay,
            onDismiss = { showWordOfDayDialog = false },
            onPlay = { tts.speak(wordOfDay.chinese) }
        )
        LaunchedEffect(Unit) { tts.speak(wordOfDay.chinese) }
    }
}

@Composable
private fun WordOfDayDialog(
    word: MasteredWordEntity,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val dialogGradient = colors.tileBlue.asList()
    val labelColor = colors.contentColorFor(colors.tileBlue)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Box(modifier = Modifier.background(Brush.verticalGradient(dialogGradient))) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    Text(
                        "📅 Word of the Day",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        word.chinese,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = labelColor
                    )
                    Text(
                        ToneUtils.coloredAnnotatedPinyin(word.pinyin, colors),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text("🇬🇧  ${word.english}", fontSize = 16.sp,
                        textAlign = TextAlign.Center, color = labelColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "🇮🇩  ${word.indonesian}",
                        fontSize = 14.sp,
                        color = labelColor.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                    if (word.note != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.background(Brush.verticalGradient(colors.tileAmber.asList()))) {
                                Text(
                                    "💡 ${word.note}",
                                    fontSize = 13.sp,
                                    color = colors.contentColorFor(colors.tileAmber),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onPlay) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play word pronunciation")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play again")
                        }
                        TextButton(onClick = onDismiss) { Text("Close") }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    val colors = MaterialTheme.appColors
    val gradient = colors.tileBlue.asList()
    val textColor = colors.contentColorFor(colors.tileBlue)
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradient))
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

fun xpIconRes(xp: Int): Int = when {
    xp >= 50000 -> R.drawable.xp5
    xp >= 10000 -> R.drawable.xp4
    xp >= 1000  -> R.drawable.xp3
    xp >= 100   -> R.drawable.xp2
    else        -> R.drawable.xp1
}

fun streakIconRes(streak: Int): Int = when {
    streak >= 30 -> R.drawable.streak_30day
    streak >= 15 -> R.drawable.streak_15day
    streak >= 10 -> R.drawable.streak_10day
    streak >= 5  -> R.drawable.streak_5day
    else         -> R.drawable.streak_1day
}

fun badgeIconRes(badge: Badge): Int? = when (badge) {
    Badge.XP_SEEKER        -> R.drawable.xp2
    Badge.XP_HUNTER        -> R.drawable.xp3
    Badge.XP_LEGEND        -> R.drawable.xp4
    Badge.XP_MYTHICAL      -> R.drawable.xp5
    Badge.STREAK_STARTER   -> R.drawable.streak_5day
    Badge.STREAK_CHAMPION  -> R.drawable.streak_15day
    Badge.STREAK_LEGEND    -> R.drawable.streak_30day
    else                   -> null
}

fun categoryIconRes(category: ScenarioCategory): Int? = when (category) {
    ScenarioCategory.ESSENTIALS          -> R.drawable.cat_essential
    ScenarioCategory.AT_SCHOOL           -> R.drawable.cat_at_school
    ScenarioCategory.SCHOOL_SUBJECTS     -> R.drawable.cat_school_subjects
    ScenarioCategory.FOOD_AND_EATING     -> R.drawable.cat_food_and_eating
    ScenarioCategory.FEELINGS_AND_HEALTH -> R.drawable.cat_feelings_and_health
    ScenarioCategory.PLAY_AND_HOBBIES    -> R.drawable.cat_play_and_hobbies
    ScenarioCategory.HOME                -> R.drawable.cat_at_home
    ScenarioCategory.OUT_AND_ABOUT       -> R.drawable.cat_out_and_about
    else                                 -> null
}

@Composable
fun CategoryCard(category: ScenarioCategory, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColors
    val categoryGradient = colors.categoryGradient(category.name)
    val labelColor = colors.contentColorFor(categoryGradient)
    val categoryIcon = categoryIconRes(category)
    val shape = RoundedCornerShape(20.dp)
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(categoryGradient.asList()), shape = shape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                if (categoryIcon != null) {
                    Image(
                        painter = painterResource(categoryIcon),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(category.emoji, fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = labelColor,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ScenarioCard(
    scenario: Scenario,
    starsAtCurrentLevel: Int = 0,
    everPlayed: Boolean = false,
    masteryLevel: Int = 1,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val cardGradient = colors.tileAmber.asList()
    val labelColor = colors.contentColorFor(colors.tileAmber)
    // New level pending = played before but no stars yet on this level
    val newLevelPending = everPlayed && starsAtCurrentLevel == 0

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(cardGradient))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character emoji with level badge overlaid at top-right
                Box(
                    modifier = Modifier.padding(end = 16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = scenario.characterEmoji,
                        fontSize = 48.sp,
                        modifier = Modifier.padding(top = 8.dp, end = 4.dp)
                    )
                    if (everPlayed) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = when {
                                masteryLevel >= 5 -> Color(0xFFFFC107)
                                masteryLevel >= 3 -> MaterialTheme.colorScheme.primary
                                masteryLevel >= 2 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ) {
                            Text(
                                text = "Lv.$masteryLevel",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scenario.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = labelColor
                    )
                    Text(
                        text = scenario.description,
                        fontSize = 13.sp,
                        color = labelColor.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    when {
                        !everPlayed -> Text(
                            text = "Not played yet",
                            fontSize = 12.sp,
                            color = labelColor.copy(alpha = 0.65f)
                        )
                        newLevelPending -> Row {
                            // Empty stars in amber — new level unlocked, waiting to be played
                            repeat(3) {
                                Text("☆", fontSize = 20.sp, color = colors.starFilled)
                            }
                        }
                        else -> Row {
                            repeat(3) { i ->
                                Text(
                                    text = if (i < starsAtCurrentLevel) "★" else "☆",
                                    fontSize = 20.sp,
                                    color = if (i < starsAtCurrentLevel) colors.starFilled else colors.starEmpty
                                )
                            }
                        }
                    }
                }
                Text("▶", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
