package com.marcroldan.rimemba.ui.quickcapture

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.marcroldan.rimemba.R
import com.marcroldan.rimemba.RimembaApplication
import com.marcroldan.rimemba.ui.list.components.ListeningOverlay
import com.marcroldan.rimemba.ui.theme.RimembaTheme
import com.marcroldan.rimemba.voice.SpeechRecognitionController
import com.marcroldan.rimemba.voice.TextToSpeechController
import kotlinx.coroutines.launch

/**
 * Actividad trampolín lanzada desde el widget de botón grande: tema transparente
 * tipo diálogo, empieza a escuchar de inmediato y se cierra sola al terminar
 * (ver [QuickCaptureViewModel] para la máquina de estados y el temporizador de
 * seguridad que evita que quede huérfana).
 */
class QuickCaptureActivity : ComponentActivity() {

    private val container by lazy { (application as RimembaApplication).container }
    private val speechController by lazy { SpeechRecognitionController(applicationContext) }
    private val ttsController by lazy { TextToSpeechController(applicationContext) }

    private val viewModel: QuickCaptureViewModel by viewModels {
        QuickCaptureViewModel.Factory(
            speechController = speechController,
            ttsController = ttsController,
            captureVoiceItemUseCase = container.captureVoiceItemUseCase,
            timeProvider = container.timeProvider
        )
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            viewModel.start()
        } else {
            Toast.makeText(this, getString(R.string.permission_record_audio_denied), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Pide POST_NOTIFICATIONS (si hace falta) antes de cerrar la actividad, para
    // que un recordatorio creado desde el widget no se quede sin notificación
    // silenciosamente en la primera ejecución (Android 13+ lo deniega por defecto).
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { finish() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RimembaTheme {
                val estado by viewModel.uiState.collectAsState()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    val subtitulo = when (estado) {
                        is QuickCaptureViewModel.UiState.ListeningPartial ->
                            (estado as QuickCaptureViewModel.UiState.ListeningPartial).texto
                        QuickCaptureViewModel.UiState.Processing -> stringResource(R.string.mic_button_processing)
                        is QuickCaptureViewModel.UiState.Error ->
                            (estado as QuickCaptureViewModel.UiState.Error).mensaje
                        else -> stringResource(R.string.mic_button_listening)
                    }
                    ListeningOverlay(escuchando = true, subtitulo = subtitulo)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.finishEvent.collect { evento ->
                val hayQuePedirPermiso = evento.requierePermisoNotificaciones &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this@QuickCaptureActivity, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED

                if (hayQuePedirPermiso) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    finish()
                }
            }
        }

        val tienePermiso = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            viewModel.start()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
