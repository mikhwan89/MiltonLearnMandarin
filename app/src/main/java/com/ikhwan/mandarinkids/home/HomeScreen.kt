package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val statTextColor  = if (isDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27)
                val streakGradient = if (isDark)
                    listOf(Color(0xFF6B5208), Color(0xFF4E3C06))
                else
                    listOf(Color(0xFFFFF0B3), Color(0xFFFFF8D9))
                val xpGradient = if (isDark)
                    listOf(Color(0xFF1A3D6E), Color(0xFF0F2A50))
                else
                    listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val dialogGradient = if (isDark) listOf(Color(0xFF1A3D6E), Color(0xFF0F2A50))
                         else        listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))
    val labelColor = if (isDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27)
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
                        ToneUtils.coloredAnnotatedPinyin(word.pinyin),
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
                            Box(modifier = Modifier.background(Brush.verticalGradient(
                                if (isDark) listOf(Color(0xFF6B5208), Color(0xFF4E3C06))
                                else        listOf(Color(0xFFFFF0B3), Color(0xFFFFF8D9))
                            ))) {
                                Text(
                                    "💡 ${word.note}",
                                    fontSize = 13.sp,
                                    color = labelColor,
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
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val gradient = if (isDark) listOf(Color(0xFF1A3D6E), Color(0xFF0F2A50))
                   else        listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))
    val textColor = if (isDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27)
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
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val gradientColors = if (isDark) {
        when (category) {
            ScenarioCategory.ESSENTIALS          -> listOf(Color(0xFF1A3D6E), Color(0xFF0F2A50))
            ScenarioCategory.AT_SCHOOL           -> listOf(Color(0xFF1A4E30), Color(0xFF10382A))
            ScenarioCategory.SCHOOL_SUBJECTS     -> listOf(Color(0xFF342670), Color(0xFF261B55))
            ScenarioCategory.FOOD_AND_EATING     -> listOf(Color(0xFF7A4210), Color(0xFF5C3008))
            ScenarioCategory.FEELINGS_AND_HEALTH -> listOf(Color(0xFF7A1830), Color(0xFF5C1024))
            ScenarioCategory.PLAY_AND_HOBBIES    -> listOf(Color(0xFF1A4E28), Color(0xFF103818))
            ScenarioCategory.HOME                -> listOf(Color(0xFF1A4558), Color(0xFF0F3242))
            ScenarioCategory.OUT_AND_ABOUT       -> listOf(Color(0xFF6B5208), Color(0xFF4E3C06))
            else                                 -> listOf(Color(0xFF2A2A2A), Color(0xFF222222))
        }
    } else {
        when (category) {
            ScenarioCategory.ESSENTIALS          -> listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))
            ScenarioCategory.AT_SCHOOL           -> listOf(Color(0xFFD4EDD0), Color(0xFFE6F4E4))
            ScenarioCategory.SCHOOL_SUBJECTS     -> listOf(Color(0xFFE8E4F5), Color(0xFFF2EFF9))
            ScenarioCategory.FOOD_AND_EATING     -> listOf(Color(0xFFFFDDB5), Color(0xFFFFEDD4))
            ScenarioCategory.FEELINGS_AND_HEALTH -> listOf(Color(0xFFF5E0E0), Color(0xFFFAEEEE))
            ScenarioCategory.PLAY_AND_HOBBIES    -> listOf(Color(0xFFD5EDD5), Color(0xFFE6F5E6))
            ScenarioCategory.HOME                -> listOf(Color(0xFFB8E4F0), Color(0xFFD4EFF8))
            ScenarioCategory.OUT_AND_ABOUT       -> listOf(Color(0xFFFFF0B3), Color(0xFFFFF8D9))
            else                                 -> listOf(Color(0xFFF5F4ED), Color(0xFFF5F4ED))
        }
    }
    val labelColor = if (isDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27)
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
                .background(Brush.verticalGradient(gradientColors), shape = shape),
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
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun ScenarioCard(scenario: Scenario, stars: Int, onClick: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val cardGradient = if (isDark) listOf(Color(0xFF6B5208), Color(0xFF4E3C06))
                       else        listOf(Color(0xFFFFF0B3), Color(0xFFFFF8D9))
    val labelColor = if (isDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27)
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
                Text(
                    text = scenario.characterEmoji,
                    fontSize = 48.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
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
                    if (stars > 0) {
                        Row {
                            repeat(3) { i ->
                                Text(
                                    text = if (i < stars) "★" else "☆",
                                    fontSize = 20.sp,
                                    color = if (i < stars) Color(0xFFFFC107) else Color(0xFFBDBDBD)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Not played yet",
                            fontSize = 12.sp,
                            color = labelColor.copy(alpha = 0.65f)
                        )
                    }
                }
                Text("▶", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
