package com.ikhwan.mandarinkids

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedbackCard(
    isCorrect: Boolean,
    explanation: String
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val gradient = if (isCorrect) {
        if (isDark) listOf(Color(0xFF1A4E30), Color(0xFF10382A))
        else        listOf(Color(0xFFD4EDD0), Color(0xFFE8F5E2))
    } else {
        if (isDark) listOf(Color(0xFF4A1515), Color(0xFF3B0D0D))
        else        listOf(Color(0xFFFFCDD2), Color(0xFFFFEBEE))
    }
    val labelColor = if (isDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27)
    val headlineColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.background(Brush.verticalGradient(gradient))) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCorrect) "✅" else "❌",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column {
                    Text(
                        text = if (isCorrect) "Correct! 对了！" else "Not quite! 再试试！",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleMedium,
                        color = headlineColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = explanation,
                        fontSize = 14.sp,
                        color = labelColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
