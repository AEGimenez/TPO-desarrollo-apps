package com.example.sportshub.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Int,
    val type: String, // "team" o "league"
    val name: String,
    val logo: String,
    val country: String?,
    val firestoreSync: Boolean
)