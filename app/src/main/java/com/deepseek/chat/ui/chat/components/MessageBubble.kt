package com.deepseek.chat.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepseek.chat.domain.model.Message
import com.deepseek.chat.domain.model.MessageRole
import com.deepseek.chat.domain.model.ThinkingMode
import com.deepseek.chat.ui.components.MarkdownText
import com.deepseek.chat.ui.theme.*

@Composable
fun MessageBubble(
    message: Message,
    thinkingMode: ThinkingMode = ThinkingMode.NON_THINKING,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    var reasoningExpanded by remember { mutableStateOf(true) }

    val reasoningLabel = when (thinkingMode) {
        ThinkingMode.THINKING_MAX -> "深度思考"
        ThinkingMode.THINKING -> "思考"
        ThinkingMode.NON_THINKING -> ""
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = if (isUser) "你" else "DeepSeek",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
        ) {
            // 深度思考内容（仅 AI 消息，灰色，可折叠）
            if (!isUser && message.reasoningContent.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(bubbleShape(isUser))
                        .background(bubbleColor(isUser))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        // 折叠/展开 标题栏
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reasoningExpanded = !reasoningExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = reasoningLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Icon(
                                if (reasoningExpanded) Icons.Filled.KeyboardArrowUp
                                else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (reasoningExpanded) "收起" else "展开",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 深度思考内容
                        AnimatedVisibility(
                            visible = reasoningExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            SelectionContainer {
                                Text(
                                    text = message.reasoningContent,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 20.sp,
                                        fontFamily = FontFamily.Default
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // 实际输出内容（仅 AI 消息，黑色，且非空）
            if (!isUser && message.content.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(bubbleShape(isUser))
                        .background(bubbleColor(isUser))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    SelectionContainer {
                        MarkdownText(content = message.content)
                    }
                }
            }

            // 用户消息气泡
            if (isUser) {
                Box(
                    modifier = Modifier
                        .clip(bubbleShape(isUser))
                        .background(bubbleColor(isUser))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = UserBubbleContent
                        )
                    }
                }
            }
        }

        // Token 用量
        if (!isUser && message.tokens != null) {
            Text(
                text = "${message.tokens} tokens",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun bubbleShape(isUser: Boolean): RoundedCornerShape {
    return if (isUser) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }
}

@Composable
private fun bubbleColor(isUser: Boolean): androidx.compose.ui.graphics.Color {
    return if (isUser) {
        UserBubbleColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
}
