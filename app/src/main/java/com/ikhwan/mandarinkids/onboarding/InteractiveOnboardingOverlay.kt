package com.ikhwan.mandarinkids.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.R

// ── Tour step data ─────────────────────────────────────────────────────────

private data class CoachStep(
    val keys: List<String> = emptyList(), // spotlight keys; empty = no spotlight (Welcome / Outro)
    val emoji: String,              // fallback when iconRes is null
    val iconRes: Int? = null,       // drawable resource; shown instead of emoji when set
    val navigateToRoute: String? = null,  // route to navigate to when this step becomes active
    val title: String,
    val message: String,
)

private val STEPS = listOf(
    CoachStep(
        emoji   = "👋",
        title   = "Welcome to Mandarinku!",
        message = "Let's take a quick tour so you know your way around. Tap Next to begin!"
    ),
    CoachStep(
        keys            = listOf(OnboardingKey.THEME_BUTTON, OnboardingKey.NAV_ROLEPLAY),
        emoji           = "🎨",
        navigateToRoute = "home",
        title           = "Pick Your Theme",
        message         = "Tap this button to cycle through 10 colour themes — light and dark. Find the one you love most!"
    ),
    CoachStep(
        keys            = listOf(OnboardingKey.STATS_ROW, OnboardingKey.NAV_ROLEPLAY),
        emoji           = "🔥",
        navigateToRoute = "home",
        title           = "Your Progress",
        message         = "Your daily streak and total XP live here. Come back every day to keep the streak alive and gain more experience points!"
    ),
    CoachStep(
        keys            = listOf(OnboardingKey.CATEGORY_GRID, OnboardingKey.NAV_ROLEPLAY),
        emoji           = "🗣️",
        iconRes         = R.drawable.nav_roleplay,
        navigateToRoute = "home",
        title           = "Practice Mandarin Conversations",
        message         = "Choose a real-life conversation scenario in Mandarin that you'd like to practice — from greetings and school life to food, home, and community. Tap any category to get started!"
    ),
    CoachStep(
        keys            = listOf(OnboardingKey.NAV_FLASHCARD),
        emoji           = "🃏",
        iconRes         = R.drawable.nav_flashcard,
        navigateToRoute = "practice",
        title           = "Flashcard Practice",
        message         = "Practice memorising words you've encountered in role-play scenarios. Filter by category, switch between Listening and Reading modes, and choose to drill weak words or maintain the ones you already know well."
    ),
    CoachStep(
        keys            = listOf(OnboardingKey.NAV_TONE),
        emoji           = "🎵",
        iconRes         = R.drawable.nav_tone,
        navigateToRoute = "tone_trainer",
        title           = "Tone Practice",
        message         = "In Mandarin, the same sound with a different tone is a completely different word! Train your ear on the four Mandarin tones and sharpen your listening skills."
    ),
    CoachStep(
        keys            = listOf(OnboardingKey.NAV_BUILD),
        emoji           = "🧱",
        iconRes         = R.drawable.nav_build,
        navigateToRoute = "sentence_builder",
        title           = "Sentence Builder",
        message         = "Practice building sentences in Mandarin by arranging word tiles in the correct order. Great for understanding grammar and natural word structure."
    ),
    CoachStep(
        keys            = listOf(OnboardingKey.NAV_PROGRESS),
        emoji           = "📊",
        iconRes         = R.drawable.nav_progress,
        navigateToRoute = "progress",
        title           = "Track Your Journey",
        message         = "See all your badges, scenario star ratings, mastered words, and XP level. Watch your progress grow over time!"
    ),
    CoachStep(
        keys            = listOf(OnboardingKey.PARENT_LOCK),
        emoji           = "🔒",
        navigateToRoute = "progress",
        title           = "Parental Controls",
        message         = "Tap this lock icon to open the Parental Dashboard — set milestone rewards for your child, and choose which categories or scenarios appear in the app so you can guide their focus."
    ),
    CoachStep(
        emoji   = "🚀",
        title   = "You're All Set!",
        message = "加油！(Jiā yóu!) Head to the Roleplay tab and tap any category card to start your first Mandarin conversation!"
    ),
)

// ── Main overlay ─────────────────────────────────────────────────────────────

/**
 * Interactive coach-mark overlay drawn over the real HomeScreen.
 *
 * - Dims the screen with a semi-transparent scrim
 * - Cuts a transparent spotlight through the scrim over the target element
 * - Animates a pulsing ring around the spotlight
 * - Draws a pointer triangle connecting the spotlight to the tooltip bubble
 * - Shows a card with title, message, Next and Skip controls
 *
 * @param coords     Read-only view of the shared bounds map (from LocalOnboardingCoords).
 * @param onComplete Called when the user completes or skips the tour.
 */
