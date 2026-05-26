package com.example.sportshub.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // La URL base obligatoria según el TP
    private const val BASE_URL = "https://www.thesportsdb.com/api/v1/json/123/"

    // El interceptor nos va a permitir ver en la consola (Logcat) las llamadas y respuestas de internet (Súper útil si algo falla)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Instancia única de Retrofit para toda la app
    val api: TheSportsDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // Convierte el JSON a nuestras clases DTO
            .build()
            .create(TheSportsDbApi::class.java)
    }
}