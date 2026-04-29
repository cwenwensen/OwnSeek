package com.deepseek.chat.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deepseek.chat.domain.model.Message
import com.deepseek.chat.domain.model.ThinkingMode
import kotlinx.coroutines.delay

@Composable
fun MessageList(
    messages: List<Message>,
    emptyHint: String,
    isStreaming: Boolean = false,
    thinkingMode: ThinkingMode = ThinkingMode.NON_THINKING,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var autoScrollEnabled by remember { mutableStateOf(true) }

    // 保持协程内引用的数据始终为最新
    val currentMessages by rememberUpdatedState(messages)

    // 每次新生成开始时重新启用自动滚动
    LaunchedEffect(isStreaming) {
        if (isStreaming) {
            autoScrollEnabled = true
        }
    }

    // 统一的滚动控制协程
    LaunchedEffect(isStreaming) {
        if (!isStreaming) return@LaunchedEffect

        var lastLen = 0
        while (true) {
            delay(50)
            val msgs = currentMessages
            if (msgs.isEmpty()) continue

            val len = msgs.last().content.length
            val totalItems = msgs.size
            val atBottom = !listState.canScrollForward
            val scrolling = listState.isScrollInProgress

            // 内容增长且自动滚动开启 → 滚动到底部
            if (len > lastLen && autoScrollEnabled) {
                listState.animateScrollToItem(totalItems - 1, 100_000)
            }

            // 用户在滚动中离开了底部 → 禁用自动滚动
            if (autoScrollEnabled && scrolling && !atBottom) {
                autoScrollEnabled = false
            }

            // 用户滚动停止且已在底部 → 恢复自动滚动
            if (!autoScrollEnabled && !scrolling && atBottom) {
                autoScrollEnabled = true
            }

            lastLen = len
        }
    }

    if (messages.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyHint,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message, thinkingMode = thinkingMode)
            }
        }
    }
}
