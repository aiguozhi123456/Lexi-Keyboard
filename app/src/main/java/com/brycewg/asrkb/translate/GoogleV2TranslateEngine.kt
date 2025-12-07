package com.brycewg.asrkb.translate

import android.content.Context
import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Google Cloud Translation API v2 引擎实现
 * 官方文档: https://cloud.google.com/translate/docs/reference/rest/v2/translate
 */
class GoogleV2TranslateEngine(
    private val context: Context,
    private val endpoint: String,
    private val apiKey: String,
    private val httpClient: OkHttpClient? = null
) : TranslateEngine {

    companion object {
        private const val TAG = "GoogleV2TranslateEngine"
        private const val DEFAULT_ENDPOINT = "https://translation.googleapis.com/language/translate/v2"
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
            val baseUrl = endpoint.ifBlank { DEFAULT_ENDPOINT }
            
            // 构建 URL 参数
            val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return TranslateResult(
                    translatedText = "",
                    targetLang = targetLang,
                    error = "Invalid endpoint URL"
                )

            urlBuilder.addQueryParameter("key", apiKey)
            urlBuilder.addQueryParameter("q", text)
            urlBuilder.addQueryParameter("target", targetLang)
            
            // 如果指定了源语言且不是 "auto"
            if (sourceLang != "auto" && sourceLang.isNotBlank()) {
                urlBuilder.addQueryParameter("source", sourceLang)
            }
            
            urlBuilder.addQueryParameter("format", "text")

            val request = Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build()

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
            val data = json.optJSONObject("data")
            val translations = data?.optJSONArray("translations")
            
            if (translations != null && translations.length() > 0) {
                val firstTranslation = translations.getJSONObject(0)
                val translatedText = firstTranslation.optString("translatedText", "")
                val detectedLang = firstTranslation.optString("detectedSourceLanguage", null)

                TranslateResult(
                    translatedText = translatedText,
                    sourceLang = detectedLang,
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
            val error = json.optJSONObject("error")
            error?.optString("message", "")
                ?.ifBlank { error.optString("error", "") }
                ?.ifBlank { body.take(200) }
                ?: body.take(200)
        } catch (e: Exception) {
            body.take(200)
        }
    }
}
