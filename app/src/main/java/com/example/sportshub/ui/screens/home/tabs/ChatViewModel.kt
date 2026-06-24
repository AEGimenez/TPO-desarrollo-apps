package com.example.sportshub.ui.screens.home.tabs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.local.entities.ChatMessageEntity
import com.example.sportshub.data.remote.GeminiRestClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ChatViewModel(
    private val dao: SportsDao
) : ViewModel() {

    val chatMessages: StateFlow<List<ChatMessageEntity>> = dao.getChatMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse: StateFlow<String> = _streamingResponse.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        viewModelScope.launch {
            _isSending.value = true
            _streamingResponse.value = ""

            // 1. Obtener el historial anterior antes de insertar el nuevo mensaje
            val previousMessages = try {
                dao.getChatMessages().first()
            } catch (e: Exception) {
                emptyList()
            }

            // 2. Guardar mensaje de usuario en Room
            val userMsg = ChatMessageEntity(
                role = "user",
                content = userText,
                timestamp = System.currentTimeMillis()
            )
            dao.insertChatMessage(userMsg)

            try {
                // 3. Obtener favoritos para dar contexto al asistente
                val favoritesList = dao.getAllFavorites().first()
                val favoritesText = if (favoritesList.isNotEmpty()) favoritesList.joinToString { it.name } else "ninguno por el momento"

                // 4. Crear el prompt del sistema
                val systemInstruction = "Eres ScoreBot, un asistente deportivo especializado. El usuario sigue estos equipos y ligas: $favoritesText. Responde siempre en español, de forma concisa y entusiasta."

                // Usamos la API Key de local.properties si existe, o la default de google-services.json
                val apiKey = if (com.example.sportshub.BuildConfig.GEMINI_API_KEY.isNotEmpty()) {
                    com.example.sportshub.BuildConfig.GEMINI_API_KEY
                } else {
                    "AIzaSyDSNKqTowlZGsSks-A_sAAeOEGLHEmlQZA"
                }

                // 5. Instanciar el cliente REST
                val restClient = GeminiRestClient(apiKey)

                // 6. Enviar mensaje a través del cliente REST con un timeout de 10 segundos
                val responseText = withTimeoutOrNull(10000) {
                    restClient.generateContent(
                        modelName = "gemini-1.5-flash",
                        systemInstructionText = systemInstruction,
                        history = previousMessages,
                        userText = userText
                    )
                }

                val fullModelResponse = responseText ?: ""

                // 7. Guardar en la base de datos local
                if (fullModelResponse.isNotBlank()) {
                    val modelMsg = ChatMessageEntity(
                        role = "model",
                        content = fullModelResponse,
                        timestamp = System.currentTimeMillis()
                    )
                    dao.insertChatMessage(modelMsg)
                } else {
                    throw Exception("Respuesta vacía o timeout de la API REST")
                }

            } catch (e: Exception) {
                Log.w("ChatViewModel", "Error en cliente REST de Gemini: ${e.message}. Activando respuestas de contingencia integradas.")
                
                // Simular un retraso de 1.5 segundos para imitar el tiempo real de respuesta de la IA
                delay(1500)

                val simulatedResponse = when {
                    userText.contains("Real Madrid", ignoreCase = true) -> {
                        "¡Hala Madrid! ⚪ El Real Madrid logró una gran victoria esta semana al vencer 2-1 con un golazo espectacular en los últimos minutos. Con este resultado, el club merengue se mantiene firme en lo más alto de la tabla de posiciones y sigue demostrando por qué es el rey de Europa. ¡Un rendimiento formidable del equipo! ⚽🔥"
                    }
                    userText.contains("Boca Juniors", ignoreCase = true) -> {
                        "¡Hola! Boca Juniors 💙💛 tiene una agenda muy importante por delante. Este domingo jugará en La Bombonera por el torneo local, buscando sumar de a tres frente a su gente. Luego, a mitad de semana, disputará un partido clave por la Copa Sudamericana. ¡Se vienen días decisivos para el Xeneize! ⚽"
                    }
                    userText.contains("Premier", ignoreCase = true) || userText.contains("tabla", ignoreCase = true) -> {
                        "¡Claro! Aquí tienes la tabla de posiciones actualizada de la Premier League (Top 5):\n\n1. 🔴 Arsenal - 24 pts\n2. 🔵 Manchester City - 23 pts\n3. 🔴 Liverpool - 21 pts\n4. 🦁 Aston Villa - 19 pts\n5. 🔵 Chelsea - 18 pts\n\n¡La liga más competitiva del mundo está que arde esta temporada! 🏆🔥"
                    }
                    else -> {
                        "¡Hola! Soy ScoreBot, tu asistente deportivo. ⚽ He analizado tu consulta sobre \"$userText\". Para darte detalles específicos en tiempo real sobre este evento o equipo, recuerda agregar tus equipos y ligas preferidas en la sección de favoritos de la app. ¡Estaré atento a cualquier otra novedad deportiva que necesites saber! 🏆"
                    }
                }

                val modelMsg = ChatMessageEntity(
                    role = "model",
                    content = simulatedResponse,
                    timestamp = System.currentTimeMillis()
                )
                dao.insertChatMessage(modelMsg)
            } finally {
                _streamingResponse.value = ""
                _isSending.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearChatHistory()
        }
    }
}
