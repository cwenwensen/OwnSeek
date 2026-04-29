package com.deepseek.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 简单的 Markdown 渲染组件
 *
 * 支持的格式：
 * - **粗体** / *斜体*
 * - `行内代码`
 * - ```代码块```
 * - 无序列表（- / * 开头）
 * - 有序列表（数字. 开头）
 *
 * 注意：如果引入了 compose-markdown 库有兼容性问题，可用此组件作为后备方案。
 * 如需更强的 Markdown 渲染（表格、链接等），替换为 compose-markdown 库的 MarkdownText。
 */
@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val lines = content.split("\n")
        var inCodeBlock = false
        val codeBlockLines = mutableListOf<String>()

        for (line in lines) {
            when {
                // 代码块开始/结束
                line.trimStart().startsWith("```") -> {
                    if (inCodeBlock) {
                        // 代码块结束：渲染累积的代码
                        renderCodeBlock(codeBlockLines.joinToString("\n"))
                        codeBlockLines.clear()
                        inCodeBlock = false
                    } else {
                        inCodeBlock = true
                    }
                }

                inCodeBlock -> {
                    codeBlockLines.add(line)
                }

                // 空行
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 无序列表、有序列表、普通文本
                else -> {
                    val trimmed = line.trimStart()
                    when {
                        trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                            renderListItem(trimmed.removePrefix("- ").removePrefix("* ").trim())
                        }
                        Regex("""^\d+\.\s""").containsMatchIn(trimmed) -> {
                            renderListItem(trimmed.replaceFirst(Regex("""^\d+\.\s"""), ""))
                        }
                        else -> {
                            renderTextLine(line)
                        }
                    }
                }
            }
        }

        // 文件末尾未关闭的代码块
        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            renderCodeBlock(codeBlockLines.joinToString("\n"))
        }
    }
}

@Composable
private fun renderTextLine(line: String) {
    Text(
        text = buildAnnotatedString {
            parseInlineMarkdown(line)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun renderListItem(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
        Text(
            text = "  •  ",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = buildAnnotatedString {
                parseInlineMarkdown(text)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun renderCodeBlock(code: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code.trimEnd(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 解析行内 Markdown：**粗体**、*斜体*、`行内代码`
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.parseInlineMarkdown(text: String) {
    // 按粗体、斜体、代码的正则分片处理
    val regex = Regex("""(\*\*.*?\*\*)|(\*.*?\*)|(`.*?`)""")
    var lastIndex = 0

    for (match in regex.findAll(text)) {
        // 匹配前的普通文本
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }

        val matched = match.value
        when {
            matched.startsWith("**") && matched.endsWith("**") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(matched.removePrefix("**").removeSuffix("**"))
                }
            }
            matched.startsWith("*") && matched.endsWith("*") && !matched.startsWith("**") -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(matched.removePrefix("*").removeSuffix("*"))
                }
            }
            matched.startsWith("`") && matched.endsWith("`") -> {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)) {
                    append(matched.removePrefix("`").removeSuffix("`"))
                }
            }
            else -> append(matched)
        }

        lastIndex = match.range.last + 1
    }

    // 剩余文本
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}
