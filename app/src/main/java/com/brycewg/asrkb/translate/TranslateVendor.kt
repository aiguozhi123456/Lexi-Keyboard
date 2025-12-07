package com.brycewg.asrkb.translate

/**
 * 翻译服务提供商枚举
 */
enum class TranslateVendor(val displayName: String) {
    IMME("Imme"),
    KISS("Kiss"),
    DEEPL("DeepL"),
    GOOGLE_V2("Google Cloud Translation v2"),
    GOOGLE_FREE("Google Translate"),
    HCFY("HCFY");

    companion object {
        fun fromString(value: String): TranslateVendor? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
