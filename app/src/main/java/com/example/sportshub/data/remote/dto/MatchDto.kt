package com.example.sportshub.data.remote.dto

import com.google.gson.annotations.SerializedName

// Esta clase envuelve la respuesta de la API (que siempre viene dentro de un array "events")
data class MatchesResponse(
    @SerializedName("events") val events: List<MatchDto>?
)

// Esta clase mapea exactamente los campos que escupe la API de TheSportsDB
data class MatchDto(
    @SerializedName("idEvent") val idEvent: String,
    @SerializedName("idHomeTeam") val idHomeTeam: String,
    @SerializedName("idAwayTeam") val idAwayTeam: String,
    @SerializedName("strHomeTeam") val strHomeTeam: String,
    @SerializedName("strAwayTeam") val strAwayTeam: String,
    @SerializedName("strHomeTeamBadge") val strHomeTeamBadge: String?,
    @SerializedName("strAwayTeamBadge") val strAwayTeamBadge: String?,
    @SerializedName("intHomeScore") val intHomeScore: String?,
    @SerializedName("intAwayScore") val intAwayScore: String?,
    @SerializedName("strStatus") val strStatus: String?,
    @SerializedName("idLeague") val idLeague: String,
    @SerializedName("strLeague") val strLeague: String,
    @SerializedName("dateEvent") val dateEvent: String?,
    @SerializedName("strTime") val strTime: String?,
    @SerializedName("strVenue") val strVenue: String?,
    @SerializedName("intRound") val intRound: String?
)