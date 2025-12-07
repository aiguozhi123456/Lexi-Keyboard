package com.brycewg.asrkb.translate

import android.content.Context
import com.brycewg.asrkb.store.Prefs
import okhttp3.OkHttpClient

/**
 * 翻译管理器，负责创建和管理翻译引擎实例
 */
class TranslateManager(
    private val context: Context,
    private val prefs: Prefs
) {
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    /**
     * 根据配置创建翻译引擎
     */
    fun createTranslateEngine(vendor: TranslateVendor): TranslateEngine {
        return when (vendor) {
            TranslateVendor.IMME -> ImmeTranslateEngine(
                context = context,
                endpoint = prefs.translateImmeEndpoint,
                apiKey = prefs.translateImmeApiKey,
                httpClient = httpClient
            )
            
            TranslateVendor.KISS -> KissTranslateEngine(
                context = context,
                endpoint = prefs.translateKissEndpoint,
                apiKey = prefs.translateKissApiKey,
                httpClient = httpClient
            )
            
            TranslateVendor.DEEPL -> DeepLTranslateEngine(
                context = context,
                endpoint = prefs.translateDeepLEndpoint,
                apiKey = prefs.translateDeepLApiKey,
                httpClient = httpClient
            )
            
            TranslateVendor.GOOGLE_V2 -> GoogleV2TranslateEngine(
                context = context,
                endpoint = prefs.translateGoogleV2Endpoint,
                apiKey = prefs.translateGoogleV2ApiKey,
                httpClient = httpClient
            )
            
            TranslateVendor.GOOGLE_FREE -> GoogleFreeTranslateEngine(
                context = context,
                endpoint = prefs.translateGoogleFreeEndpoint,
                httpClient = httpClient
            )
            
            TranslateVendor.HCFY -> HcfyTranslateEngine(
                context = context,
                endpoint = prefs.translateHcfyEndpoint,
                apiKey = prefs.translateHcfyApiKey,
                httpClient = httpClient
            )
        }
    }

    /**
     * 使用当前配置的翻译引擎执行翻译
     */
    suspend fun translate(
        text: String,
        sourceLang: String? = null,
        targetLang: String? = null
    ): TranslateResult {
        val vendor = prefs.translateVendor
        val engine = createTranslateEngine(vendor)
        
        val src = sourceLang ?: prefs.translateSourceLang
        val tgt = targetLang ?: prefs.translateTargetLang
        
        return engine.translate(text, src, tgt)
    }

    /**
     * 检查翻译功能是否已配置
     */
    fun isConfigured(): Boolean {
        val vendor = prefs.translateVendor
        return when (vendor) {
            TranslateVendor.IMME -> prefs.translateImmeApiKey.isNotBlank()
            TranslateVendor.KISS -> prefs.translateKissApiKey.isNotBlank()
            TranslateVendor.DEEPL -> prefs.translateDeepLApiKey.isNotBlank()
            TranslateVendor.GOOGLE_V2 -> prefs.translateGoogleV2ApiKey.isNotBlank()
            TranslateVendor.GOOGLE_FREE -> true // 免费 API 不需要 key
            TranslateVendor.HCFY -> prefs.translateHcfyApiKey.isNotBlank()
        }
    }
}
