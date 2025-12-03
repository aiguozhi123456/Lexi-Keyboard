package com.brycewg.asrkb.asr

import com.brycewg.asrkb.R

/**
 * LLM (Large Language Model) vendor enumeration for AI post-processing.
 * Defines built-in providers with their endpoints, models, and configuration URLs.
 */
enum class LlmVendor(
    val id: String,
    val displayNameResId: Int,
    val endpoint: String,
    val defaultModel: String,
    val models: List<String>,
    val registerUrl: String,
    val guideUrl: String
) {
    /** SiliconFlow free tier - no API key required */
    SF_FREE(
        id = "sf_free",
        displayNameResId = R.string.llm_vendor_sf_free,
        endpoint = "https://api.siliconflow.cn/v1",
        defaultModel = "Qwen/Qwen3-8B",
        models = listOf("Qwen/Qwen3-8B", "THUDM/GLM-4-9B-0414"),
        registerUrl = "https://cloud.siliconflow.cn/i/g8thUcWa",
        guideUrl = "https://brycewg.notion.site/lexisharp-keyboard-providers-guide"
    ),

    /** OpenAI - GPT models */
    OPENAI(
        id = "openai",
        displayNameResId = R.string.llm_vendor_openai,
        endpoint = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini",
        models = listOf("gpt-4o-mini", "gpt-4o", "gpt-4.1-mini", "gpt-4.1"),
        registerUrl = "https://platform.openai.com/signup",
        guideUrl = "https://platform.openai.com/docs/quickstart"
    ),

    /** Google Gemini */
    GEMINI(
        id = "gemini",
        displayNameResId = R.string.llm_vendor_gemini,
        endpoint = "https://generativelanguage.googleapis.com/v1beta/openai",
        defaultModel = "gemini-2.0-flash",
        models = listOf("gemini-2.0-flash", "gemini-2.5-flash", "gemini-2.5-pro"),
        registerUrl = "https://aistudio.google.com/apikey",
        guideUrl = "https://ai.google.dev/gemini-api/docs/openai"
    ),

    /** DeepSeek */
    DEEPSEEK(
        id = "deepseek",
        displayNameResId = R.string.llm_vendor_deepseek,
        endpoint = "https://api.deepseek.com/v1",
        defaultModel = "deepseek-chat",
        models = listOf("deepseek-chat", "deepseek-reasoner"),
        registerUrl = "https://platform.deepseek.com/",
        guideUrl = "https://api-docs.deepseek.com/"
    ),

    /** Moonshot (Kimi) */
    MOONSHOT(
        id = "moonshot",
        displayNameResId = R.string.llm_vendor_moonshot,
        endpoint = "https://api.moonshot.cn/v1",
        defaultModel = "moonshot-v1-8k",
        models = listOf("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"),
        registerUrl = "https://platform.moonshot.cn/",
        guideUrl = "https://platform.moonshot.cn/docs/"
    ),

    /** Custom - user-defined OpenAI-compatible API */
    CUSTOM(
        id = "custom",
        displayNameResId = R.string.llm_vendor_custom,
        endpoint = "",
        defaultModel = "",
        models = emptyList(),
        registerUrl = "",
        guideUrl = ""
    );

    /** Whether this vendor requires an API key */
    val requiresApiKey: Boolean
        get() = this != SF_FREE

    /** Whether this vendor uses built-in endpoint (not user-configurable) */
    val hasBuiltinEndpoint: Boolean
        get() = this != CUSTOM && endpoint.isNotBlank()

    companion object {
        /** Get vendor by ID, defaulting to SF_FREE if not found */
        fun fromId(id: String?): LlmVendor = when (id?.lowercase()) {
            OPENAI.id -> OPENAI
            GEMINI.id -> GEMINI
            DEEPSEEK.id -> DEEPSEEK
            MOONSHOT.id -> MOONSHOT
            CUSTOM.id -> CUSTOM
            else -> SF_FREE
        }

        /** Get all vendors for UI selection */
        fun allVendors(): List<LlmVendor> = entries.toList()

        /** Get built-in vendors (excluding custom) */
        fun builtinVendors(): List<LlmVendor> = entries.filter { it != CUSTOM }
    }
}
