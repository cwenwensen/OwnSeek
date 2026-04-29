package com.deepseek.chat.domain.model

/**
 * 回复格式
 * TEXT        - 普通文本回复
 * JSON_OBJECT - 要求模型返回合法 JSON
 */
enum class ResponseFormatType(val displayName: String, val apiValue: String) {
    TEXT("文本", "text"),
    JSON_OBJECT("JSON", "json_object");

    companion object {
        fun fromApiValue(value: String): ResponseFormatType =
            entries.find { it.apiValue == value } ?: TEXT
    }
}
