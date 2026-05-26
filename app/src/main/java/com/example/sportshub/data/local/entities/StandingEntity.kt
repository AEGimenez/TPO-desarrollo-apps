package com.example.sportshub.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "standings")
data class StandingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leagueId: Int,
    val season: Int,
    val rank: Int,
    val teamId: Int,
    val teamName: String,
    val teamLogo: String,
    val points: Int,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int,
    val updatedAt: Long
)