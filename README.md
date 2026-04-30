# OwnSeek

轻量化非官方 Android 本地客户端，自备并填写 API Key 即可直接在手机上调用 DeepSeek V4 Pro，数据仅在本地存储，无用量限制与内容审查。Kotlin + Jetpack Compose 原生构建。

> **声明：** 本项目与 DeepSeek（深度求索）公司无关，仅为第三方社区客户端。使用前请自行获取 [DeepSeek API Key](https://platform.deepseek.com/)。

## 功能

- **流式对话 (Streaming)** — 实时逐字显示 AI 回复
- **Markdown 渲染** — 支持代码高亮、表格、公式等格式
- **Markdown 预览** — 一键查看渲染后的回复内容
- **多轮对话** — 自动保存对话历史，支持切换和删除会话
- **思考模式 (Thinking)** — 支持 DeepSeek V4 Pro 的思考链推理
- **响应格式切换** — 支持 Markdown / JSON 两种输出格式
- **可调参数** — Temperature、Top-P、Max Tokens 等参数实时可调
- **Token 消耗统计** — 每次 AI 回复后显示输入/输出/总计 Token 用量栏，消息气泡显示单条消息的 Token 数，侧边栏显示每个会话的累计 Token 总数
- **暗色主题** — 开箱即用的 Material3 深色主题
- **纯本地存储** — 所有数据存储在设备本地，不上传任何第三方服务器

## 截图

<!-- TODO: 添加应用截图 -->
<!-- 将截图放入 screenshots/ 目录，然后取消下面的注释
| 对话界面 | 设置页面 | 侧边栏 |
|:---:|:---:|:---:|
| ![chat](screenshots/chat.png) | ![settings](screenshots/settings.png) | ![drawer](screenshots/drawer.png) |
-->

## 技术栈

| 类别 | 技术 |
|------|------|
| UI 框架 | Jetpack Compose + Material3 |
| 架构模式 | MVVM (ViewModel + StateFlow) |
| 网络层 | Retrofit 2 + OkHttp 4 (SSE 流式) |
| 数据持久化 | DataStore Preferences + JSON 文件 |
| 序列化 | Kotlinx Serialization |
| Markdown | compose-markdown |
| 依赖注入 | 手动 DI (轻量级) |

## 构建与运行

### 前置要求

- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17
- Android SDK 34
- 一台运行 Android 8.0 (API 26) 以上的设备或模拟器

### 编译步骤

```bash
# 克隆仓库
git clone https://github.com/你的用户名/deepseek-android-chat.git
cd deepseek-android-chat

# 编译 Debug APK
./gradlew assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/`。

### 使用方式

1. 安装 APK 到设备
2. 打开应用 → 进入**设置**页面
3. 填入你的 DeepSeek API Key（格式：`sk-xxx`）
4. 返回对话页面，开始聊天

## 项目结构

```
app/src/main/java/com/deepseek/chat/
├── data/
│   ├── api/              # Retrofit 接口、OkHttp 拦截器、SSE 流解析
│   ├── local/            # 对话存储、设置数据存储
│   ├── repository/       # 数据仓库实现
│   └── streaming/        # 流式数据解析器
├── di/                   # 手动依赖注入
├── domain/
│   ├── model/            # 领域模型
│   └── repository/       # 仓库接口
└── ui/
    ├── chat/             # 对话界面、ViewModel、组件
    ├── settings/         # 设置界面、ViewModel、组件
    ├── components/       # 通用 UI 组件
    ├── navigation/       # 导航图
    └── theme/            # Material3 主题配置
```

## 开源协议

本项目使用 [Apache License 2.0](LICENSE)。

## 免责声明

- 本项目是非官方第三方客户端，与 DeepSeek 公司无关
- 使用本应用需要自行获取 DeepSeek API Key，并遵守 DeepSeek 的 API 使用条款
- 本应用不会收集、上传任何用户数据，所有数据存储在设备本地
- API 调用费用由用户自己的 DeepSeek 账户承担
