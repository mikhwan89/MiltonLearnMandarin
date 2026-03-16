package com.ikhwan.mandarinkids.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
            }
        }
    }

    /** Fire-and-forget speech at the given rate (default 1.0x). */
    fun speak(text: String, rate: Float = 1.0f) {
        tts?.setSpeechRate(rate)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), null)
    }

    /** Suspend until the utterance completes or errors. Cancellation stops TTS. */
    suspend fun speakAndAwait(text: String, utteranceId: String, rate: Float = 1.0f) {
        tts?.setSpeechRate(rate)
        suspendCancellableCoroutine<Unit> { continuation ->
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId) continuation.resume(Unit)
                }
                override fun onError(id: String?) {
                    if (id == utteranceId) continuation.resume(Unit)
                }
            })
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            continuation.invokeOnCancellation { tts?.stop() }
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}

@Composable
fun rememberTtsManager(): TtsManager {
    val context = LocalContext.current
    val manager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose { manager.shutdown() }
    }
    return manager
}
