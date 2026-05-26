package com.example.sportshub.data.repository

import android.util.Log
import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.remote.TheSportsDbApi
import com.example.sportshub.data.remote.dto.MatchDto
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale

class MatchRepository(
    private val api: TheSportsDbApi,
    private val dao: SportsDao
) {
    // La UI va a leer permanentemente de acá (Directo de Room)
    val matches: Flow<List<MatchEntity>> = dao.getAllMatches()

    // esto busca en internet y actualiza Room
    suspend fun refreshMatches(leagueId: String) {
        try {
            // Buscamos los próximos partidos y los pasados al mismo tiempo
            val nextResponse = api.getNextMatches(leagueId)
            val pastResponse = api.getPastMatches(leagueId)

            val allDtos = mutableListOf<MatchDto>()
            nextResponse.events?.let { allDtos.addAll(it) }
            pastResponse.events?.let { allDtos.addAll(it) }

            // Convertimos esa lista de DTOs  a Entidades
            val entities = allDtos.map { it.toEntity() }

            // se guarda en la local DB
            dao.insertMatches(entities)

            Log.d("MatchRepository", "Partidos actualizados correctamente en Room")
        } catch (e: Exception) {
            Log.e("MatchRepository", "Error al traer partidos de internet: ${e.message}")
        }
    }


    private fun MatchDto.toEntity(): MatchEntity {
        // Transformamos la fecha que viene como texto  a un Timestamp
        val timestamp = try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateString = "${this.dateEvent} ${this.strTime}"
            format.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }

        return MatchEntity(
            id = idEvent.toIntOrNull() ?: 0,
            homeTeamId = idHomeTeam.toIntOrNull() ?: 0,
            awayTeamId = idAwayTeam.toIntOrNull() ?: 0,
            homeTeamName = strHomeTeam,
            awayTeamName = strAwayTeam,
            homeTeamLogo = strHomeTeamBadge ?: "",
            awayTeamLogo = strAwayTeamBadge ?: "",
            homeGoals = intHomeScore?.toIntOrNull(),
            awayGoals = intAwayScore?.toIntOrNull(),
            status = strStatus ?: "NS",
            leagueId = idLeague.toIntOrNull() ?: 0,
            leagueName = strLeague,
            leagueLogo = "",
            date = timestamp,
            venue = strVenue,
            round = intRound,
            updatedAt = System.currentTimeMillis()
        )
    }
}