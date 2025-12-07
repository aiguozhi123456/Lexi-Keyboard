# 翻译 API 接入架构设计

## 概述

为 Lexi-Keyboard 项目添加翻译功能支持，接入以下翻译 API：
1. `/imme` - Imme 翻译 API
2. `/kiss` - Kiss 翻译 API
3. `/deepl` - DeepL 翻译 API
4. `/google/language/translate/v2` - Google Cloud Translation API v2
5. `/google/translate_a/single` - Google Translate 免费 API
6. `/hcfy` - HCFY 翻译 API

## 架构设计

### 1. 包结构
```
com.brycewg.asrkb.translate/
├── TranslateEngine.kt           # 翻译引擎接口
├── TranslateVendor.kt           # 翻译服务提供商枚举
├── TranslateManager.kt          # 翻译管理器
├── ImmeTranslateEngine.kt       # Imme 翻译引擎实现
├── KissTranslateEngine.kt       # Kiss 翻译引擎实现
├── DeepLTranslateEngine.kt      # DeepL 翻译引擎实现
├── GoogleV2TranslateEngine.kt   # Google Cloud Translation API v2 实现
├── GoogleFreeTranslateEngine.kt # Google Translate 免费 API 实现
└── HcfyTranslateEngine.kt       # HCFY 翻译引擎实现
```

### 2. 核心接口设计

#### TranslateEngine 接口
```kotlin
interface TranslateEngine {
    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslateResult
}

data class TranslateResult(
    val translatedText: String,
    val sourceLang: String? = null,
    val targetLang: String,
    val error: String? = null
)
```

#### TranslateVendor 枚举
```kotlin
enum class TranslateVendor {
    IMME,
    KISS,
    DEEPL,
    GOOGLE_V2,
    GOOGLE_FREE,
    HCFY
}
```

### 3. 配置存储（Prefs.kt 扩展）

需要添加的配置项：
- 当前选择的翻译服务提供商
- 各个 API 的 endpoint 和 API key
- 默认源语言和目标语言
- 翻译功能开关

### 4. API 端点说明

基于用户提供的路径，推测各 API 的基本信息：

1. **Imme API** (`/imme`)
   - 可能是自定义或第三方翻译服务
   - 需要 API key 认证

2. **Kiss API** (`/kiss`)
   - 可能是自定义或第三方翻译服务
   - 需要 API key 认证

3. **DeepL API** (`/deepl`)
   - 官方 DeepL API
   - 需要 API key 认证
   - 支持多种语言对

4. **Google Cloud Translation API v2** (`/google/language/translate/v2`)
   - Google 官方付费 API
   - 需要 API key 认证
   - RESTful API

5. **Google Translate Free API** (`/google/translate_a/single`)
   - Google Translate 网页版使用的 API
   - 无需认证（或使用 token）
   - 可能有频率限制

6. **HCFY API** (`/hcfy`)
   - 可能是自定义或第三方翻译服务
   - 需要 API key 认证

## 实现计划

### Phase 1: 创建基础架构
- 创建 translate 包
- 定义 TranslateEngine 接口
- 定义 TranslateVendor 枚举
- 创建 TranslateManager

### Phase 2: 实现各翻译引擎
- 实现 6 个翻译引擎类
- 使用 OkHttp 进行 HTTP 请求
- 处理错误和异常

### Phase 3: 集成到 Prefs
- 添加翻译相关配置项
- 实现配置的持久化存储

### Phase 4: UI 集成（可选）
- 添加翻译设置界面
- 在键盘中添加翻译功能入口

## 注意事项

1. 所有网络请求使用协程异步执行
2. 统一的错误处理机制
3. 支持自定义 endpoint（用于代理或自建服务）
4. API key 安全存储
5. 请求超时设置
6. 支持语言代码转换（不同 API 可能使用不同的语言代码格式）
