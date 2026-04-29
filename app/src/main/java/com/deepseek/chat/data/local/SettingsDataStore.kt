package com.deepseek.chat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.deepseek.chat.domain.model.ChatSettings
import com.deepseek.chat.domain.model.ResponseFormatType
import com.deepseek.chat.domain.model.ThinkingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "deepseek_settings")

/**
 * 本地存储管理器
 * 封装 Preferences DataStore，为每个设置项提供类型安全的读写方法
 */
class SettingsDataStore(private val context: Context) {

    // ========== 读取：每个设置项暴露为一个 Flow ==========

    /** API 密钥的 Flow */
    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_KEY] ?: ""
    }

    /** 完整的 ChatSettings 对象 Flow */
    val chatSettings: Flow<ChatSettings> = context.dataStore.data.map { prefs ->
        ChatSettings(
            model = prefs[KEY_MODEL] ?: "deepseek-v4-pro",
            thinkingMode = ThinkingMode.fromApiValue(
                prefs[KEY_THINKING_MODE] ?: ThinkingMode.NON_THINKING.apiValue
            ),
            temperature = prefs[KEY_TEMPERATURE] ?: 1.0f,
            topP = prefs[KEY_TOP_P] ?: 1.0f,
            maxTokens = prefs[KEY_MAX_TOKENS] ?: 4096,
            presencePenalty = prefs[KEY_PRESENCE_PENALTY] ?: 0.0f,
            frequencyPenalty = prefs[KEY_FREQUENCY_PENALTY] ?: 0.0f,
            seed = prefs[KEY_SEED],
            stream = prefs[KEY_STREAM] ?: true,
            responseFormat = ResponseFormatType.fromApiValue(
                prefs[KEY_RESPONSE_FORMAT] ?: ResponseFormatType.TEXT.apiValue
            )
        )
    }

    // ========== 写入：每个设置项一个 suspend 函数 ==========

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_API_KEY] = key
        }
    }

    suspend fun setThinkingMode(mode: ThinkingMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THINKING_MODE] = mode.apiValue
        }
    }

    suspend fun setTemperature(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TEMPERATURE] = value
        }
    }

    suspend fun setTopP(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOP_P] = value
        }
    }

    suspend fun setMaxTokens(value: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MAX_TOKENS] = value
        }
    }

    suspend fun setPresencePenalty(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRESENCE_PENALTY] = value
        }
    }

    suspend fun setFrequencyPenalty(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FREQUENCY_PENALTY] = value
        }
    }

    suspend fun setSeed(value: Int?) {
        context.dataStore.edit { prefs ->
            if (value != null) {
                prefs[KEY_SEED] = value
            } else {
                prefs.remove(KEY_SEED)
            }
        }
    }

    suspend fun setStream(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STREAM] = value
        }
    }

    suspend fun setResponseFormat(value: ResponseFormatType) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RESPONSE_FORMAT] = value.apiValue
        }
    }

    /** 一键恢复所有设置为默认值 */
    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }

    // ========== 偏好键定义 ==========

    companion object {
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_MODEL = stringPreferencesKey("model")
        private val KEY_THINKING_MODE = stringPreferencesKey("thinking_mode")
        private val KEY_TEMPERATURE = floatPreferencesKey("temperature")
        private val KEY_TOP_P = floatPreferencesKey("top_p")
        private val KEY_MAX_TOKENS = intPreferencesKey("max_tokens")
        private val KEY_PRESENCE_PENALTY = floatPreferencesKey("presence_penalty")
        private val KEY_FREQUENCY_PENALTY = floatPreferencesKey("frequency_penalty")
        private val KEY_SEED = intPreferencesKey("seed")
        private val KEY_STREAM = booleanPreferencesKey("stream")
        private val KEY_RESPONSE_FORMAT = stringPreferencesKey("response_format")
    }
}
