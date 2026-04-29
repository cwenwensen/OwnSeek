package com.deepseek.chat.ui.chat.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * 打字指示器 — 三个跳动的圆点
 * 当 AI 正在生成回复时显示
 */
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DeepSeek 正在思考",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )

        repeat(3) { index ->
            val alpha = infiniteRepeatableAnimation(
                animation = tween(400, delayMillis = index * 150),
                initialValue = 0.3f,
                targetValue = 1.0f,
                repetitionMode = RepeatMode.Reverse
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(6.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/**
 * 无限重复的动画（正向→反向→正向...）
 */
@Composable
private fun infiniteRepeatableAnimation(
    animation: DurationBasedAnimationSpec<Float>,
    initialValue: Float,
    targetValue: Float,
    repetitionMode: RepeatMode
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    return infiniteTransition.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = animation,
            repeatMode = repetitionMode
        ),
        label = "dotAlpha"
    ).value
}
