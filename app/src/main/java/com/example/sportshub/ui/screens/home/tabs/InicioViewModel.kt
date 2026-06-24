package com.example.sportshub.ui.screens.home.tabs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.entities.FavoriteEntity
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.local.entities.NewsEntity
import com.example.sportshub.data.local.entities.StandingEntity
import com.example.sportshub.data.repository.MatchRepository
import com.example.sportshub.data.repository.NewsRepository
import com.example.sportshub.data.repository.FavoritesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InicioViewModel(
    private val matchRepository: MatchRepository,
    private val newsRepository: NewsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val dao: com.example.sportshub.data.local.SportsDao
) : ViewModel() {

    val favoriteLeagues = favoritesRepository.favorites
        .map { list -> list.filter { it.type == "league" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val upcomingMatches: StateFlow<List<MatchEntity>> = favoritesRepository.favorites
        .flatMapLatest { favs ->
            val favLeaguesIds = favs.filter { it.type == "league" }.map { it.id }
            val favTeamsIds = favs.filter { it.type == "team" }.map { it.id }

            matchRepository.getAllMatches().map { list ->
                val upcoming = list.filter {
                    it.status.uppercase() in listOf("NS", "1H", "2H", "HT", "POST")
                }
                
                // Ordenar mostrando favoritos primero, luego por fecha
                val sorted = upcoming.sortedWith { m1, m2 ->
                    val m1Fav = favLeaguesIds.contains(m1.leagueId) || favTeamsIds.contains(m1.homeTeamId) || favTeamsIds.contains(m1.awayTeamId)
                    val m2Fav = favLeaguesIds.contains(m2.leagueId) || favTeamsIds.contains(m2.homeTeamId) || favTeamsIds.contains(m2.awayTeamId)
                    
                    if (m1Fav && !m2Fav) {
                        -1
                    } else if (!m1Fav && m2Fav) {
                        1
                    } else {
                        m1.date.compareTo(m2.date)
                    }
                }

                sorted.take(10)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNews: StateFlow<List<NewsEntity>> = newsRepository.allNews
        .map { list -> list.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStandingsLeague = MutableStateFlow<FavoriteEntity?>(null)
    val selectedStandingsLeague = _selectedStandingsLeague.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val standingsTable: StateFlow<List<StandingEntity>> = _selectedStandingsLeague
        .flatMapLatest { league ->
            if (league == null) {
                // Si no hay seleccionada, devolver vacío
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                dao.getStandingsByLeague(league.id)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Sincronizar favoritos y cargar sus datos
            favoritesRepository.syncFavorites()
            
            // Refrescar partidos y noticias en segundo plano
            newsRepository.refreshNews("futbol")

            // Seleccionar por defecto la primera liga favorita para mostrar su tabla
            favoritesRepository.favorites.collect { favs ->
                val leagues = favs.filter { it.type == "league" }
                if (leagues.isNotEmpty() && _selectedStandingsLeague.value == null) {
                    val mainLeague = leagues[0]
                    _selectedStandingsLeague.value = mainLeague
                    refreshStandingsForLeague(mainLeague.id.toString())
                }
            }
        }
    }

    fun selectStandingsLeague(league: FavoriteEntity) {
        _selectedStandingsLeague.value = league
        refreshStandingsForLeague(league.id.toString())
    }

    private fun refreshStandingsForLeague(leagueId: String) {
        viewModelScope.launch {
            val season = when (leagueId) {
                "4406" -> "2025"
                else -> "2025-2026"
            }
            favoritesRepository.refreshStandings(leagueId, season)
        }
    }
}
