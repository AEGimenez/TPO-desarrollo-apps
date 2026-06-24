package com.example.sportshub.data.remote

import com.example.sportshub.data.remote.dto.MatchesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TheSportsDbApi {

    // Obtiene los próximos 15 eventos de una liga específica
    @GET("eventsnextleague.php")
    suspend fun getNextMatches(@Query("id") leagueId: String): MatchesResponse

    // Obtiene los últimos 15 eventos finalizados de una liga específica
    @GET("eventspastleague.php")
    suspend fun getPastMatches(@Query("id") leagueId: String): MatchesResponse

    // Obtiene todos los partidos de una temporada específica (Premium)
    @GET("eventsseason.php")
    suspend fun getMatchesBySeason(
        @Query("id") leagueId: String,
        @Query("s") season: String
    ): MatchesResponse

    // Obtiene las ligas por deporte y país
    @GET("search_all_leagues.php")
    suspend fun searchLeagues(
        @Query("s") sport: String,
        @Query("c") country: String? = null
    ): LeagueResponse

    // Obtiene todos los equipos de una liga específica
    @GET("lookup_all_teams.php")
    suspend fun getTeamsByLeague(@Query("id") leagueId: String): TeamResponse

    // Obtiene próximos partidos de un equipo
    @GET("eventsnext.php")
    suspend fun getNextMatchesByTeam(@Query("id") teamId: String): MatchesResponse

    // Obtiene la tabla de posiciones de una liga para una temporada
    @GET("lookuptable.php")
    suspend fun getStandingsTable(
        @Query("l") leagueId: String,
        @Query("s") season: String
    ): StandingTableResponse

    // Obtiene detalle de un partido específico
    @GET("lookupevent.php")
    suspend fun getEventDetails(@Query("id") eventId: String): MatchesResponse

    // Busca equipos por nombre
    @GET("searchteams.php")
    suspend fun searchTeams(@Query("t") teamName: String): TeamResponse
}

// DTOs adicionales para TheSportsDB
data class LeagueResponse(val countrys: List<LeagueDto>?)
data class LeagueDto(
    val idLeague: String,
    val strLeague: String,
    val strSport: String,
    val strLeagueBadge: String?
)

data class TeamResponse(val teams: List<TeamDto>?)
data class TeamDto(
    val idTeam: String,
    val strTeam: String,
    val strTeamShort: String?,
    val strBadge: String?,
    val strCountry: String?,
    val strLeague: String?,
    val idLeague: String?,
    val intFormedYear: String?,
    val strStadium: String?
)

data class StandingTableResponse(val table: List<StandingDto>?)
data class StandingDto(
    val idStanding: String?,
    val intRank: String?,
    val idTeam: String?,
    val strTeam: String?,
    val strBadge: String?,
    val idLeague: String?,
    val strSeason: String?,
    val intPlayed: String?,
    val intWin: String?,
    val intDraw: String?,
    val intLoss: String?,
    val intGoalsFor: String?,
    val intGoalsAgainst: String?,
    val intGoalDifference: String?,
    val intPoints: String?,
    val strForm: String?
)