package com.ikhwan.mandarinkids.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.R
import com.ikhwan.mandarinkids.ToneUtils
import kotlinx.coroutines.launch

// ── Data ──────────────────────────────────────────────────────────────────────

private data class OnboardingPage(
    val iconRes: Int,
    val title: String,
    val subtitle: String,
    val steps: List<Pair<String, String>>,  // icon emoji → description
    val gradientTop: Color
)

private val PAGES = listOf(
    OnboardingPage(
        iconRes = R.drawable.nav_roleplay,
        title = "Learn with Role-Play!",
        subtitle = "Have real Mandarin conversations in fun, everyday scenarios",
        steps = listOf(
            "📚" to "Choose a category — School, Food, Home, Out & About and more",
            "🎯" to "Pick a scenario to practise",
            "💬" to "Have a step-by-step Mandarin conversation",
            "✅" to "Take a quiz to test what you learned and earn stars!"
        ),
        gradientTop = Color(0xFFD0E8F8)
    ),
    OnboardingPage(
        iconRes = R.drawable.nav_flashcard,
        title = "Master Mandarin Words!",
        subtitle = "Build vocabulary through flashcard practice",
        steps = listOf(
            "👁️" to "Review words from scenarios you've played",
            "🎛️" to "Pick your mode: Listening only 👂, Reading only 👀, or Both",
            "⬆️" to "Correct answer → word star rating goes up",
            "⬇️" to "Wrong answer → star goes down — keep practising!"
        ),
        gradientTop = Color(0xFFD4EDD0)
    ),
    OnboardingPage(
        iconRes = R.drawable.nav_tone,
        title = "Tones Change the Meaning!",
        subtitle = "In Mandarin, the same sound with a different tone = a completely different word",
        steps = listOf(
            "🔴" to "mā (妈) — Tone 1: high & flat → MOTHER",
            "🟠" to "má (麻) — Tone 2: rising → NUMB / HEMP",
            "🟢" to "mǎ (马) — Tone 3: dip then rise → HORSE",
            "🔵" to "mà (骂) — Tone 4: falling → TO SCOLD"
        ),
        gradientTop = Color(0xFFE8E4F5)
    ),
    OnboardingPage(
        iconRes = R.drawable.nav_build,
        title = "Build Sentences!",
        subtitle = "Arrange word tiles to form correct Mandarin sentences",
        steps = listOf(
            "🔀" to "Scrambled word tiles appear on screen",
            "👆" to "Tap them in the right order to build the sentence",
            "💡" to "Practice Mandarin grammar and word order",
            "🚀" to "Each sentence you crack makes you stronger!"
        ),
        gradientTop = Color(0xFFFFDDB5)
    ),
    OnboardingPage(
        iconRes = R.drawable.nav_progress,
        title = "Track Your Journey!",
        subtitle = "See how far you've come and unlock amazing rewards",
        steps = listOf(
            "🏅" to "Earn badges as you play — Streak Champion, Grand Master & more",
            "⭐" to "Track your star rating on every scenario",
            "🌙" to "Switch between Light and Dark mode in Progress",
            "🔒" to "Parents: tap Parental Control to set real-life rewards & manage content"
        ),
        gradientTop = Color(0xFFF5E0E0)
    )
)

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()

    val currentPage = pagerState.currentPage
    val targetTop = PAGES[currentPage].gradientTop

    val animatedTop by animateColorAsState(
        targetValue = targetTop,
        animationSpec = tween(durationMillis = 400),
        label = "gradient_top"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(animatedTop, Color.White)))
    ) {
        // Skip
        TextButton(
            onClick = onComplete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp)
        ) {
            Text(
                "Skip",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp
            )
        }

        // Pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 168.dp)
        ) { index ->
            PageContent(page = PAGES[index])
        }

        // Dots + CTA
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DotsIndicator(
                count = PAGES.size,
                current = currentPage
            )

            val isLast = currentPage == PAGES.size - 1
            Button(
                onClick = {
                    if (isLast) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isLast) "Let's Start! 🚀" else "Next →",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Page content ──────────────────────────────────────────────────────────────

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(page.iconRes),
            contentDescription = page.title,
            modifier = Modifier.size(96.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = page.subtitle,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            page.steps.forEachIndexed { index, (icon, text) ->
                StepRow(icon = icon, text = text, isTonePage = page.iconRes == R.drawable.nav_tone, stepIndex = index)
            }
        }
    }
}

// ── Step row ──────────────────────────────────────────────────────────────────

@Composable
private fun StepRow(icon: String, text: String, isTonePage: Boolean, stepIndex: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon bubble
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 18.sp)
            }
        }

        // Text — tone page gets colored pinyin annotations
        if (isTonePage) {
            Text(
                text = toneAnnotated(text, stepIndex),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Returns an [AnnotatedString] for tone page steps, colouring the pinyin syllable
 * at the start of each step text using [ToneUtils.toneColor].
 *
 * Each step text is in the form: "mā (妈) — Tone 1: …"
 * We detect the tone from the pinyin and colour just the leading "mX (妈)" part.
 */
private fun toneAnnotated(text: String, stepIndex: Int): AnnotatedString {
    // tones in order: 1=mā, 2=má, 3=mǎ, 4=mà
    val toneNumber = stepIndex + 1  // steps 0–3 → tones 1–4
    val color = ToneUtils.toneColor(toneNumber)

    // Split at " — " to colour only the left side (pinyin + character)
    val splitIdx = text.indexOf(" — ")
    return if (splitIdx < 0) {
        buildAnnotatedString { append(text) }
    } else {
        val pinyinPart = text.substring(0, splitIdx)
        val restPart = text.substring(splitIdx)
        buildAnnotatedString {
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                append(pinyinPart)
            }
            append(restPart)
        }
    }
}

// ── Dots indicator ────────────────────────────────────────────────────────────

@Composable
private fun DotsIndicator(count: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val isSelected = index == current
            val dotWidth by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 250),
                label = "dot_w"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
            )
        }
    }
}
