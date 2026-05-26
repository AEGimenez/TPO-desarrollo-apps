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
}