package com.ikhwan.mandarinkids.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
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

    /** Suspend until the utterance completes or is interrupted. Cancellation stops TTS. */
    suspend fun speakAndAwait(text: String, utteranceId: String, rate: Float = 1.0f) {
        tts?.setSpeechRate(rate)
        suspendCancellableCoroutine<Unit> { continuation ->
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}

                override fun onDone(id: String?) {
                    if (id == utteranceId && continuation.isActive) continuation.resume(Unit)
                }

                override fun onError(id: String?) {
                    if (id == utteranceId && continuation.isActive) continuation.resume(Unit)
                }

                // API 23+: called when an utterance is interrupted by QUEUE_FLUSH or stop().
                // Without this override, interrupted long utterances leave the coroutine hanging.
                override fun onStop(id: String?, interrupted: Boolean) {
                    if (id == utteranceId && continuation.isActive) continuation.resume(Unit)
                }
            })
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            continuation.invokeOnCancellation { tts?.stop() }
        }
        // Some TTS engines fire onDone before the audio buffer has fully played out —
        // this is especially noticeable on long sentences. Poll until the engine is
        // truly silent before returning, so the caller can safely start the next line.
        while (tts?.isSpeaking == true) {
            delay(50)
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
