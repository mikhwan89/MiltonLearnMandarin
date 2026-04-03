package com.ikhwan.mandarinkids.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class PinMode { SET, VERIFY, CHANGE }
enum class LockMode { PIN, MATH }
enum class MathDifficulty { EASY, MEDIUM, HARD }

data class MathQuestion(val display: String, val answer: Int)

fun generateMathQuestion(difficulty: MathDifficulty): MathQuestion {
    return when (difficulty) {
        MathDifficulty.EASY -> {
            if (Random.nextBoolean()) {
                val a = Random.nextInt(11, 89)
                val b = Random.nextInt(11, 99 - a)
                MathQuestion("$a + $b = ?", a + b)
            } else {
                val a = Random.nextInt(21, 99)
                val b = Random.nextInt(11, a - 1)
                MathQuestion("$a − $b = ?", a - b)
            }
        }
        MathDifficulty.MEDIUM -> {
            val a = Random.nextInt(2, 13)
            val b = Random.nextInt(2, 13)
            MathQuestion("$a × $b = ?", a * b)
        }
        MathDifficulty.HARD -> {
            if (Random.nextBoolean()) {
                val b = Random.nextInt(2, 13)
                val result = Random.nextInt(2, 13)
                MathQuestion("${b * result} ÷ $b = ?", result)
            } else {
                val a = Random.nextInt(2, 20)
                val b = Random.nextInt(2, 10)
                val c = Random.nextInt(2, 10)
                MathQuestion("$a + $b × $c = ?", a + b * c)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    mode: PinMode,
    lockMode: LockMode = LockMode.PIN,
    mathDifficulty: MathDifficulty = MathDifficulty.MEDIUM,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    onVerify: (String) -> Boolean = { false },
    onSetPin: (String) -> Unit = {}
) {
    val topBarTitle = when {
        lockMode == LockMode.MATH && mode == PinMode.VERIFY -> "🔐 Parent Access"
        mode == PinMode.VERIFY -> "🔐 Parent Access"
        mode == PinMode.CHANGE -> "🔑 Change PIN"
        else -> "🔑 Create PIN"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (lockMode == LockMode.MATH && mode == PinMode.VERIFY) {
            MathChallengeContent(
                difficulty = mathDifficulty,
                onSuccess = onSuccess,
                modifier = Modifier.padding(padding)
            )
        } else {
            PinPadContent(
                mode = mode,
                onSuccess = onSuccess,
                onVerify = onVerify,
                onSetPin = onSetPin,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun PinPadContent(
    mode: PinMode,
    onSuccess: () -> Unit,
    onVerify: (String) -> Boolean,
    onSetPin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var entered by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    // VERIFY → step 0 only; SET → starts at step 1; CHANGE → step 0=verify, 1=new, 2=confirm
    var step by remember { mutableStateOf(if (mode == PinMode.SET) 1 else 0) }
    var error by remember { mutableStateOf("") }

    fun handleDigit(d: String) {
        if (entered.length >= 4) return
        entered += d
        error = ""
        if (entered.length < 4) return

        when {
            mode == PinMode.VERIFY -> {
                if (onVerify(entered)) onSuccess()
                else { error = "Wrong PIN. Try again."; entered = "" }
            }
            mode == PinMode.CHANGE && step == 0 -> {
                if (onVerify(entered)) { entered = ""; step = 1 }
                else { error = "Wrong PIN. Try again."; entered = "" }
            }
            (mode == PinMode.SET || mode == PinMode.CHANGE) && step == 1 -> {
                confirmPin = entered; entered = ""; step = 2
            }
            (mode == PinMode.SET || mode == PinMode.CHANGE) && step == 2 -> {
                if (entered == confirmPin) { onSetPin(entered); onSuccess() }
                else { error = "PINs don't match. Try again."; entered = ""; confirmPin = ""; step = 1 }
            }
        }
    }

    val subtitle = when {
        mode == PinMode.VERIFY -> "Enter your 4-digit PIN"
        mode == PinMode.CHANGE && step == 0 -> "Enter your current PIN"
        mode == PinMode.CHANGE && step == 1 -> "Enter your new 4-digit PIN"
        mode == PinMode.CHANGE && step == 2 -> "Enter new PIN again to confirm"
        step == 1 -> "Choose a 4-digit PIN"
        else -> "Enter PIN again to confirm"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(subtitle, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

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
                            onClick = { if (entered.isNotEmpty()) { entered = entered.dropLast(1); error = "" } },
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

@Composable
private fun MathChallengeContent(
    difficulty: MathDifficulty,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = remember { generateMathQuestion(difficulty) }
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var cooldown by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    fun submit() {
        if (entered.isBlank() || cooldown > 0) return
        if (entered.trim() == question.answer.toString()) {
            onSuccess()
        } else {
            error = "Wrong answer."
            entered = ""
            scope.launch {
                for (i in 3 downTo 1) { cooldown = i; delay(1000L) }
                cooldown = 0
                error = ""
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Solve to enter 🧮", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        Text(
            text = question.display,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = entered,
            onValueChange = { v -> if (v.length <= 6 && v.all { it.isDigit() || it == '-' }) entered = v },
            label = { Text("Your answer") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            enabled = cooldown == 0,
            modifier = Modifier
                .width(180.dp)
                .focusRequester(focusRequester),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 22.sp)
        )

        Spacer(Modifier.height(12.dp))

        if (error.isNotEmpty()) {
            Text(
                text = if (cooldown > 0) "$error Try again in ${cooldown}s…" else error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { submit() },
            enabled = entered.isNotBlank() && cooldown == 0,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(52.dp)
        ) {
            Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
