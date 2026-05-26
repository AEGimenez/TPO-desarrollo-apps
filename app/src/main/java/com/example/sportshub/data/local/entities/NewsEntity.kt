package com.example.sportshub.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val sourceName: String,
    val author: String?,
    val publishedAt: Long,
    val category: String,
    val updatedAt: Long
)

