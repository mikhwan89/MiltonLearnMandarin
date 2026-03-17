package com.ikhwan.mandarinkids.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ikhwan.mandarinkids.ToneUtils

/** Splits a Chinese string into individual characters, filtering non-CJK chars. */
private fun splitChineseChars(text: String): List<String> =
    text.filter { c ->
        val cp = c.code
        (cp in 0x4E00..0x9FFF) || // CJK Unified Ideographs
        (cp in 0x3400..0x4DBF) || // CJK Extension A
        (cp in 0x20000..0x2A6DF)  // CJK Extension B
    }.map { it.toString() }

@SuppressLint("SetJavaScriptEnabled")
private fun hanziHtml(character: String): String = """
<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { background: transparent; display: flex; justify-content: center; align-items: center; height: 100vh; }
    #target { }
  </style>
</head>
<body>
  <div id="target"></div>
  <script src="hanzi-writer.min.js"></script>
  <script>
    HanziWriter.create('target', '$character', {
      width: 260,
      height: 260,
      padding: 10,
      showOutline: true,
      strokeColor: '#1565C0',
      outlineColor: '#BBDEFB',
      strokeAnimationSpeed: 0.7,
      delayBetweenStrokes: 400,
      delayBetweenLoops: 1200,
      dataURL: 'https://cdn.jsdelivr.net/npm/hanzi-writer-data@latest/'
    }).loopCharacterAnimation();
  </script>
</body>
</html>
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrokeOrderSheet(
    chinese: String,
    pinyin: String,
    onDismiss: () -> Unit
) {
    val chars = remember(chinese) { splitChineseChars(chinese) }
    var charIndex by remember { mutableIntStateOf(0) }
    val currentChar = chars.getOrNull(charIndex) ?: return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "✏️ How to Write",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Character + pinyin label
            Text(
                currentChar,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (pinyin.isNotBlank()) {
                Text(
                    ToneUtils.coloredAnnotatedPinyin(pinyin),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // WebView showing animated strokes
            Card(
                modifier = Modifier
                    .size(280.dp)
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                key(currentChar) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.allowFileAccess = true
                                webViewClient = WebViewClient()
                                setBackgroundColor(0x00000000) // transparent
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(
                                "file:///android_asset/",
                                hanziHtml(currentChar),
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Text(
                "Watch the strokes, then try writing it yourself!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            // Prev / Next navigation for multi-character words
            if (chars.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { charIndex-- },
                        enabled = charIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous character")
                    }
                    Text(
                        "${charIndex + 1} / ${chars.size}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { charIndex++ },
                        enabled = charIndex < chars.size - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next character")
                    }
                }
            }

            TextButton(onClick = onDismiss) { Text("Close") }
        }
    }
}
