package com.marcroldan.rimemba.ui.quickcapture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marcroldan.rimemba.core.Constants
import com.marcroldan.rimemba.core.TimeProvider
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.usecase.CaptureVoiceItemUseCase
import com.marcroldan.rimemba.voice.SpeechRecognitionController
import com.marcroldan.rimemba.voice.TextToSpeechController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Máquina de estados de la captura rápida desde el widget: Idle -> Listening ->
 * Processing -> Speaking -> Done. Un temporizador de seguridad fuerza el cierre
 * si el reconocedor de voz nunca llama a ningún callback (evita que la actividad
 * trampolín quede huérfana en algún dispositivo).
 */
class QuickCaptureViewModel(
    private val speechController: SpeechRecognitionController,
    private val ttsController: TextToSpeechController,
    private val captureVoiceItemUseCase: CaptureVoiceItemUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState
        data object Listening : UiState
        data class ListeningPartial(val texto: String) : UiState
        data object Processing : UiState
        data object Speaking : UiState
        data object Done : UiState
        data class Error(val mensaje: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    /** [requierePermisoNotificaciones] indica si hay que pedir POST_NOTIFICATIONS
     * antes de cerrar la actividad (se acaba de crear un recordatorio). */
    data class FinishEvent(val requierePermisoNotificaciones: Boolean)

    private val _finishEvent = MutableSharedFlow<FinishEvent>(extraBufferCapacity = 1)
    val finishEvent: SharedFlow<FinishEvent> = _finishEvent

    private var safetyJob: Job? = null
    private var yaProcesado = false

    fun start() {
        armarTemporizadorDeSeguridad()
        viewModelScope.launch {
            speechController.state.collect { estado ->
                when (estado) {
                    is SpeechRecognitionController.State.Listening -> _uiState.value = UiState.Listening
                    is SpeechRecognitionController.State.PartialResult ->
                        _uiState.value = UiState.ListeningPartial(estado.texto)
                    is SpeechRecognitionController.State.Result -> procesarResultado(estado.texto)
                    is SpeechRecognitionController.State.Error -> {
                        if (!yaProcesado) {
                            yaProcesado = true
                            _uiState.value = UiState.Error(estado.mensaje)
                            terminar(requierePermisoNotificaciones = false)
                        }
                    }
                    SpeechRecognitionController.State.Idle -> Unit
                }
            }
        }
        speechController.startListening()
    }

    private fun procesarResultado(texto: String) {
        if (yaProcesado) return
        yaProcesado = true
        safetyJob?.cancel()
        viewModelScope.launch {
            _uiState.value = UiState.Processing
            val item = captureVoiceItemUseCase(texto)
            val mensaje = captureVoiceItemUseCase.buildConfirmationMessage(item, timeProvider.now())
            _uiState.value = UiState.Speaking
            ttsController.speak(mensaje)
            _uiState.value = UiState.Done
            terminar(requierePermisoNotificaciones = item.tipo == TipoItem.RECORDATORIO)
        }
    }

    private fun armarTemporizadorDeSeguridad() {
        safetyJob = viewModelScope.launch {
            delay(Constants.QUICK_CAPTURE_SAFETY_TIMEOUT_MS)
            if (!yaProcesado) {
                yaProcesado = true
                speechController.stopListening()
                terminar(requierePermisoNotificaciones = false)
            }
        }
    }

    private fun terminar(requierePermisoNotificaciones: Boolean) {
        _finishEvent.tryEmit(FinishEvent(requierePermisoNotificaciones))
    }

    override fun onCleared() {
        safetyJob?.cancel()
        speechController.destroy()
        ttsController.shutdown()
        super.onCleared()
    }

    class Factory(
        private val speechController: SpeechRecognitionController,
        private val ttsController: TextToSpeechController,
        private val captureVoiceItemUseCase: CaptureVoiceItemUseCase,
        private val timeProvider: TimeProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            QuickCaptureViewModel(speechController, ttsController, captureVoiceItemUseCase, timeProvider) as T
    }
}
