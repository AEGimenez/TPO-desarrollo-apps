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

    fun getAllMatches(): Flow<List<MatchEntity>> = dao.getAllMatches()

    fun searchMatches(query: String): Flow<List<MatchEntity>> = dao.searchMatches(query)

    fun getMatchesByLeague(leagueId: Int): Flow<List<MatchEntity>> = dao.getMatchesByLeague(leagueId)

    fun searchMatchesInLeague(leagueId: Int, query: String): Flow<List<MatchEntity>> = dao.searchMatchesInLeague(leagueId, query)


    suspend fun refreshMatches(leagueId: String) {
        try {
            val season = when (leagueId) {
                "4406" -> "2025"
                else -> "2025-2026"
            }

            Log.d("MatchRepository", "Intentando descargar temporada $season para liga $leagueId...")

            // Obtenemos los eventos de la temporada completa
            val response = api.getMatchesBySeason(leagueId, season)
            val allDtos = response.events?.toMutableList() ?: mutableListOf()

            // Si por algún motivo la temporada completa viene vacía, usamos de respaldo próximos y pasados
            if (allDtos.isEmpty()) {
                Log.d("MatchRepository", "Temporada completa vacía en API, usando fallback de próximos/pasados...")
                val nextResponse = api.getNextMatches(leagueId)
                val pastResponse = api.getPastMatches(leagueId)
                nextResponse.events?.let { allDtos.addAll(it) }
                pastResponse.events?.let { allDtos.addAll(it) }
            }

            // Convertimos esa lista de DTOs a Entidades
            val entities = allDtos.map { it.toEntity() }.toMutableList()

            // Agregamos partidos de prueba adicionales sólo como complemento para desarrollo local
            entities.addAll(getMockMatches(leagueId))

            // Guardamos de forma masiva en Room
            dao.insertMatches(entities)
            Log.d("MatchRepository", "Se guardaron ${entities.size} partidos reales y Premium en Room")
        } catch (e: Exception) {
            Log.e("MatchRepository", "Error al traer partidos premium: ${e.message}")
            // Fallback de contingencia si falla la llamada premium (por red o clave):
            try {
                val nextResponse = api.getNextMatches(leagueId)
                val pastResponse = api.getPastMatches(leagueId)
                val allDtos = mutableListOf<MatchDto>()
                nextResponse.events?.let { allDtos.addAll(it) }
                pastResponse.events?.let { allDtos.addAll(it) }
                val entities = allDtos.map { it.toEntity() }.toMutableList()
                entities.addAll(getMockMatches(leagueId))
                dao.insertMatches(entities)
            } catch (fallbackEx: Exception) {
                Log.e("MatchRepository", "Fallo también el fallback: ${fallbackEx.message}")
            }
        }
    }

    private fun getMockMatches(leagueId: String): List<MatchEntity> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        val leagueIdInt = leagueId.toIntOrNull() ?: 0

        return when (leagueId) {
            "4406" -> listOf( // Liga Argentina
                MatchEntity(
                    id = 1001, homeTeamId = 1, awayTeamId = 2,
                    homeTeamName = "River Plate", awayTeamName = "Boca Juniors",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 2, awayGoals = 1, status = "FT",
                    leagueId = leagueIdInt, leagueName = "Liga Argentina", leagueLogo = "",
                    date = now - oneDay * 2, venue = "El Monumental", round = "Fecha 15", updatedAt = now
                ),
                MatchEntity(
                    id = 1002, homeTeamId = 3, awayTeamId = 4,
                    homeTeamName = "Racing Club", awayTeamName = "Independiente",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 1, awayGoals = 0, status = "FT",
                    leagueId = leagueIdInt, leagueName = "Liga Argentina", leagueLogo = "",
                    date = now - oneDay, venue = "El Cilindro", round = "Fecha 15", updatedAt = now
                ),
                MatchEntity(
                    id = 1003, homeTeamId = 5, awayTeamId = 6,
                    homeTeamName = "San Lorenzo", awayTeamName = "Huracán",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = null, awayGoals = null, status = "NS",
                    leagueId = leagueIdInt, leagueName = "Liga Argentina", leagueLogo = "",
                    date = now + oneDay, venue = "Nuevo Gasómetro", round = "Fecha 16", updatedAt = now
                ),
                MatchEntity(
                    id = 1004, homeTeamId = 7, awayTeamId = 8,
                    homeTeamName = "Talleres", awayTeamName = "Belgrano",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 1, awayGoals = 1, status = "1H",
                    leagueId = leagueIdInt, leagueName = "Liga Argentina", leagueLogo = "",
                    date = now, venue = "Mario Alberto Kempes", round = "Fecha 16", updatedAt = now
                ),
                MatchEntity(
                    id = 1005, homeTeamId = 9, awayTeamId = 10,
                    homeTeamName = "Estudiantes LP", awayTeamName = "Gimnasia LP",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 3, awayGoals = 1, status = "FT",
                    leagueId = leagueIdInt, leagueName = "Liga Argentina", leagueLogo = "",
                    date = now - oneDay * 5, venue = "Uno", round = "Fecha 14", updatedAt = now
                )
            )
            "4328" -> listOf( // Premier League
                MatchEntity(
                    id = 2001, homeTeamId = 11, awayTeamId = 12,
                    homeTeamName = "Manchester City", awayTeamName = "Manchester United",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 3, awayGoals = 1, status = "FT",
                    leagueId = leagueIdInt, leagueName = "Premier League", leagueLogo = "",
                    date = now - oneDay * 3, venue = "Etihad Stadium", round = "Round 28", updatedAt = now
                ),
                MatchEntity(
                    id = 2002, homeTeamId = 13, awayTeamId = 14,
                    homeTeamName = "Arsenal", awayTeamName = "Chelsea",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 2, awayGoals = 2, status = "FT",
                    leagueId = leagueIdInt, leagueName = "Premier League", leagueLogo = "",
                    date = now - oneDay, venue = "Emirates Stadium", round = "Round 28", updatedAt = now
                ),
                MatchEntity(
                    id = 2003, homeTeamId = 15, awayTeamId = 16,
                    homeTeamName = "Liverpool", awayTeamName = "Everton",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = null, awayGoals = null, status = "NS",
                    leagueId = leagueIdInt, leagueName = "Premier League", leagueLogo = "",
                    date = now + oneDay * 2, venue = "Anfield", round = "Round 29", updatedAt = now
                ),
                MatchEntity(
                    id = 2004, homeTeamId = 17, awayTeamId = 18,
                    homeTeamName = "Tottenham", awayTeamName = "Aston Villa",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 2, awayGoals = 1, status = "2H",
                    leagueId = leagueIdInt, leagueName = "Premier League", leagueLogo = "",
                    date = now, venue = "Tottenham Hotspur Stadium", round = "Round 29", updatedAt = now
                )
            )
            "4335" -> listOf( // La Liga
                MatchEntity(
                    id = 3001, homeTeamId = 21, awayTeamId = 22,
                    homeTeamName = "Real Madrid", awayTeamName = "Barcelona",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 3, awayGoals = 2, status = "FT",
                    leagueId = leagueIdInt, leagueName = "La Liga", leagueLogo = "",
                    date = now - oneDay * 4, venue = "Santiago Bernabéu", round = "Jornada 32", updatedAt = now
                ),
                MatchEntity(
                    id = 3002, homeTeamId = 23, awayTeamId = 24,
                    homeTeamName = "Atlético Madrid", awayTeamName = "Sevilla",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 1, awayGoals = 0, status = "FT",
                    leagueId = leagueIdInt, leagueName = "La Liga", leagueLogo = "",
                    date = now - oneDay, venue = "Cívitas Metropolitano", round = "Jornada 32", updatedAt = now
                ),
                MatchEntity(
                    id = 3003, homeTeamId = 25, awayTeamId = 26,
                    homeTeamName = "Athletic Bilbao", awayTeamName = "Real Sociedad",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = null, awayGoals = null, status = "NS",
                    leagueId = leagueIdInt, leagueName = "La Liga", leagueLogo = "",
                    date = now + oneDay * 3, venue = "San Mamés", round = "Jornada 33", updatedAt = now
                )
            )
            "4332" -> listOf( // Serie A
                MatchEntity(
                    id = 4001, homeTeamId = 31, awayTeamId = 32,
                    homeTeamName = "Inter", awayTeamName = "Milan",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 2, awayGoals = 1, status = "FT",
                    leagueId = leagueIdInt, leagueName = "Serie A", leagueLogo = "",
                    date = now - oneDay * 2, venue = "San Siro", round = "Giornata 33", updatedAt = now
                ),
                MatchEntity(
                    id = 4002, homeTeamId = 33, awayTeamId = 34,
                    homeTeamName = "Juventus", awayTeamName = "Torino",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = 2, awayGoals = 0, status = "FT",
                    leagueId = leagueIdInt, leagueName = "Serie A", leagueLogo = "",
                    date = now - oneDay, venue = "Allianz Stadium", round = "Giornata 33", updatedAt = now
                ),
                MatchEntity(
                    id = 4003, homeTeamId = 35, awayTeamId = 36,
                    homeTeamName = "Roma", awayTeamName = "Lazio",
                    homeTeamLogo = "", awayTeamLogo = "",
                    homeGoals = null, awayGoals = null, status = "NS",
                    leagueId = leagueIdInt, leagueName = "Serie A", leagueLogo = "",
                    date = now + oneDay * 2, venue = "Stadio Olimpico", round = "Giornata 34", updatedAt = now
                )
            )
            else -> emptyList()
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