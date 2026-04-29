package com.deepseek.chat.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepseek.chat.domain.model.ResponseFormatType
import com.deepseek.chat.domain.model.ThinkingMode
import com.deepseek.chat.ui.components.ApiKeyField
import com.deepseek.chat.ui.settings.components.DropdownSetting
import com.deepseek.chat.ui.settings.components.NumberFieldSetting
import com.deepseek.chat.ui.settings.components.SliderSetting
import com.deepseek.chat.ui.settings.components.ToggleSetting
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("调试设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetDefaults() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "恢复默认")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        val currentState = state
        if (currentState == null) {
            // DataStore 尚未返回真实数据，显示加载指示器避免默认值闪烁
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SettingsContent(
                state = currentState,
                modifier = Modifier.padding(padding),
                onApiKeyChange = { viewModel.updateApiKey(it) },
                onTemperatureChange = { viewModel.updateTemperature(it) },
                onTopPChange = { viewModel.updateTopP(it) },
                onMaxTokensChange = { viewModel.updateMaxTokens(it) },
                onPresencePenaltyChange = { viewModel.updatePresencePenalty(it) },
                onFrequencyPenaltyChange = { viewModel.updateFrequencyPenalty(it) },
                onSeedChange = { viewModel.updateSeed(it) },
                onThinkingModeChange = { viewModel.updateThinkingMode(it) },
                onStreamChange = { viewModel.updateStream(it) },
                onResponseFormatChange = { viewModel.updateResponseFormat(it) }
            )
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    modifier: Modifier = Modifier,
    onApiKeyChange: (String) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onTopPChange: (Float) -> Unit,
    onMaxTokensChange: (Float) -> Unit,
    onPresencePenaltyChange: (Float) -> Unit,
    onFrequencyPenaltyChange: (Float) -> Unit,
    onSeedChange: (String) -> Unit,
    onThinkingModeChange: (ThinkingMode) -> Unit,
    onStreamChange: (Boolean) -> Unit,
    onResponseFormatChange: (ResponseFormatType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // ====== API 密钥 ======
        ApiKeyField(
            apiKey = state.apiKey,
            onApiKeyChange = onApiKeyChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ====== 常用选项（高频调整） ======
        SectionHeader("常用选项")

        DropdownSetting(
            label = "思考模式",
            description = "思考模式消耗更多 token，但在数学/代码任务上更准确",
            options = ThinkingMode.entries.map { it.displayName to it },
            selectedValue = state.thinkingMode,
            onOptionSelected = onThinkingModeChange
        )

        ToggleSetting(
            label = "流式输出",
            description = "开启后 AI 回复会逐字显示，关闭后等待完整回复一次性返回",
            checked = state.stream,
            onCheckedChange = onStreamChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ====== 生成参数 ======
        SectionHeader("生成参数")

        SliderSetting(
            label = "Temperature",
            description = "控制回复的随机性。值越高越有创意（建议 1.0），值越低越严谨（建议 0.1）",
            value = state.temperature,
            onValueChange = onTemperatureChange,
            valueRange = 0f..2f,
            formatValue = { String.format("%.2f", it) }
        )

        SliderSetting(
            label = "Top P",
            description = "词汇选择的核采样范围。与 Temperature 配合使用",
            value = state.topP,
            onValueChange = onTopPChange,
            valueRange = 0f..1f,
            formatValue = { String.format("%.2f", it) }
        )

        // Max Tokens: 使用滑块进行粗调 + 支持修改
        SliderSetting(
            label = "最大 Tokens",
            description = "限制回复最大长度。1 token ≈ 1 个中文字或 0.75 个英文单词",
            value = state.maxTokens,
            onValueChange = onMaxTokensChange,
            valueRange = 16f..131072f,
            steps = 5,
            formatValue = { v ->
                val n = v.roundToInt()
                when {
                    n >= 1000 -> "${n / 1000}K"
                    else -> n.toString()
                }
            }
        )

        SliderSetting(
            label = "Presence Penalty",
            description = "正数鼓励模型谈论新话题，负数允许重复话题",
            value = state.presencePenalty,
            onValueChange = onPresencePenaltyChange,
            valueRange = -2f..2f,
            formatValue = { String.format("%.1f", it) }
        )

        SliderSetting(
            label = "Frequency Penalty",
            description = "正数减少词汇重复，负数允许更多重复",
            value = state.frequencyPenalty,
            onValueChange = onFrequencyPenaltyChange,
            valueRange = -2f..2f,
            formatValue = { String.format("%.1f", it) }
        )

        NumberFieldSetting(
            label = "随机种子 (可选)",
            description = "设置后，相同种子 + 相同输入会产生相同输出",
            value = state.seedText,
            onValueChange = onSeedChange,
            isError = state.seedError != null,
            errorMessage = state.seedError
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ====== 高级选项 ======
        SectionHeader("高级选项")

        DropdownSetting(
            label = "回复格式",
            description = "选择 JSON 格式时，模型会尽力输出合法的 JSON 字符串",
            options = ResponseFormatType.entries.map { it.displayName to it },
            selectedValue = state.responseFormat,
            onOptionSelected = onResponseFormatChange
        )

        // 底部留白
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}
