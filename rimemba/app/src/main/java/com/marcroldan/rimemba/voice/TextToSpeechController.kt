package com.marcroldan.rimemba.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Envuelve [TextToSpeech], esperando a `onInit` antes de hablar y usando
 * [UtteranceProgressListener] para saber cuándo termina de hablar — importante
 * para no cerrar la actividad trampolín antes de que acabe la confirmación.
 */
class TextToSpeechController(context: Context) {

    private val listo = CompletableDeferred<Boolean>()

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext) { status ->
        val ok = status == TextToSpeech.SUCCESS
        if (ok) {
            tts?.language = Locale("es", "ES")
        }
        listo.complete(ok)
    }

    suspend fun speak(texto: String) {
        val disponible = listo.await()
        val instancia = tts
        if (!disponible || instancia == null) return

        suspendCancellableCoroutine<Unit> { continuation ->
            val utteranceId = UUID.randomUUID().toString()
            instancia.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                @Deprecated("Deprecated in Java", ReplaceWith(""))
                override fun onError(utteranceId: String?) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            })

            continuation.invokeOnCancellation { instancia.stop() }

            instancia.speak(texto, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
