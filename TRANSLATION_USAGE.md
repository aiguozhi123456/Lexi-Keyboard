# 翻译功能使用指南

## 概述

本项目已集成多个翻译 API 的支持，包括：

1. **Imme** - Imme 翻译服务
2. **Kiss** - Kiss 翻译服务
3. **DeepL** - DeepL 官方翻译 API
4. **Google Cloud Translation v2** - Google 云翻译 API v2
5. **Google Translate Free** - Google 翻译免费 API（默认）
6. **HCFY** - HCFY 翻译服务

## 快速开始

### 1. 基本使用

```kotlin
import com.brycewg.asrkb.translate.TranslateManager
import com.brycewg.asrkb.store.Prefs

// 创建翻译管理器
val prefs = Prefs(context)
val translateManager = TranslateManager(context, prefs)

// 执行翻译
val result = translateManager.translate(
    text = "Hello, world!",
    sourceLang = "en",
    targetLang = "zh"
)

if (result.isSuccess) {
    println("翻译结果: ${result.translatedText}")
} else {
    println("翻译失败: ${result.error}")
}
```

### 2. 配置翻译服务

```kotlin
import com.brycewg.asrkb.translate.TranslateVendor

// 选择翻译服务提供商
prefs.translateVendor = TranslateVendor.DEEPL

// 配置 API 密钥
prefs.translateDeepLApiKey = "your-deepl-api-key"

// 配置自定义端点（可选）
prefs.translateDeepLEndpoint = "https://api.deepl.com/v2/translate"

// 设置默认语言
prefs.translateSourceLang = "auto"  // 自动检测
prefs.translateTargetLang = "zh"    // 中文
```

### 3. 使用不同的翻译服务

#### DeepL

```kotlin
prefs.translateVendor = TranslateVendor.DEEPL
prefs.translateDeepLApiKey = "your-api-key"
// 免费版使用: https://api-free.deepl.com/v2/translate
// 专业版使用: https://api.deepl.com/v2/translate
prefs.translateDeepLEndpoint = "https://api-free.deepl.com/v2/translate"
```

#### Google Cloud Translation v2

```kotlin
prefs.translateVendor = TranslateVendor.GOOGLE_V2
prefs.translateGoogleV2ApiKey = "your-google-api-key"
```

#### Google Translate Free（无需 API Key）

```kotlin
prefs.translateVendor = TranslateVendor.GOOGLE_FREE
// 无需配置 API Key，直接使用
```

#### Imme

```kotlin
prefs.translateVendor = TranslateVendor.IMME
prefs.translateImmeApiKey = "your-imme-api-key"
prefs.translateImmeEndpoint = "https://api.imme.ai/translate"
```

#### Kiss

```kotlin
prefs.translateVendor = TranslateVendor.KISS
prefs.translateKissApiKey = "your-kiss-api-key"
prefs.translateKissEndpoint = "https://api.kiss.ai/translate"
```

#### HCFY

```kotlin
prefs.translateVendor = TranslateVendor.HCFY
prefs.translateHcfyApiKey = "your-hcfy-api-key"
prefs.translateHcfyEndpoint = "https://api.hcfy.app/translate"
```

## 语言代码

不同的翻译服务可能使用不同的语言代码格式：

### 常用语言代码

| 语言 | 代码 |
|------|------|
| 自动检测 | `auto` |
| 中文（简体） | `zh` 或 `zh-CN` |
| 中文（繁体） | `zh-TW` |
| 英语 | `en` |
| 日语 | `ja` |
| 韩语 | `ko` |
| 法语 | `fr` |
| 德语 | `de` |
| 西班牙语 | `es` |
| 俄语 | `ru` |

### DeepL 特殊说明

DeepL 使用大写语言代码，例如：
- `EN` - 英语
- `ZH` - 中文
- `JA` - 日语

代码会自动转换为大写，无需手动处理。

## API 端点说明

### 默认端点

各服务的默认端点已在代码中配置：

- **DeepL Free**: `https://api-free.deepl.com/v2/translate`
- **DeepL Pro**: `https://api.deepl.com/v2/translate`
- **Google v2**: `https://translation.googleapis.com/language/translate/v2`
- **Google Free**: `https://translate.googleapis.com/translate_a/single`

### 自定义端点

如果需要使用代理或自建服务，可以配置自定义端点：

```kotlin
prefs.translateDeepLEndpoint = "https://your-proxy.com/deepl/v2/translate"
```

## 错误处理

```kotlin
val result = translateManager.translate("Hello", "en", "zh")

if (result.isSuccess) {
    // 翻译成功
    val translatedText = result.translatedText
    val detectedLang = result.sourceLang  // 可能为 null
} else {
    // 翻译失败
    val errorMessage = result.error
    Log.e("Translation", "Error: $errorMessage")
}
```

## 检查配置状态

```kotlin
// 检查当前翻译服务是否已正确配置
if (translateManager.isConfigured()) {
    // 可以使用翻译功能
} else {
    // 需要配置 API Key 或其他必要信息
}
```

## 注意事项

1. **API Key 安全**: 请妥善保管 API Key，不要将其硬编码在代码中或提交到版本控制系统
2. **请求频率**: 注意各服务的请求频率限制，避免过于频繁的调用
3. **网络超时**: 所有翻译引擎默认超时时间为 30 秒
4. **语言支持**: 不同服务支持的语言可能不同，请参考各服务的官方文档
5. **免费额度**: 部分服务有免费额度限制，超出后可能需要付费

## 集成到键盘

如需在键盘中集成翻译功能，可以参考以下步骤：

1. 在键盘面板中添加翻译按钮
2. 获取当前输入的文本或选中的文本
3. 调用 `TranslateManager.translate()` 执行翻译
4. 将翻译结果插入到输入框或显示在界面上

示例代码：

```kotlin
// 在键盘服务中
class AsrKeyboardService : InputMethodService() {
    private lateinit var translateManager: TranslateManager
    
    override fun onCreate() {
        super.onCreate()
        val prefs = Prefs(this)
        translateManager = TranslateManager(this, prefs)
    }
    
    private suspend fun translateSelectedText() {
        val selectedText = currentInputConnection?.getSelectedText(0)?.toString()
        if (selectedText.isNullOrBlank()) {
            // 没有选中文本
            return
        }
        
        val result = translateManager.translate(
            text = selectedText,
            sourceLang = "auto",
            targetLang = "zh"
        )
        
        if (result.isSuccess) {
            // 替换选中文本为翻译结果
            currentInputConnection?.commitText(result.translatedText, 1)
        } else {
            // 显示错误提示
            Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
        }
    }
}
```

## 更多信息

- 项目架构设计: `translation_api_design.md`
- 源代码位置: `app/src/main/java/com/brycewg/asrkb/translate/`
