package com.brycewg.asrkb.translate

/**
 * 翻译结果数据类
 */
data class TranslateResult(
    val translatedText: String,
    val sourceLang: String? = null,
    val targetLang: String,
    val error: String? = null
) {
    val isSuccess: Boolean
        get() = error == null && translatedText.isNotBlank()
}
