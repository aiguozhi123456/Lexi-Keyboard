package com.brycewg.asrkb.translate

import android.content.Context
import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Google Translate 免费 API 引擎实现
 * 使用 Google Translate 网页版的内部 API
 */
class GoogleFreeTranslateEngine(
    private val context: Context,
    private val endpoint: String,
    private val httpClient: OkHttpClient? = null
) : TranslateEngine {

    companion object {
        private const val TAG = "GoogleFreeTranslateEngine"
        private const val DEFAULT_ENDPOINT = "https://translate.googleapis.com/translate_a/single"
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

            urlBuilder.addQueryParameter("client", "gtx")
            urlBuilder.addQueryParameter("sl", if (sourceLang == "auto") "auto" else sourceLang)
            urlBuilder.addQueryParameter("tl", targetLang)
            urlBuilder.addQueryParameter("dt", "t")
            urlBuilder.addQueryParameter("q", text)

            val request = Request.Builder()
                .url(urlBuilder.build())
                .addHeader("User-Agent", "Mozilla/5.0")
                .get()
                .build()

            val response = http.newCall(request).execute()

            response.use { resp ->
                val bodyStr = resp.body?.string().orEmpty()
                
                if (!resp.isSuccessful) {
                    return TranslateResult(
                        translatedText = "",
                        targetLang = targetLang,
                        error = "HTTP ${resp.code}: ${resp.message}"
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
            // Google Translate 免费 API 返回的是一个嵌套数组
            // 格式: [[[translated_text, original_text, null, null, 0]], null, source_lang]
            val jsonArray = JSONArray(body)
            
            if (jsonArray.length() > 0) {
                val translationsArray = jsonArray.getJSONArray(0)
                val translatedTextBuilder = StringBuilder()
                
                // 遍历所有翻译片段
                for (i in 0 until translationsArray.length()) {
                    val segment = translationsArray.getJSONArray(i)
                    if (segment.length() > 0) {
                        val translatedSegment = segment.optString(0, "")
                        if (translatedSegment.isNotBlank()) {
                            translatedTextBuilder.append(translatedSegment)
                        }
                    }
                }
                
                val translatedText = translatedTextBuilder.toString()
                
                // 尝试获取检测到的源语言（如果有）
                val detectedLang = if (jsonArray.length() > 2) {
                    jsonArray.optString(2, null)
                } else {
                    null
                }

                TranslateResult(
                    translatedText = translatedText,
                    sourceLang = detectedLang,
                    targetLang = targetLang
                )
            } else {
                TranslateResult(
                    translatedText = "",
                    targetLang = targetLang,
                    error = "Empty response from Google Translate"
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
}
