package com.brycewg.asrkb.translate

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Kiss 翻译引擎实现
 */
class KissTranslateEngine(
    private val context: Context,
    private val endpoint: String,
    private val apiKey: String,
    private val httpClient: OkHttpClient? = null
) : TranslateEngine {

    companion object {
        private const val TAG = "KissTranslateEngine"
        private const val DEFAULT_ENDPOINT = "https://api.kiss.ai/translate"
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
            
            // 构建请求 JSON
            val jsonBody = JSONObject().apply {
                put("text", text)
                put("source", sourceLang)
                put("target", targetLang)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("X-API-Key", apiKey)
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
            val translatedText = json.optString("translation", "")
                .ifBlank { json.optString("translated_text", "") }
                .ifBlank { json.optString("result", "") }
            
            val detectedLang = json.optString("source_language", null)

            TranslateResult(
                translatedText = translatedText,
                sourceLang = detectedLang,
                targetLang = targetLang
            )
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
            json.optString("error", "")
                .ifBlank { json.optString("message", "") }
                .ifBlank { body.take(200) }
        } catch (e: Exception) {
            body.take(200)
        }
    }
}
