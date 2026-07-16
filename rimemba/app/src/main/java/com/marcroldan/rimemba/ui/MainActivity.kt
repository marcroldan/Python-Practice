package com.marcroldan.rimemba.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.marcroldan.rimemba.R
import com.marcroldan.rimemba.RimembaApplication
import com.marcroldan.rimemba.core.TimeProvider
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.usecase.CaptureVoiceItemUseCase
import com.marcroldan.rimemba.ui.list.ItemListScreen
import com.marcroldan.rimemba.ui.list.ItemListViewModel
import com.marcroldan.rimemba.ui.list.components.ListeningOverlay
import com.marcroldan.rimemba.ui.theme.RimembaTheme
import com.marcroldan.rimemba.voice.SpeechRecognitionController
import com.marcroldan.rimemba.voice.TextToSpeechController

class MainActivity : ComponentActivity() {

    private val container by lazy { (application as RimembaApplication).container }

    private val viewModel: ItemListViewModel by viewModels {
        ItemListViewModel.Factory(container.itemRepository)
    }

    private val speechController by lazy { SpeechRecognitionController(applicationContext) }
    private val ttsController by lazy { TextToSpeechController(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RimembaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
                ) { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        ItemListScreen(
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        VoiceCaptureSection(
                            speechController = speechController,
                            ttsController = ttsController,
                            captureVoiceItemUseCase = container.captureVoiceItemUseCase,
                            timeProvider = container.timeProvider
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        speechController.destroy()
        ttsController.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun VoiceCaptureSection(
    speechController: SpeechRecognitionController,
    ttsController: TextToSpeechController,
    captureVoiceItemUseCase: CaptureVoiceItemUseCase,
    timeProvider: TimeProvider
) {
    val context = LocalContext.current
    val estado by speechController.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            speechController.startListening()
        } else {
            Toast.makeText(context, context.getString(R.string.permission_record_audio_denied), Toast.LENGTH_LONG).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            Toast.makeText(context, context.getString(R.string.permission_notifications_denied), Toast.LENGTH_LONG).show()
        }
    }

    fun iniciarEscucha() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            speechController.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun asegurarPermisoDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedido) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(estado) {
        val resultado = estado
        if (resultado is SpeechRecognitionController.State.Result) {
            val item = captureVoiceItemUseCase(resultado.texto)
            if (item.tipo == TipoItem.RECORDATORIO) {
                asegurarPermisoDeNotificaciones()
            }
            val mensaje = captureVoiceItemUseCase.buildConfirmationMessage(item, timeProvider.now())
            ttsController.speak(mensaje)
            speechController.resetToIdle()
        } else if (resultado is SpeechRecognitionController.State.Error) {
            Toast.makeText(context, resultado.mensaje, Toast.LENGTH_SHORT).show()
            speechController.resetToIdle()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (estado) {
            is SpeechRecognitionController.State.Listening,
            is SpeechRecognitionController.State.PartialResult -> {
                val subtitulo = (estado as? SpeechRecognitionController.State.PartialResult)?.texto
                    ?: stringResource(R.string.mic_button_listening)
                ListeningOverlay(escuchando = true, subtitulo = subtitulo)
            }
            else -> {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { iniciarEscucha() }
                ) {
                    Box(
                        modifier = Modifier.padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = stringResource(R.string.mic_button_content_description),
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
