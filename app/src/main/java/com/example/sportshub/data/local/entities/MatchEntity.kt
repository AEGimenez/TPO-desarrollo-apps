package com.example.sportshub.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeTeamLogo: String,
    val awayTeamLogo: String,
    val homeGoals: Int?,
    val awayGoals: Int?,
    val status: String,
    val leagueId: Int,
    val leagueName: String,
    val leagueLogo: String,
    val date: Long,
    val venue: String?,
    val round: String?,
    val updatedAt: Long
)