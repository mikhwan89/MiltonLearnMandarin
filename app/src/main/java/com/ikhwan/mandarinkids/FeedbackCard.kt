package com.ikhwan.mandarinkids

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedbackCard(
    isCorrect: Boolean,
    explanation: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
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
                    color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = explanation,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
