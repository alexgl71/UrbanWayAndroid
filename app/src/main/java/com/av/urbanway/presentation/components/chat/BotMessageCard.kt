package com.av.urbanway.presentation.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.av.urbanway.presentation.components.ChatChoiceChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BotAction(
    val id: String,
    val label: String,
    val onInvoke: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BotMessageCard(
    messageId: String,
    isLastMessage: Boolean = false,
    onExpandClick: (() -> Unit)? = null,
    actions: List<BotAction> = emptyList(),
    lastUpdatedEpochMillis: Long? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alwaysReveal = isLastMessage && actions.isNotEmpty()
    var reveal by remember(messageId, alwaysReveal, actions) { mutableStateOf(alwaysReveal) }

    LaunchedEffect(reveal, messageId, alwaysReveal) {
        if (alwaysReveal) {
            reveal = true
        } else if (reveal) {
            kotlinx.coroutines.delay(5000)
            reveal = false
        }
    }

    BotMessageContainer(
        isLastMessage = isLastMessage,
        onExpandClick = onExpandClick,
        onBodyClick = if (!alwaysReveal && actions.isNotEmpty()) ({ reveal = !reveal }) else null,
        showActions = reveal && actions.isNotEmpty(),
        actions = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                actions.forEach { act ->
                    ChatChoiceChip(text = act.label, onClick = {
                        reveal = false
                        act.onInvoke()
                    })
                }
            }
        },
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth()) {
            content()
        }
    }
}
