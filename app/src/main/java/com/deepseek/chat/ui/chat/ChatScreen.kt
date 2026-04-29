package com.deepseek.chat.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepseek.chat.domain.model.StreamState
import com.deepseek.chat.ui.chat.components.ChatInputBar
import com.deepseek.chat.ui.chat.components.DrawerContent
import com.deepseek.chat.ui.chat.components.MessageList
import com.deepseek.chat.ui.chat.components.TokenUsageBar
import com.deepseek.chat.ui.chat.components.TypingIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var navigatingToSettings by remember { mutableStateOf(false) }

    // 删除确认弹窗
    state.showDeleteDialog?.let { convId ->
        val conv = state.conversations.find { it.id == convId }
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("确认删除") },
            text = { Text("确定要删除对话「${conv?.title ?: "新对话"}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteConversation(convId) }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("取消")
                }
            }
        )
    }

    // 清空确认弹窗
    if (state.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleClearDialog(false) },
            title = { Text("确认清空") },
            text = { Text("确定要清空所有对话记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearChat()
                    viewModel.toggleClearDialog(false)
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleClearDialog(false) }) {
                    Text("取消")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                conversations = state.historyConversations,
                currentConversationId = state.currentConversationId,
                onSettingsClick = {
                    if (!navigatingToSettings) {
                        navigatingToSettings = true
                        scope.launch {
                            drawerState.close()
                            onNavigateToSettings()
                        }
                    }
                },
                onConversationClick = { convId ->
                    scope.launch { drawerState.close() }
                    viewModel.switchConversation(convId)
                },
                onDeleteConversation = { convId ->
                    viewModel.showDeleteDialog(convId)
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                TopAppBar(
                    title = { Text(state.displayTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "菜单"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.newConversation() }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "新建对话"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 错误提示
                state.errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text("关闭")
                            }
                        }
                    }
                }

                // 消息列表
                MessageList(
                    messages = state.messages,
                    emptyHint = "DeepSeek V4 Pro\n有问题随时问我",
                    isStreaming = state.isStreaming,
                    thinkingMode = state.thinkingMode,
                    modifier = Modifier.weight(1f)
                )

                // 打字指示器
                if (state.isStreaming) {
                    TypingIndicator()
                }

                // Token 用量
                state.tokenUsage?.let { usage ->
                    TokenUsageBar(usage = usage)
                }

                // 输入栏
                ChatInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSend = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    onStop = { viewModel.cancelStream() },
                    isStreaming = state.isStreaming,
                    sendEnabled = state.sendEnabled && inputText.isNotBlank()
                )
            }
        }
    }
}
