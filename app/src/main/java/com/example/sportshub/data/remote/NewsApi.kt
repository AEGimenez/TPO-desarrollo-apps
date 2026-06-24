package com.example.sportshub.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsApi {

    @GET("everything")
    suspend fun getSportsNews(
        @Query("q") query: String,
        @Query("language") language: String = "es",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Header("X-Api-Key") apiKey: String = "f85f777a02af41c094b65f9352fdadd3"
    ): NewsResponse
}

data class NewsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("totalResults") val totalResults: Int,
    @SerializedName("articles") val articles: List<NewsDto>?
)

data class NewsDto(
    @SerializedName("source") val source: NewsSourceDto?,
    @SerializedName("author") val author: String?,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String,
    @SerializedName("urlToImage") val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: String
)

data class NewsSourceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String
)
