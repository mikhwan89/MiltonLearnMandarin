package com.ikhwan.mandarinkids.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val confettiColors = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0),
    Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFEB3B),
    Color(0xFFFF9800), Color(0xFF00BCD4)
)

private class ConfettiParticle(canvasWidth: Float) {
    var x = Random.nextFloat() * canvasWidth
    var y = Random.nextFloat() * -200f        // start above the screen
    val vx = (Random.nextFloat() - 0.5f) * 180f
    var vy = Random.nextFloat() * 180f + 80f
    val color = confettiColors.random()
    val size = Random.nextFloat() * 14f + 8f
    var rotation = Random.nextFloat() * 360f
    val rotationSpeed = (Random.nextFloat() - 0.5f) * 400f
    val isCircle = Random.nextBoolean()

    fun update(dt: Float) {
        x += vx * dt
        y += vy * dt
        vy += 350f * dt   // gravity
        rotation += rotationSpeed * dt
    }
}

/**
 * Full-screen confetti overlay that animates for [durationMs] ms then disappears.
 * Place this as a Box overlay — it is pointer-transparent.
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
    durationMs: Long = 2500L
) {
    // Use a frame counter to drive recomposition each frame
    var frameMs by remember { mutableLongStateOf(0L) }
    var startMs by remember { mutableLongStateOf(-1L) }
    val particles = remember { mutableListOf<ConfettiParticle>() }

    LaunchedEffect(Unit) {
        var lastMs = -1L
        while (true) {
            withFrameMillis { now ->
                if (startMs == -1L) {
                    startMs = now
                    // Initialise particles with a placeholder width; update on first draw
                    repeat(particleCount) { particles.add(ConfettiParticle(1080f)) }
                }
                val elapsed = now - startMs
                if (elapsed > durationMs) return@withFrameMillis
                val dt = if (lastMs == -1L) 0.016f else (now - lastMs) / 1000f
                lastMs = now
                particles.forEach { it.update(dt) }
                frameMs = now
            }
            if (startMs != -1L && frameMs - startMs > durationMs) break
        }
    }

    val elapsed = if (startMs == -1L) 0L else frameMs - startMs
    if (elapsed < durationMs) {
        Canvas(modifier = modifier.fillMaxSize()) {
            // Re-initialise particle X positions using real canvas width on first frame
            if (particles.isNotEmpty() && particles[0].x > size.width && size.width > 0) {
                particles.forEach { it.x = Random.nextFloat() * size.width }
            }
            particles.forEach { p ->
                withTransform({
                    translate(p.x, p.y)
                    rotate(p.rotation, pivot = Offset.Zero)
                }) {
                    val alpha = if (elapsed > durationMs - 500L) {
                        1f - (elapsed - (durationMs - 500L)) / 500f
                    } else 1f
                    val color = p.color.copy(alpha = alpha.coerceIn(0f, 1f))
                    if (p.isCircle) {
                        drawCircle(color, radius = p.size / 2f, center = Offset.Zero)
                    } else {
                        drawRect(
                            color,
                            topLeft = Offset(-p.size / 2f, -p.size * 0.35f),
                            size = Size(p.size, p.size * 0.7f)
                        )
                    }
                }
            }
        }
    }
}
