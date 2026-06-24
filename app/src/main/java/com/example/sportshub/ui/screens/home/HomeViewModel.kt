package com.example.sportshub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.local.entities.StandingEntity
import com.example.sportshub.data.repository.MatchRepository
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

data class League(val id: String, val name: String)

enum class MatchFilterType {
    ALL, UPCOMING, PLAYED, FAVORITES
}

enum class PartidosSubTab {
    PARTIDOS, POSICIONES
}

class HomeViewModel(
    private val repository: MatchRepository,
    private val favoritesRepository: FavoritesRepository,
    private val dao: SportsDao
) : ViewModel() {
    val leagues = listOf(
        League("4406", "Liga Argentina"),
        League("4328", "Premier League"),
        League("4335", "La Liga"),
        League("4332", "Serie A")
    )

    private val _selectedLeague = MutableStateFlow(leagues[0])
    val selectedLeague = _selectedLeague.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MatchFilterType.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedSubTab = MutableStateFlow(PartidosSubTab.PARTIDOS)
    val selectedSubTab = _selectedSubTab.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val standingsTable = _selectedLeague.flatMapLatest { league ->
        dao.getStandingsByLeague(league.id.toIntOrNull() ?: 0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val matches = combine(_selectedLeague, _searchQuery, _selectedFilter) { league, query, filter ->
        Triple(league, query, filter)
    }.flatMapLatest { (league, query, filter) ->
        val leagueIdInt = league.id.toIntOrNull() ?: 0
        val baseFlow = if (query.isEmpty()) {
            repository.getMatchesByLeague(leagueIdInt)
        } else {
            repository.searchMatchesInLeague(leagueIdInt, query)
        }
        
        // Combinamos la lista base con los favoritos para aplicar el filtro de favoritos de forma reactiva
        combine(baseFlow, dao.getAllFavorites()) { list, favs ->
            val filtered = when (filter) {
                MatchFilterType.ALL -> list
                MatchFilterType.UPCOMING -> list.filter { it.status.uppercase() == "NS" || it.status.uppercase() == "POST" }
                MatchFilterType.PLAYED -> list.filter { it.status.uppercase() == "FT" }
                MatchFilterType.FAVORITES -> {
                    val favLeaguesIds = favs.filter { it.type == "league" }.map { it.id }
                    val favTeamsIds = favs.filter { it.type == "team" }.map { it.id }
                    list.filter { match ->
                        favLeaguesIds.contains(match.leagueId) ||
                                favTeamsIds.contains(match.homeTeamId) ||
                                favTeamsIds.contains(match.awayTeamId)
                    }
                }
            }

            if (filter == MatchFilterType.PLAYED) {
                filtered.sortedByDescending { it.date } // Más recientes
            } else {
                filtered.sortedBy { it.date } // Proximos
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: MatchFilterType) {
        _selectedFilter.value = filter
    }

    fun onSubTabSelected(tab: PartidosSubTab) {
        _selectedSubTab.value = tab
    }

    fun onLeagueSelected(league: League) {
        _selectedLeague.value = league
        viewModelScope.launch {
            repository.refreshMatches(league.id)
        }
        viewModelScope.launch {
            val season = when (league.id) {
                "4406" -> "2025"
                else -> "2025-2026"
            }
            favoritesRepository.refreshStandings(league.id, season)
        }
    }

    init {
        // Al arrancar refrescamos los partidos y posiciones
        viewModelScope.launch {
            repository.refreshMatches(_selectedLeague.value.id)
        }
        viewModelScope.launch {
            val defaultLeague = _selectedLeague.value
            val season = when (defaultLeague.id) {
                "4406" -> "2025"
                else -> "2025-2026"
            }
            favoritesRepository.refreshStandings(defaultLeague.id, season)
        }
    }
}