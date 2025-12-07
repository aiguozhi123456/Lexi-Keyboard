package com.brycewg.asrkb.translate

import android.content.Context
import android.util.Log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * DeepL 翻译引擎实现
 * 官方文档: https://www.deepl.com/docs-api
 */
class DeepLTranslateEngine(
    private val context: Context,
    private val endpoint: String,
    private val apiKey: String,
    private val httpClient: OkHttpClient? = null
) : TranslateEngine {

    companion object {
        private const val TAG = "DeepLTranslateEngine"
        private const val DEFAULT_ENDPOINT = "https://api-free.deepl.com/v2/translate"
        private const val PRO_ENDPOINT = "https://api.deepl.com/v2/translate"
    }

    private val http: OkHttpClient = httpClient ?: OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslateResult {
        return try {
            val url = endpoint.ifBlank { DEFAULT_ENDPOINT }
            
            // DeepL 使用 form-urlencoded 格式
            val formBody = FormBody.Builder()
                .add("text", text)
                .add("target_lang", targetLang.uppercase())
                .apply {
                    // 如果 sourceLang 不是 "auto"，则添加 source_lang
                    if (sourceLang != "auto" && sourceLang.isNotBlank()) {
                        add("source_lang", sourceLang.uppercase())
                    }
                }
                .build()

            val requestBuilder = Request.Builder()
                .url(url)
                .post(formBody)

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "DeepL-Auth-Key $apiKey")
            }

            val request = requestBuilder.build()
            val response = http.newCall(request).execute()

            response.use { resp ->
                val bodyStr = resp.body?.string().orEmpty()
                
                if (!resp.isSuccessful) {
                    val errorMsg = extractErrorMessage(bodyStr)
                    return TranslateResult(
                        translatedText = "",
                        targetLang = targetLang,
                        error = "HTTP ${resp.code}: $errorMsg"
                    )
                }

                parseTranslateResponse(bodyStr, targetLang)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed", e)
            TranslateResult(
                translatedText = "",
                targetLang = targetLang,
                error = e.message ?: "Unknown error"
            )
        }
    }

    private fun parseTranslateResponse(body: String, targetLang: String): TranslateResult {
        return try {
            val json = JSONObject(body)
            val translations = json.optJSONArray("translations")
            
            if (translations != null && translations.length() > 0) {
                val firstTranslation = translations.getJSONObject(0)
                val translatedText = firstTranslation.optString("text", "")
                val detectedLang = firstTranslation.optString("detected_source_language", null)

                TranslateResult(
                    translatedText = translatedText,
                    sourceLang = detectedLang?.lowercase(),
                    targetLang = targetLang
                )
            } else {
                TranslateResult(
                    translatedText = "",
                    targetLang = targetLang,
                    error = "No translations found in response"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response", e)
            TranslateResult(
                translatedText = "",
                targetLang = targetLang,
                error = "Failed to parse response: ${e.message}"
            )
        }
    }

    private fun extractErrorMessage(body: String): String {
        return try {
            val json = JSONObject(body)
            json.optString("message", "")
                .ifBlank { json.optString("error", "") }
                .ifBlank { body.take(200) }
        } catch (e: Exception) {
            body.take(200)
        }
    }
}
