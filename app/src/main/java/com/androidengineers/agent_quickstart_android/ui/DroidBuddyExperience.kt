package com.androidengineers.agent_quickstart_android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.androidengineers.agent_quickstart_android.agenda.AgendaState
import com.androidengineers.agent_quickstart_android.model.*
import com.androidengineers.agent_quickstart_android.ui.theme.DroidBuddyTheme
import kotlin.math.*

private val ReferencePaper = Color(0xFFFAF7F5)
private val ReferenceInk = Color(0xFF23232A)
private val Lavender = Color(0xFFB6A1EB)
private val VoiceCharcoal = Color(0xFF17171E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroidBuddyHomeScreen(
    ui: ConversationUiState,
    agenda: AgendaState,
    onTalk: () -> Unit,
    onDraft: (String) -> Unit,
    onAgenda: () -> Unit,
    language: com.androidengineers.agent_quickstart_android.data.VoiceLanguage =
        com.androidengineers.agent_quickstart_android.data.VoiceLanguage.ENGLISH,
    onLanguage: (com.androidengineers.agent_quickstart_android.data.VoiceLanguage) -> Unit = {},
) {
    var typing by rememberSaveable { mutableStateOf(false) }
    var setup by rememberSaveable { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize().background(ReferencePaper).statusBarsPadding()) {
        val compact = maxHeight < 670.dp
        Column(
            Modifier.fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        "DroidBuddy",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                        color = ReferenceInk,
                    )
                    Text(
                        "droidcon India 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { setup = true }) {
                    Icon(Icons.Outlined.Tune, "Voice setup and about", tint = ReferenceInk)
                }
            }
            TextButton(onClick = { setup = true }) { Text("${language.label} · Voice language") }
            Spacer(Modifier.height(if (compact) 16.dp else 32.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Hey developer,",
                    fontSize = 34.sp,
                    lineHeight = 43.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-1.2).sp,
                    color = ReferenceInk,
                )
                Text(
                    "what’s your plan\nfor today?",
                    fontSize = 34.sp,
                    lineHeight = 43.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-1.2).sp,
                    color = ReferenceInk,
                )
            }
            VoiceWave(
                Modifier.fillMaxWidth().height(if (compact) 130.dp else 178.dp),
                active = false,
            )
            Text(
                "A little curiosity goes a long way.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(if (compact) 30.dp else 42.dp))
            Box(Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
                FilledIconButton(
                    onClick = onTalk,
                    modifier = Modifier.size(78.dp),
                    shape = CircleShape,
                    colors =
                        IconButtonDefaults.filledIconButtonColors(
                            containerColor = ReferenceInk,
                            contentColor = Color.White,
                        ),
                ) {
                    Icon(
                        Icons.Outlined.Mic,
                        if (ui.inConversation) "Return to conversation" else "Talk to DroidBuddy",
                        Modifier.size(31.dp),
                    )
                }
                FilledTonalIconButton(
                    onClick = { typing = true },
                    modifier = Modifier.align(Alignment.CenterEnd).size(48.dp),
                    colors =
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.85f),
                            contentColor = ReferenceInk,
                        ),
                ) {
                    Icon(Icons.Outlined.Keyboard, "Type a question")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (ui.inConversation) "Your conversation is active. Tap to return."
                else "Ask about sessions, Android, or your next idea.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Powered by Agora Conversational AI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
            )
            if (ui.errorMessage != null)
                Text(
                    ui.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            val saved = agenda.agenda?.sessions?.count { it.id in agenda.saved } ?: 0
            TextButton(onClick = onAgenda, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (saved > 0) "$saved saved in your session plan"
                    else "Explore the full conference agenda",
                    Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(18.dp))
            }
        }
    }
    if (typing)
        ModalBottomSheet(onDismissRequest = { typing = false }, containerColor = ReferencePaper) {
            Column(
                Modifier.padding(24.dp).imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("What’s on your mind?", style = MaterialTheme.typography.headlineMedium)
                OutlinedTextField(
                    ui.textDraft,
                    onDraft,
                    label = { Text("Your question") },
                    placeholder = { Text("Which sessions cover on-device AI?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(18.dp),
                )
                Button(
                    onClick = {
                        typing = false
                        onTalk()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                ) {
                    Text(if (ui.inConversation) "Continue conversation" else "Start conversation")
                }
                Text(
                    "Your draft stays ready to send in the conversation. The default voice session requests microphone access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    if (setup)
        ModalBottomSheet(onDismissRequest = { setup = false }, containerColor = ReferencePaper) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Talk. Discover. Connect.", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "A Voice AI demo by Akshay Nandwana, Google Developer Expert in Android. Powered by Agora Conversational AI."
                )
                Text("Conversation language", style = MaterialTheme.typography.titleMedium)
                val languageLocked = ui.inConversation || ui.isStarting || ui.isStopping
                com.androidengineers.agent_quickstart_android.data.VoiceLanguage.entries.forEach {
                    option ->
                    val available =
                        !option.usesMurf ||
                            com.androidengineers.agent_quickstart_android.BuildConfig.MURF_API_KEY
                                .isNotBlank()
                    OutlinedButton(
                        onClick = { onLanguage(option) },
                        enabled = !languageLocked && available,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            (if (language == option) "✓ " else "") +
                                option.label +
                                if (option.usesMurf) " · ARES + Murf" else " · ARES + MiniMax"
                        )
                    }
                }
                Text(
                    if (languageLocked) "End your conversation to change language."
                    else if (
                        com.androidengineers.agent_quickstart_android.BuildConfig.MURF_API_KEY
                            .isBlank()
                    )
                        "Hindi requires MURF_API_KEY in local.properties, then rebuild."
                    else "Your selection is saved for the next conversation.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    if (ui.isConfigured)
                        "Your Agora demo is configured. Voice connects directly to your own project."
                    else ui.configMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "The complete published agenda is available offline. Voice needs internet and your configured Agora project. No résumé or LinkedIn PDF is connected in this build.",
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(
                    onClick = {
                        setup = false
                        onAgenda()
                    }
                ) {
                    Text("Explore the agenda")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
}

/** Decorative at rest; animation is enabled only for an actual connected agent state. */
@Composable
fun VoiceWave(modifier: Modifier = Modifier, active: Boolean) {
    val phase =
        if (active) {
            val transition = rememberInfiniteTransition(label = "Connected voice")
            val value by
                transition.animateFloat(
                    0f,
                    (2 * PI).toFloat(),
                    infiniteRepeatable(tween(3000, easing = LinearEasing)),
                    label = "Voice movement",
                )
            value
        } else 0f
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        for (layer in 0..3) {
            val path = Path()
            val center = h / 2
            val amplitude = h * (0.18f + layer * 0.025f)
            for (i in 0..160) {
                val x = w * i / 160
                val t = i / 160f
                val envelope = exp(-((t - 0.5f) / 0.24f).pow(2))
                val y =
                    center +
                        sin(t * PI.toFloat() * 8 + phase + layer * 0.35f) * amplitude * envelope
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            for (i in 160 downTo 0) {
                val x = w * i / 160
                val t = i / 160f
                val envelope = exp(-((t - 0.5f) / 0.24f).pow(2))
                val y =
                    center -
                        sin(t * PI.toFloat() * 8 + phase + layer * 0.35f) * amplitude * envelope
                path.lineTo(x, y)
            }
            path.close()
            drawPath(
                path,
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Lavender.copy(alpha = 0.17f),
                        Color(0xFFB3AEED).copy(alpha = 0.24f),
                        Color.Transparent,
                    )
                ),
            )
        }
    }
}

@Composable
private fun VoiceSculpture(state: AgentVisualState, modifier: Modifier = Modifier) {
    val active =
        state == AgentVisualState.LISTENING ||
            state == AgentVisualState.THINKING ||
            state == AgentVisualState.SPEAKING
    val phase =
        if (active) {
            val transition = rememberInfiniteTransition(label = "Agent state")
            val value by
                transition.animateFloat(
                    0f,
                    (2 * PI).toFloat(),
                    infiniteRepeatable(
                        tween(
                            if (state == AgentVisualState.SPEAKING) 2400 else 5200,
                            easing = LinearEasing,
                        )
                    ),
                    label = "State movement",
                )
            value
        } else 0f
    Canvas(modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        val radius = min(size.width, size.height) * 0.33f
        val path = Path()
        for (i in 0..240) {
            val angle = 2 * PI * i / 240
            val r = radius * (1 + 0.13 * sin(3 * angle + phase) + 0.06 * cos(2 * angle - phase))
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(
            path,
            Brush.radialGradient(
                listOf(Color(0xFFDDC7F5), Color(0xFF9C80CA), Color(0xFF514569)),
                center = center.copy(y = center.y + radius * 0.7f),
                radius = radius * 2.4f,
            ),
        )
        drawPath(
            path,
            Brush.linearGradient(listOf(Color(0xFFE1D5FF), Color(0xFF9181B6), Color(0xFFDDC7F5))),
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroidBuddyVoiceScreen(
    ui: ConversationUiState,
    onCollapse: () -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onMute: () -> Unit,
    onDraft: (String) -> Unit,
    onSend: (Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var showTranscript by rememberSaveable { mutableStateOf(false) }
    var showText by rememberSaveable { mutableStateOf(false) }
    var showDetails by rememberSaveable { mutableStateOf(false) }
    BackHandler { onCollapse() }
    val view = LocalView.current
    val activity =
        generateSequence(LocalContext.current) {
                (it as? android.content.ContextWrapper)?.baseContext
            }
            .filterIsInstance<android.app.Activity>()
            .firstOrNull()
    DisposableEffect(view, activity) {
        val controller = activity?.let { WindowCompat.getInsetsController(it.window, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (previousStatus != null) controller.isAppearanceLightStatusBars = previousStatus
            if (previousNavigation != null)
                controller.isAppearanceLightNavigationBars = previousNavigation
        }
    }
    DroidBuddyTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = VoiceCharcoal,
            contentColor = Color(0xFFF5F2FA),
        ) {
            BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
                val compact = maxHeight < 650.dp
                Column(
                    Modifier.fillMaxSize()
                        .widthIn(max = 720.dp)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onCollapse) {
                            Icon(
                                Icons.Outlined.KeyboardArrowDown,
                                "Minimize conversation",
                                tint = Color(0xFFE4DEEE),
                            )
                        }
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "DroidBuddy",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                            )
                            Text(
                                "droidcon India 2026",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFADA5BA),
                            )
                        }
                        IconButton(onClick = { showDetails = true }) {
                            Icon(
                                Icons.Outlined.MoreHoriz,
                                "Connection details",
                                tint = Color(0xFFE4DEEE),
                            )
                        }
                    }
                    Column(
                        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(if (compact) 12.dp else 30.dp))
                        Text(
                            "YOUR CONFERENCE COMPANION",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.8.sp,
                            color = Color(0xFFB7ACC8),
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        VoiceSculpture(
                            if (
                                !ui.micRequestedEnabled &&
                                    ui.agentVisualState == AgentVisualState.LISTENING
                            )
                                AgentVisualState.IDLE
                            else ui.agentVisualState,
                            Modifier.size(if (compact) 180.dp else 236.dp),
                        )
                        val label =
                            when {
                                ui.isStopping -> "Ending conversation…"
                                ui.isStarting -> "Connecting…"
                                !ui.inConversation ->
                                    if (ui.errorMessage != null) "Let’s try again."
                                    else "Ready when you are."
                                !ui.micRequestedEnabled &&
                                    ui.agentVisualState == AgentVisualState.LISTENING ->
                                    "Microphone muted"
                                ui.agentVisualState == AgentVisualState.LISTENING ->
                                    "I’m listening."
                                ui.agentVisualState == AgentVisualState.THINKING ->
                                    "Thinking it through…"
                                ui.agentVisualState == AgentVisualState.SPEAKING ->
                                    "Here’s what I found."
                                ui.agentVisualState == AgentVisualState.DISCONNECTED ->
                                    "Connection interrupted"
                                ui.agentVisualState == AgentVisualState.WAITING ->
                                    "Your agent is joining…"
                                else -> "Go ahead, I’m here."
                            }
                        Text(
                            label,
                            fontSize = 28.sp,
                            lineHeight = 35.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFFAF7FF),
                        )
                        Text(
                            when {
                                ui.isStarting -> "Setting up your private voice channel"
                                ui.agentVisualState == AgentVisualState.SPEAKING ->
                                    "You can interrupt naturally, any time."
                                ui.inConversation && !ui.micRequestedEnabled ->
                                    "Unmute below, or send a typed message."
                                ui.inConversation -> "Take your time. A good question is worth it."
                                else -> "Sessions, Android, and your next idea."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFAAA2B8),
                            modifier = Modifier.padding(top = 10.dp, bottom = 24.dp),
                        )
                        val current = ui.liveTranscript ?: ui.transcriptHistory.lastOrNull()
                        Surface(
                            color = Color(0xFF24222D),
                            shape = RoundedCornerShape(24.dp),
                            border =
                                androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF35313F)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (current?.speaker == TranscriptSpeaker.USER)
                                            Icons.Outlined.Mic
                                        else Icons.Outlined.GraphicEq,
                                        null,
                                        tint = Lavender,
                                        modifier = Modifier.size(17.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (current == null)
                                            (if (ui.textDraft.isNotBlank()) "YOUR DRAFT"
                                            else "TRY ASKING")
                                        else if (current.speaker == TranscriptSpeaker.USER) "YOU"
                                        else "DROIDBUDDY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFC9BBDC),
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (current != null)
                                        TextButton(
                                            onClick = { showTranscript = true },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.heightIn(min = 48.dp),
                                        ) {
                                            Text("View all", color = Color(0xFFD9C8F0))
                                        }
                                }
                                Text(
                                    current?.text?.takeLast(700)
                                        ?: ui.textDraft.ifBlank {
                                            "Which sessions should I explore if I’m interested in Android and AI?"
                                        },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFF0EBF7),
                                    modifier =
                                        Modifier.heightIn(max = 140.dp)
                                            .verticalScroll(rememberScrollState()),
                                )
                            }
                        }
                        if (ui.errorMessage != null || ui.warningMessage != null) {
                            Surface(
                                color = Color(0xFF38262C),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        ui.errorMessage ?: ui.warningMessage.orEmpty(),
                                        color = Color(0xFFFFCCC6),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier =
                                            Modifier.heightIn(max = 120.dp)
                                                .verticalScroll(rememberScrollState()),
                                    )
                                    TextButton(onDismiss, contentPadding = PaddingValues(0.dp)) {
                                        Text("Dismiss", color = Color(0xFFFFDAD5))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                    Column(
                        Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (!ui.inConversation && !ui.isStarting) {
                            Button(
                                onStart,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE3D4FA),
                                        contentColor = VoiceCharcoal,
                                    ),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Icon(Icons.Outlined.Mic, null)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    if (ui.isConfigured) "Start conversation"
                                    else "Check voice setup"
                                )
                            }
                        } else
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    FilledTonalIconButton(
                                        onMute,
                                        enabled = ui.inConversation && !ui.isStopping,
                                        modifier = Modifier.size(58.dp),
                                        colors =
                                            IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = Color(0xFF302D39),
                                                contentColor = Color.White,
                                            ),
                                    ) {
                                        Icon(
                                            if (ui.micRequestedEnabled) Icons.Outlined.Mic
                                            else Icons.Outlined.MicOff,
                                            if (ui.micRequestedEnabled) "Mute microphone"
                                            else "Unmute microphone",
                                        )
                                    }
                                    Text(
                                        if (ui.micRequestedEnabled) "Mute" else "Unmute",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFC3BBCE),
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    FilledIconButton(
                                        onEnd,
                                        enabled =
                                            ui.inConversation && !ui.isStopping && !ui.isStarting,
                                        modifier = Modifier.size(74.dp),
                                        colors =
                                            IconButtonDefaults.filledIconButtonColors(
                                                containerColor = Color(0xFFE85E58),
                                                contentColor = Color.White,
                                            ),
                                    ) {
                                        Icon(
                                            Icons.Outlined.Close,
                                            "End conversation",
                                            Modifier.size(30.dp),
                                        )
                                    }
                                    Text(
                                        "End",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFC3BBCE),
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    FilledTonalIconButton(
                                        onClick = { showText = true },
                                        enabled = ui.canSendText && !ui.isStopping,
                                        modifier = Modifier.size(58.dp),
                                        colors =
                                            IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = Color(0xFF302D39),
                                                contentColor = Color.White,
                                            ),
                                    ) {
                                        Icon(Icons.Outlined.Keyboard, "Type a message")
                                    }
                                    Text(
                                        "Type",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFC3BBCE),
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                            }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = { showTranscript = true }) {
                                Text("Transcript", color = Color(0xFFD0C5DE))
                            }
                            if (ui.textDraft.isNotBlank() && ui.canSendText)
                                TextButton(onClick = { showText = true }) {
                                    Text("Send your draft", color = Color(0xFFDFC9FF))
                                }
                        }
                        Text(
                            "Powered by Agora Conversational AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFAAA0B8),
                        )
                    }
                }
            }
        }
        if (showText)
            ModalBottomSheet(
                onDismissRequest = { showText = false },
                containerColor = VoiceCharcoal,
            ) {
                Box(Modifier.padding(20.dp).imePadding()) {
                    ConversationTextControls(ui, onDraft, onSend)
                }
            }
        if (showTranscript)
            ModalBottomSheet(
                onDismissRequest = { showTranscript = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = VoiceCharcoal,
                contentColor = Color(0xFFF5F2FA),
            ) {
                val turns = ui.transcriptHistory + listOfNotNull(ui.liveTranscript)
                val scroll = rememberLazyListState()
                // Follow new turns only when already at the end; don't pull readers away
                // from earlier messages on every streaming transcript update.
                LaunchedEffect(turns.size) {
                    val lastVisible = scroll.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    if (turns.isNotEmpty() && (lastVisible == null || lastVisible >= turns.lastIndex - 1)) {
                        scroll.animateScrollToItem(turns.lastIndex)
                    }
                }
                Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Your conversation",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                        )
                        IconButton(onClick = { showTranscript = false }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close transcript")
                        }
                    }
                    HorizontalDivider(color = Color(0xFF35313E))
                    LazyColumn(
                        Modifier.fillMaxWidth().weight(1f).testTag("transcript-list"),
                        state = scroll,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        if (turns.isEmpty())
                            item {
                                Text(
                                    "Your live transcript will appear here after you start talking.",
                                    color = Color(0xFFBDB7C8),
                                )
                            }
                        items(turns, key = { it.key }) { turn ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    if (turn.speaker == TranscriptSpeaker.USER) "YOU" else "DROIDBUDDY",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Lavender,
                                )
                                Text(turn.text, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFF5F2FA))
                                if (turn.status == TranscriptTurnStatus.INTERRUPTED)
                                    Text(
                                        "Interrupted",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFBDB7C8),
                                    )
                            }
                        }
                    }
                }
            }
        if (showDetails)
            ModalBottomSheet(
                onDismissRequest = { showDetails = false },
                containerColor = VoiceCharcoal,
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Connection details", style = MaterialTheme.typography.headlineSmall)
                    Text("${ui.rtcConnectionLabel} · ${ui.rtmConnectionLabel}")
                    Text("Microphone ${if(ui.micRequestedEnabled)"enabled" else "muted"}")
                    if (ui.channelName != null)
                        Text(
                            "Channel: ${ui.channelName}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    if (!ui.isConfigured)
                        Text(
                            ui.configMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    ui.issues.takeLast(5).forEach {
                        Text(
                            "${it.source}: ${it.message}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeDesignPreview() {
    DroidBuddyTheme(darkTheme = false) {
        DroidBuddyHomeScreen(ConversationUiState(), AgendaState(loading = false), {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun VoiceDesignPreview() {
    DroidBuddyVoiceScreen(
        ConversationUiState(inConversation = true, agentVisualState = AgentVisualState.LISTENING),
        {},
        {},
        {},
        {},
        {},
        { _, _ -> },
        {},
    )
}
