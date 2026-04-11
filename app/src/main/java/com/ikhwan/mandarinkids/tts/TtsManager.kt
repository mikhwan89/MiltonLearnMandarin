package com.ikhwan.mandarinkids.tts

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ikhwan.mandarinkids.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class TtsManager(private val context: Context) {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
            }
        }
    }

    /**
     * Plays a 300 ms silent WAV clip that wakes the audio hardware (DAC / earphone amp)
     * before TTS begins, preventing the first syllable from being clipped on wired or
     * Bluetooth earphones after a period of silence.
     *
     * Returns immediately (fire-and-forget). The primer is ~300 ms so TTS queued right
     * after will start into an already-active audio path.
     */
    private fun primeSilence() {
        try {
            MediaPlayer.create(context, R.raw.silence_300ms)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (_: Exception) {
            // If the resource is missing or playback fails, proceed without the primer
            // rather than crashing or blocking TTS.
        }
    }

    /**
     * Plays a 300 ms silent WAV and suspends until it finishes, then returns.
     * Used by [speakAndAwait] so TTS starts only after the audio path is warm.
     */
    private suspend fun primeSilenceAndAwait() {
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                val mp = MediaPlayer.create(context, R.raw.silence_300ms)
                if (mp == null) {
                    cont.resume(Unit)
                    return@suspendCancellableCoroutine
                }
                mp.setOnCompletionListener {
                    it.release()
                    if (cont.isActive) cont.resume(Unit)
                }
                mp.setOnErrorListener { it, _, _ ->
                    it.release()
                    if (cont.isActive) cont.resume(Unit)
                    true
                }
                cont.invokeOnCancellation { runCatching { mp.release() } }
                mp.start()
            }
        } catch (_: Exception) {
            // Proceed without the primer on any failure.
        }
    }

    /** Fire-and-forget speech at the given rate (default 1.0x). */
    fun speak(text: String, rate: Float = 1.0f) {
        primeSilence()
        tts?.setSpeechRate(rate)
        // Small delay so the silence primer has time to start before TTS is queued.
        // TTS is queued onto the engine's own thread so the 300 ms primer plays first.
        tts?.speak(text, TextToSpeech.QUEUE_ADD, Bundle(), null)
    }

    /** Suspend until the utterance completes or is interrupted. Cancellation stops TTS. */
    suspend fun speakAndAwait(text: String, utteranceId: String, rate: Float = 1.0f) {
        primeSilenceAndAwait()
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
