package com.example.sportshub.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val shortName: String?,
    val logo: String,
    val country: String,
    val founded: Int?,
    val leagueId: Int
)