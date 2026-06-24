package com.example.sportshub.data.repository

import android.util.Log
import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.local.entities.NewsEntity
import com.example.sportshub.data.remote.NewsApi
import com.example.sportshub.data.remote.NewsDto
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale

class NewsRepository(
    private val newsApi: NewsApi,
    private val dao: SportsDao
) {
    val allNews: Flow<List<NewsEntity>> = dao.getAllNews()

    suspend fun refreshNews(query: String) {
        try {
            Log.d("NewsRepository", "Refrescando noticias con query: $query")
            val response = newsApi.getSportsNews(query = query)
            val dtos = response.articles ?: emptyList()
            
            val entities = dtos.map { dto ->
                dto.toEntity(category = query)
            }
            
            dao.insertNews(entities)
            Log.d("NewsRepository", "Se guardaron ${entities.size} noticias en Room")
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error al refrescar noticias: ${e.message}", e)
        }
    }

    private fun NewsDto.toEntity(category: String): NewsEntity {
        val hashId = url.md5()
        val timestamp = try {
            // NewsAPI date format: "2025-04-20T21:30:00Z"
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            format.parse(this.publishedAt)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        return NewsEntity(
            id = hashId,
            title = this.title,
            description = this.description,
            url = this.url,
            imageUrl = this.urlToImage,
            sourceName = this.source?.name ?: "Deportes",
            author = this.author,
            publishedAt = timestamp,
            category = category,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun String.md5(): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digested = md.digest(this.toByteArray())
            digested.joinToString("") {
                String.format("%02x", it)
            }
        } catch (e: Exception) {
            this.hashCode().toString()
        }
    }
}
