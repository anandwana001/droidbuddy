package com.androidengineers.agent_quickstart_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidengineers.agent_quickstart_android.agenda.AgendaScreen
import com.androidengineers.agent_quickstart_android.agenda.AgendaViewModel
import com.androidengineers.agent_quickstart_android.config.DroidBuddyConfig
import com.androidengineers.agent_quickstart_android.ui.ConversationViewModel
import com.androidengineers.agent_quickstart_android.ui.DroidBuddyHomeScreen
import com.androidengineers.agent_quickstart_android.ui.DroidBuddyVoiceScreen
import com.androidengineers.agent_quickstart_android.ui.theme.DroidBuddyTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ConversationViewModel>()
    private val agendaViewModel by viewModels<AgendaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val ui by viewModel.uiState.collectAsStateWithLifecycle()
            val language by viewModel.language.collectAsStateWithLifecycle()
            val agenda by agendaViewModel.state.collectAsStateWithLifecycle()
            var destination by rememberSaveable { mutableStateOf("home") }
            var voiceExpanded by rememberSaveable { mutableStateOf(false) }
            val context = LocalContext.current
            val currentViewModel by rememberUpdatedState(viewModel)
            val microphone =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    granted ->
                    currentViewModel.updateMicrophonePermission(granted)
                    if (granted) currentViewModel.startConversation()
                    else currentViewModel.microphoneDenied()
                }
            LaunchedEffect(Unit) {
                currentViewModel.updateMicrophonePermission(
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                )
            }
            val start: () -> Unit = {
                voiceExpanded = true
                if (!ui.inConversation && !ui.isStarting) {
                    if (!DroidBuddyConfig.isConfigured) currentViewModel.startConversation()
                    else {
                        val granted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO,
                            ) == PackageManager.PERMISSION_GRANTED
                        currentViewModel.updateMicrophonePermission(granted)
                        if (granted) currentViewModel.startConversation()
                        else microphone.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
            if (voiceExpanded) {
                DroidBuddyVoiceScreen(
                    ui,
                    onCollapse = { voiceExpanded = false },
                    onStart = start,
                    onEnd = {
                        currentViewModel.endConversation()
                        voiceExpanded = false
                    },
                    onMute = viewModel::toggleMicrophone,
                    onDraft = viewModel::updateTextDraft,
                    onSend = viewModel::sendText,
                    onDismiss = viewModel::clearTransientMessages,
                )
            } else
                DroidBuddyTheme(darkTheme = false) {
                    Scaffold(
                        containerColor = Color(0xFFFAF7F5),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            Column {
                                if (ui.inConversation)
                                    Surface(color = Color(0xFFECE5F8)) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(start = 20.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                if (ui.isStopping) "Ending conversation…"
                                                else "Voice conversation active",
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                            TextButton(onClick = { voiceExpanded = true }) {
                                                Text("Return")
                                            }
                                        }
                                    }
                                NavigationBar(
                                    containerColor = Color(0xFFFCFAF9),
                                    tonalElevation = 0.dp,
                                ) {
                                    val colors =
                                        NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF26232C),
                                            selectedTextColor = Color(0xFF26232C),
                                            indicatorColor = Color(0xFFECE5F8),
                                        )
                                    NavigationBarItem(
                                        destination == "home",
                                        { destination = "home" },
                                        icon = { Icon(Icons.Outlined.Home, null) },
                                        label = { Text("Home") },
                                        colors = colors,
                                    )
                                    NavigationBarItem(
                                        false,
                                        start,
                                        icon = { Icon(Icons.Outlined.Mic, null) },
                                        label = { Text("Talk") },
                                        colors = colors,
                                    )
                                    NavigationBarItem(
                                        destination == "agenda",
                                        { destination = "agenda" },
                                        icon = { Icon(Icons.Outlined.CalendarMonth, null) },
                                        label = { Text("Agenda") },
                                        colors = colors,
                                    )
                                }
                            }
                        },
                    ) { padding ->
                        Box(
                            Modifier.fillMaxSize()
                                .padding(bottom = padding.calculateBottomPadding()),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            if (destination == "agenda")
                                AgendaScreen(
                                    agenda,
                                    agendaViewModel::toggleSaved,
                                    agendaViewModel::load,
                                ) { prompt ->
                                    currentViewModel.updateTextDraft(prompt)
                                    voiceExpanded = true
                                }
                            else
                                DroidBuddyHomeScreen(
                                    ui,
                                    agenda,
                                    start,
                                    viewModel::updateTextDraft,
                                    { destination = "agenda" },
                                    language,
                                    viewModel::selectLanguage,
                                )
                        }
                    }
                }
        }
    }
}
