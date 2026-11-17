package com.androidengineers.agent_quickstart_android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidengineers.agent_quickstart_android.model.ConversationUiState

/** Typed instructions and exact read-aloud use the existing Agora think/speak APIs. */
@Composable
internal fun ConversationTextControls(
    ui: ConversationUiState,
    onTextChanged: (String) -> Unit,
    onSend: (speak: Boolean, append: Boolean) -> Unit,
) {
    var speak by rememberSaveable { mutableStateOf(false) }
    var append by rememberSaveable { mutableStateOf(true) }
    val enabled = ui.canSendText && !ui.isStopping && !ui.isSendingText
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Message DroidBuddy", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(!speak, { speak = false }, { Text("Ask DroidBuddy") }, enabled = enabled)
            FilterChip(speak, { speak = true }, { Text("Read aloud") }, enabled = enabled)
        }
        Text(
            if (speak) "Read your text exactly as written."
            else "Send a question and listen to the response.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            ui.textDraft,
            onTextChanged,
            enabled = enabled,
            label = { Text(if (speak) "Text to read" else "Your message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 5,
            shape = RoundedCornerShape(18.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(append, { append = it }, enabled = enabled)
            Text("Queue instead of interrupting", style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = { onSend(speak, append) },
            enabled = enabled && ui.textDraft.isNotBlank(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        ) {
            Text(if (ui.isSendingText) "Sending…" else "Send")
        }
        ui.textActionStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(12.dp))
    }
}
