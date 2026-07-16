package com.marcroldan.rimemba.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * Envuelve [SpeechRecognizer] con [RecognitionListener] (no
 * `startActivityForResult` con el Intent de reconocimiento) para que el
 * reconocimiento ocurra in-place, sin abrir la UI de Google — necesario tanto
 * para el botón único en MainActivity como para la actividad trampolín del widget.
 */
class SpeechRecognitionController(private val context: Context) {

    sealed interface State {
        data object Idle : State
        data object Listening : State
        data class PartialResult(val texto: String) : State
        data class Result(val texto: String) : State
        data class Error(val mensaje: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    private var recognizer: SpeechRecognizer? = null

    fun startListening(locale: Locale = Locale("es", "ES")) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = State.Error("El reconocimiento de voz no está disponible en este dispositivo")
            return
        }

        destroy()

        val nuevoRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = nuevoRecognizer
        nuevoRecognizer.setRecognitionListener(listener)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        _state.value = State.Listening
        nuevoRecognizer.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
    }

    /**
     * Vuelve a [State.Idle] tras consumir un [State.Result]/[State.Error].
     * Necesario porque [State.Result] es un data class: si dos frases seguidas
     * transcriben exactamente el mismo texto, un `LaunchedEffect(estado)` que
     * observe este flujo no se relanzaría sin este reseteo intermedio, ya que
     * la igualdad estructural del estado no cambiaría entre una llamada y la siguiente.
     */
    fun resetToIdle() {
        _state.value = State.Idle
    }

    fun destroy() {
        recognizer?.setRecognitionListener(null)
        recognizer?.destroy()
        recognizer = null
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit

        override fun onError(error: Int) {
            _state.value = State.Error(mensajeDeError(error))
        }

        override fun onResults(results: Bundle?) {
            val texto = primerResultado(results)
            _state.value = if (texto.isNullOrBlank()) {
                State.Error("No se entendió lo dicho")
            } else {
                State.Result(texto)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            primerResultado(partialResults)?.let { _state.value = State.PartialResult(it) }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun primerResultado(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun mensajeDeError(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH -> "No se entendió lo dicho"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó ninguna voz"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Error de red"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Falta el permiso de micrófono"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor de voz está ocupado"
        else -> "Error de reconocimiento de voz ($error)"
    }
}
