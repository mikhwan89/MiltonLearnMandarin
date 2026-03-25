package com.ikhwan.mandarinkids.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PinMode { SET, VERIFY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    mode: PinMode,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    onVerify: (String) -> Boolean,
    onSetPin: (String) -> Unit
) {
    var entered by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(if (mode == PinMode.SET) 1 else 0) }
    // step 0 = verify, 1 = set new, 2 = confirm new
    var error by remember { mutableStateOf("") }

    fun handleDigit(d: String) {
        if (entered.length >= 4) return
        entered += d
        error = ""

        if (entered.length == 4) {
            when {
                mode == PinMode.VERIFY -> {
                    if (onVerify(entered)) {
                        onSuccess()
                    } else {
                        error = "Wrong PIN. Try again."
                        entered = ""
                    }
                }
                mode == PinMode.SET && step == 1 -> {
                    confirmPin = entered
                    entered = ""
                    step = 2
                }
                mode == PinMode.SET && step == 2 -> {
                    if (entered == confirmPin) {
                        onSetPin(entered)
                        onSuccess()
                    } else {
                        error = "PINs don't match. Try again."
                        entered = ""
                        confirmPin = ""
                        step = 1
                    }
                }
            }
        }
    }

    val title = when {
        mode == PinMode.VERIFY -> "🔐 Parent Access"
        step == 1 -> "🔑 Create PIN"
        else -> "🔑 Confirm PIN"
    }

    val subtitle = when {
        mode == PinMode.VERIFY -> "Enter your 4-digit PIN"
        step == 1 -> "Choose a 4-digit PIN"
        else -> "Enter PIN again to confirm"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(subtitle, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < entered.length) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numpad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { label ->
                        if (label.isEmpty()) {
                            Spacer(modifier = Modifier.size(68.dp))
                        } else if (label == "⌫") {
                            OutlinedButton(
                                onClick = { if (entered.isNotEmpty()) entered = entered.dropLast(1) },
                                modifier = Modifier.size(68.dp),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Backspace, contentDescription = "Delete")
                            }
                        } else {
                            Button(
                                onClick = { handleDigit(label) },
                                modifier = Modifier.size(68.dp),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
