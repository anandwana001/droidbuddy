package com.androidengineers.agent_quickstart_android.agenda

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AgendaScreen(
    state: AgendaState,
    onSave: (String) -> Unit,
    onRetry: () -> Unit,
    onAsk: (String) -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var room by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val agenda = state.agenda
    val selected = agenda?.sessions?.firstOrNull { it.id == selectedId }
    val source = LocalUriHandler.current
    BackHandler(selected != null) { selectedId = null }
    if (selected != null) {
        SessionDetails(
            selected,
            state,
            { selectedId = null },
            { onSave(selected.id) },
            { onAsk("Tell me about the session ‘${selected.title}’ and who it is for.") },
        )
        return
    }
    val filtered =
        agenda?.sessions.orEmpty().filter {
            it.matches(search, room, filter == "Talks") &&
                (filter != "Saved" || it.id in state.saved)
        }
    LazyColumn(
        Modifier.fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFFFAF7F5))
            .statusBarsPadding()
            .imePadding()
            .widthIn(max = 900.dp),
        contentPadding = PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "DROIDCON INDIA 2026",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            Text("Your Session Plan", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "20 November · Bengaluru\nYour conference, at a glance. All times IST.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            OutlinedTextField(
                search,
                { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search sessions, speakers, topics") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                listOf("All", "Talks", "Saved").forEach { label ->
                    FilterChip(filter == label, { filter = label }, { Text(label) })
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                FilterChip(room.isEmpty(), { room = "" }, { Text("All rooms") })
                agenda
                    ?.sessions
                    .orEmpty()
                    .flatMap { it.rooms }
                    .distinct()
                    .forEach { label ->
                        FilterChip(
                            room == label,
                            { room = if (room == label) "" else label },
                            { Text(label) },
                        )
                    }
            }
        }
        if (state.loading) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        if (state.error != null)
            item {
                Text(state.error)
                TextButton(onRetry) { Text("Try again") }
            }
        if (agenda != null)
            item {
                Text(
                    "${filtered.size} entries · ${agenda.speakerCount} speakers\nPublished schedule · Snapshot ${agenda.importedAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = { source.openUri(agenda.sourceUrl) },
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text("Check the latest official agenda ↗")
                }
            }
        if (!state.loading && filtered.isEmpty())
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            if (filter == "Saved") "Your day starts here."
                            else "No sessions found.",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            if (filter == "Saved")
                                "Bookmark sessions to keep them here, even offline."
                            else "Try another topic or clear the room filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        filtered
            .groupBy { it.startTime }
            .forEach { (time, sessions) ->
                sessions.forEach { session ->
                    item(key = session.id) {
                        SessionItem(
                            session,
                            session.id in state.saved,
                            { selectedId = session.id },
                            { onSave(session.id) },
                        )
                    }
                }
            }
        item {
            Text(
                "Schedule details can change. Saved sessions stay on this device. DroidBuddy is an independent companion; no official endorsement is implied.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SessionItem(
    session: ConferenceSession,
    saved: Boolean,
    onOpen: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            Modifier.width(53.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                session.startLabel().replace(" ", "\n"),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF39343F),
                modifier = Modifier.padding(top = 10.dp),
            )
            Spacer(Modifier.height(10.dp))
            Box(Modifier.width(1.dp).weight(1f).background(Color(0xFFE1DAE5)))
        }
        Surface(
            Modifier.weight(1f).clickable(onClick = onOpen),
            shape = RoundedCornerShape(20.dp),
            color = if (session.service) Color(0xFFF0EBE9) else Color(0xFFECE5F8),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        session.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF27232E),
                    )
                    IconButton(onClick = onSave, modifier = Modifier.size(48.dp)) {
                        Icon(
                            if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            if (saved) "Unsave ${session.title}" else "Save ${session.title}",
                            tint = Color(0xFF494052),
                        )
                    }
                }
                if (session.speakers.isNotEmpty())
                    Text(
                        session.speakers.joinToString(" · ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF514957),
                    )
                Text(
                    session.rooms.joinToString(" · ") + " · ${session.durationMinutes()} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF514957),
                )
            }
        }
    }
}

@Composable
private fun SessionDetails(
    session: ConferenceSession,
    state: AgendaState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onAsk: () -> Unit,
) {
    val conflicts =
        state.agenda?.sessions.orEmpty().filter { it.id in state.saved && session.overlaps(it) }
    LazyColumn(
        Modifier.fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFFFAF7F5))
            .statusBarsPadding()
            .widthIn(max = 900.dp),
        contentPadding = PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            TextButton(onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                Spacer(Modifier.width(8.dp))
                Text("Agenda")
            }
        }
        item {
            Text(
                session.rooms.joinToString(" · "),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(12.dp))
            Text(session.title, style = MaterialTheme.typography.headlineLarge)
        }
        item {
            Text(session.timeLabel(), style = MaterialTheme.typography.titleMedium)
            Text(
                "${session.durationMinutes()} minutes${if (session.level.isNotBlank()) " · ${session.level}" else ""}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (conflicts.isNotEmpty())
            item {
                Text(
                    "Overlaps with saved: ${conflicts.joinToString { it.title }}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onSave, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                    Text(if (session.id in state.saved) "Saved · Remove" else "Save session")
                }
                OutlinedButton(onAsk, modifier = Modifier.heightIn(min = 52.dp)) {
                    Icon(Icons.Outlined.Mic, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ask AI")
                }
            }
        }
        if (session.abstract.isNotBlank())
            item { Text(session.abstract, style = MaterialTheme.typography.bodyLarge) }
        session.speakers.forEach { speaker ->
            item(key = speaker.id) {
                HorizontalDivider()
                Spacer(Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        speaker.photoUrl,
                        contentDescription = "Portrait of ${speaker.name}",
                        modifier = Modifier.size(60.dp).clip(CircleShape),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(speaker.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            speaker.headline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (speaker.bio.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(speaker.bio, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Text(
                "Published by the event’s Sessionize feed. Snapshot ${state.agenda?.importedAt?.take(10)}. Check the event website for changes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
