package com.example.sportshub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.repository.MatchRepository
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
    ALL, UPCOMING, PLAYED
}

class HomeViewModel(private val repository: MatchRepository) : ViewModel() {
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
        
        // Filtrado y ordenado
        baseFlow.map { list ->
            val filtered = when (filter) {
                MatchFilterType.ALL -> list
                MatchFilterType.UPCOMING -> list.filter { it.status.uppercase() == "NS" || it.status.uppercase() == "POST" }
                MatchFilterType.PLAYED -> list.filter { it.status.uppercase() == "FT" }
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

    fun onLeagueSelected(league: League) {
        _selectedLeague.value = league
        viewModelScope.launch {
            repository.refreshMatches(league.id)
        }
    }

    init {
        // Al arrancar refrescamos los partidos
        viewModelScope.launch {
            repository.refreshMatches(_selectedLeague.value.id)
        }
    }
}