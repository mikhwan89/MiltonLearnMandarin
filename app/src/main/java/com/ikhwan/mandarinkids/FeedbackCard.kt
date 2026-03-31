package com.ikhwan.mandarinkids

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.ui.theme.appColors

@Composable
fun FeedbackCard(
    isCorrect: Boolean,
    explanation: String
) {
    val colors = MaterialTheme.appColors
    val gradient = if (isCorrect) colors.tileGreen.asList() else colors.answerWrongSoft.asList()
    val labelColor = colors.onLightTile
    val headlineColor = if (isCorrect) colors.answerCorrectText else colors.answerWrongText

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
