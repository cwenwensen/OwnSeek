package com.deepseek.chat.domain.model

/**
 * DeepSeek V4 Pro 的思考模式
 *
 * NON_THINKING  - 不思考，最快，适合日常对话
 * THINKING      - 思考，在数学/代码任务上更准确
 * THINKING_MAX  - 深度思考，最佳准确率但消耗最多 token
 */
enum class ThinkingMode(val displayName: String, val apiValue: String) {
    NON_THINKING("不思考", "non-thinking"),
    THINKING("思考", "thinking"),
    THINKING_MAX("深度思考", "thinking_max");

    companion object {
        fun fromApiValue(value: String): ThinkingMode =
            entries.find { it.apiValue == value } ?: NON_THINKING
    }
}
