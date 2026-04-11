package com.ikhwan.mandarinkids.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ikhwan.mandarinkids.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class TtsManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /**
     * True once TTS is initialised and a Chinese voice is confirmed available.
     * Backed by Compose [mutableStateOf] so composables observing it will recompose
     * when it changes (e.g. to trigger auto-play on the first flashcard).
     */
    var isReady: Boolean by mutableStateOf(false)
        private set

    /**
     * Set by the caller to receive a one-time callback when TTS is unavailable —
     * either because the engine failed to initialise or because no Chinese voice
     * is installed. Invoked on the TTS init thread.
     * Use it to show an in-app banner guiding the user to install the voice pack.
     */
    var onChineseVoiceMissing: (() -> Unit)? = null

    /**
     * Populated after init with the reason TTS is unavailable:
     *  "VOICE_MISSING"  — engine OK but Chinese voice pack not installed
     *  "INIT_FAILED"    — TTS engine itself failed to start
     * Null when TTS is working correctly.
     */
    var ttsFailureReason: String? by mutableStateOf(null)
        private set

    // AudioFocus plumbing ---------------------------------------------------

    private val focusAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val focusRequest: AudioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(focusAttributes)
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener { /* no-op: we stop on cancellation */ }
            .build()

    private fun acquireFocus() {
        audioManager.requestAudioFocus(focusRequest)
    }

    private fun releaseFocus() {
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    // -----------------------------------------------------------------------

    init {
        tts = TextToSpeech(context) { status ->
            val engineName = tts?.defaultEngine ?: "unknown"
            Log.d("TtsManager", "init status=$status engine=$engineName")

            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINESE)
                val voiceAvailable = result != TextToSpeech.LANG_MISSING_DATA &&
                                     result != TextToSpeech.LANG_NOT_SUPPORTED
                if (voiceAvailable) {
                    isReady = true
                } else {
                    Log.w("TtsManager", "Chinese voice not installed (engine=$engineName)")
                    ttsFailureReason = "VOICE_MISSING"
                    onChineseVoiceMissing?.invoke()
                }
            } else {
                // TTS engine failed to initialise entirely (e.g. Samsung TTS with no
                // Chinese support, or engine not installed).
                Log.e("TtsManager", "TTS init failed status=$status engine=$engineName")
                ttsFailureReason = "INIT_FAILED"
                onChineseVoiceMissing?.invoke()
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
        if (!isReady) return
        acquireFocus()
        primeSilence()
        tts?.setSpeechRate(rate)
        // TTS is queued onto the engine's own thread so the 300 ms primer plays first.
        tts?.speak(text, TextToSpeech.QUEUE_ADD, Bundle(), null)
        // AudioFocus released in shutdown() or when the utterance ends naturally;
        // for fire-and-forget we release shortly after queuing — the audio path
        // stays active until the engine finishes draining its queue.
        releaseFocus()
    }

    /** Suspend until the utterance completes or is interrupted. Cancellation stops TTS. */
    suspend fun speakAndAwait(text: String, utteranceId: String, rate: Float = 1.0f) {
        if (!isReady) return
        acquireFocus()
        try {
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
        } finally {
            releaseFocus()
        }
    }

    fun stop() {
        tts?.stop()
        releaseFocus()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        releaseFocus()
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