@Composable
fun InteractiveOnboardingOverlay(
    coords: Map<String, Rect>,
    onNavigateToRoute: (String) -> Unit,
    onComplete: () -> Unit,
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val step = STEPS[stepIndex]
    val isLast = stepIndex == STEPS.lastIndex

    // Navigate the background to the relevant tab when the step changes
    LaunchedEffect(stepIndex) {
        step.navigateToRoute?.let { onNavigateToRoute(it) }
    }

    val spotlightRects: List<Rect> = step.keys.mapNotNull { coords[it] }
    // Primary spotlight is the first key (used for bubble positioning)
    val spotlightRect: Rect? = spotlightRects.firstOrNull()

    val density = LocalDensity.current

    // Pulse animation — scale and alpha breathe in sync
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseExpand by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseExpand"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.80f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightDp = maxHeight
        val screenHeightPx = with(density) { screenHeightDp.toPx() }

        // ── Scrim + spotlight (offscreen layer so BlendMode.Clear works) ──
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                // Swallow all taps — content below is not interactive during tour
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                // Swipe left = previous step; swipe right = next step
                .pointerInput(Unit) {
                    var totalDx = 0f
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                totalDx < -80f -> if (stepIndex > 0) stepIndex--
                                totalDx > 80f  -> if (stepIndex == STEPS.lastIndex) onComplete() else stepIndex++
                            }
                            totalDx = 0f
                        },
                        onDragCancel = { totalDx = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDx += dragAmount
                        }
                    )
                }
        ) {
            // 1. Dark scrim
            drawRect(Color.Black.copy(alpha = 0.70f))

            val pad    = 14f
            val corner = with(density) { 20.dp.toPx() }

            // 2. Punch transparent holes for every spotlight rect
            for (rect in spotlightRects) {
                val rx = rect.left   - pad
                val ry = rect.top    - pad
                val rw = rect.width  + pad * 2
                val rh = rect.height + pad * 2
                val holePath = Path().apply {
                    addRoundRect(RoundRect(rx, ry, rx + rw, ry + rh, CornerRadius(corner)))
                }
                drawPath(holePath, Color.Black, blendMode = BlendMode.Clear)
            }

            // 3. Pulsing ring — only on the primary (first) spotlight
            if (spotlightRect != null) {
                val ep = pad + pulseExpand
                val ringPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            spotlightRect.left   - ep,
                            spotlightRect.top    - ep,
                            spotlightRect.right  + ep,
                            spotlightRect.bottom + ep,
                            CornerRadius(corner + pulseExpand)
                        )
                    )
                }
                drawPath(
                    ringPath, Color.White.copy(alpha = pulseAlpha),
                    style = Stroke(width = with(density) { 2.5.dp.toPx() })
                )
            }
        }

        // ── Bubble card ─────────────────────────────────────────────────
        val bubbleHPad = 20.dp
        val gapDp      = 14.dp

        when {
            spotlightRect == null -> {
                // No spotlight → centred card
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    BubbleCard(
                        step       = step,
                        stepIndex  = stepIndex,
                        totalSteps = STEPS.size,
                        isLast     = isLast,
                        onNext     = { if (isLast) onComplete() else stepIndex++ },
                        onSkip     = onComplete,
                        modifier   = Modifier
                            .widthIn(max = 360.dp)
                            .padding(horizontal = bubbleHPad)
                    )
                }
            }

            spotlightRect.center.y < screenHeightPx * 0.5f -> {
                // Spotlight in TOP half → bubble below
                val bubbleTopDp = with(density) { spotlightRect.bottom.toDp() } + gapDp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = bubbleTopDp)
                        .padding(horizontal = bubbleHPad)
                ) {
                    BubbleCard(
                        step       = step,
                        stepIndex  = stepIndex,
                        totalSteps = STEPS.size,
                        isLast     = isLast,
                        onNext     = { if (isLast) onComplete() else stepIndex++ },
                        onSkip     = onComplete,
                    )
                }
            }

            else -> {
                // Spotlight in BOTTOM half → bubble above
                val offsetUpDp = screenHeightDp -
                    with(density) { spotlightRect.top.toDp() } + gapDp
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -offsetUpDp)
                        .fillMaxWidth()
                        .padding(horizontal = bubbleHPad)
                ) {
                    BubbleCard(
                        step       = step,
                        stepIndex  = stepIndex,
                        totalSteps = STEPS.size,
                        isLast     = isLast,
                        onNext     = { if (isLast) onComplete() else stepIndex++ },
                        onSkip     = onComplete,
                    )
                }
            }
        }

        // ── Step indicator dots (always at bottom) ────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            STEPS.indices.forEach { i ->
                val active = i == stepIndex
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(if (active) 20.dp else 6.dp)
                        .background(
                            color = if (active) Color.White else Color.White.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

// ── Tooltip bubble card ───────────────────────────────────────────────────────

@Composable
private fun BubbleCard(
    step: CoachStep,
    stepIndex: Int,
    totalSteps: Int,
    isLast: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape           = RoundedCornerShape(20.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        modifier        = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Step counter
            Text(
                text  = "${stepIndex + 1} / $totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (step.iconRes != null) {
                Image(
                    painter = painterResource(step.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(step.emoji, fontSize = 34.sp)
            }
            Text(
                text       = step.title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text      = step.message,
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLast) {
                    TextButton(
                        onClick  = onSkip,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text     = "Skip tour",
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
                Button(
                    onClick  = onNext,
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(if (isLast) 2f else 1f)
                        .height(48.dp)
                ) {
                    Text(
                        text       = if (isLast) "Let's go! 🚀" else "Next →",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp
                    )
                }
            }
        }
    }
}
