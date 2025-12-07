package com.brycewg.asrkb.translate

/**
 * 翻译引擎接口
 */
interface TranslateEngine {
    /**
     * 执行翻译
     * @param text 待翻译文本
     * @param sourceLang 源语言代码（如 "en", "zh", "auto" 等）
     * @param targetLang 目标语言代码
     * @return 翻译结果
     */
    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslateResult
}
