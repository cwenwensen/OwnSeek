package com.deepseek.chat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.chat.DeepSeekApp
import com.deepseek.chat.domain.model.ChatSettings
import com.deepseek.chat.domain.model.ResponseFormatType
import com.deepseek.chat.domain.model.ThinkingMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val dataStore = DeepSeekApp.instance.settingsDataStore

    /** 合并 API 密钥和设置参数为一个 UI 状态。初始为 null，等 DataStore 返回真实值后才有内容 */
    val uiState: StateFlow<SettingsUiState?> = combine(
        dataStore.apiKey,
        dataStore.chatSettings
    ) { apiKey, settings ->
        settings.toUiState(apiKey)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ========== 每个设置的更新方法 ==========

    fun updateApiKey(key: String) {
        viewModelScope.launch { dataStore.setApiKey(key) }
    }

    fun updateThinkingMode(mode: ThinkingMode) {
        viewModelScope.launch { dataStore.setThinkingMode(mode) }
    }

    fun updateTemperature(value: Float) {
        viewModelScope.launch { dataStore.setTemperature(value) }
    }

    fun updateTopP(value: Float) {
        viewModelScope.launch { dataStore.setTopP(value) }
    }

    fun updateMaxTokens(value: Float) {
        viewModelScope.launch { dataStore.setMaxTokens(value.toInt()) }
    }

    fun updatePresencePenalty(value: Float) {
        viewModelScope.launch { dataStore.setPresencePenalty(value) }
    }

    fun updateFrequencyPenalty(value: Float) {
        viewModelScope.launch { dataStore.setFrequencyPenalty(value) }
    }

    fun updateSeed(text: String) {
        viewModelScope.launch {
            if (text.isBlank()) {
                dataStore.setSeed(null)
            } else {
                text.toIntOrNull()?.let { dataStore.setSeed(it) }
            }
        }
    }

    fun updateStream(value: Boolean) {
        viewModelScope.launch { dataStore.setStream(value) }
    }

    fun updateResponseFormat(format: ResponseFormatType) {
        viewModelScope.launch { dataStore.setResponseFormat(format) }
    }

    fun resetDefaults() {
        viewModelScope.launch { dataStore.resetAll() }
    }
}
