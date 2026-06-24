package com.example.sportshub.data.remote

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GeminiRestClient(private val apiKey: String) {
    private val client = OkHttpClient()
    private val gson = Gson()

    // Request data classes
    data class GeminiRequest(
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig? = null
    )

    data class GeminiContent(
        val role: String,
        val parts: List<GeminiPart>
    )

    data class GeminiPart(
        val text: String
    )

    data class GeminiGenerationConfig(
        val temperature: Float? = null
    )

    // Response data classes
    data class GeminiResponse(
        val candidates: List<GeminiCandidate>? = null,
        val error: GeminiErrorDetail? = null
    )

    data class GeminiCandidate(
        val content: GeminiContent? = null
    )

    data class GeminiErrorDetail(
        val code: Int,
        val message: String,
        val status: String
    )

    suspend fun generateContent(
        modelName: String = "gemini-1.5-flash",
        systemInstructionText: String? = null,
        history: List<com.example.sportshub.data.local.entities.ChatMessageEntity>,
        userText: String
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1/models/$modelName:generateContent?key=$apiKey"
        
        // 1. Build contents list including history
        val contentsList = mutableListOf<GeminiContent>()
        
        // Add previous messages (filtering out error messages)
        var lastRole: String? = null
        for (msg in history) {
            if (msg.content.startsWith("Error al conectar con ScoreBot:") || msg.content.contains("contingencia local")) continue
            if (msg.role != lastRole) {
                var contentText = msg.content
                // Si es el primer mensaje del historial, le concatenamos la instrucción del sistema para v1
                if (contentsList.isEmpty() && systemInstructionText != null) {
                    contentText = "$systemInstructionText\n\n$contentText"
                }
                contentsList.add(GeminiContent(role = msg.role, parts = listOf(GeminiPart(text = contentText))))
                lastRole = msg.role
            }
        }
        
        // Add current user message
        val currentText = if (contentsList.isEmpty() && systemInstructionText != null) {
            "$systemInstructionText\n\n$userText"
        } else {
            userText
        }

        if (lastRole != "user") {
            contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = currentText))))
        } else {
            contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = currentText))))
        }

        val requestBodyData = GeminiRequest(
            contents = contentsList,
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        val jsonRequest = gson.toJson(requestBodyData)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonRequest.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return kotlinx.coroutines.Dispatchers.IO.let { dispatcher ->
            kotlinx.coroutines.withContext(dispatcher) {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        val errorResponse = try {
                            gson.fromJson(responseBody, GeminiResponse::class.java)
                        } catch (e: Exception) {
                            null
                        }
                        val errMsg = errorResponse?.error?.message ?: "HTTP ${response.code}"
                        throw IOException(errMsg)
                    }

                    val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
                    val textResult = geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    
                    if (textResult.isNullOrBlank()) {
                        throw IOException("Respuesta de modelo vacía")
                    }
                    textResult
                }
            }
        }
    }
}
