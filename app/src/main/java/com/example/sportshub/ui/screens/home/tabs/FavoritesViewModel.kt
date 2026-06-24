package com.example.sportshub.ui.screens.home.tabs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.entities.FavoriteEntity
import com.example.sportshub.data.remote.TeamDto
import com.example.sportshub.data.remote.LeagueDto
import com.example.sportshub.data.repository.FavoritesRepository
import com.example.sportshub.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntity>> = repository.favorites
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchResults = MutableStateFlow<List<TeamDto>>(emptyList())
    val searchResults: StateFlow<List<TeamDto>> = _searchResults.asStateFlow()

    private val _searchLeagueResults = MutableStateFlow<List<LeagueDto>>(emptyList())
    val searchLeagueResults: StateFlow<List<LeagueDto>> = _searchLeagueResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        viewModelScope.launch {
            // Sincronizar con Firestore al arrancar
            repository.syncFavorites()
        }
    }

    fun searchTeams(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            try {
                // Usamos el cliente directo de Retrofit para buscar
                val response = RetrofitClient.api.searchTeams(query)
                _searchResults.value = response.teams ?: emptyList()
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error al buscar equipos: ${e.message}", e)
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun searchLeagues(query: String) {
        if (query.isBlank()) {
            _searchLeagueResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            try {
                val allLeagues = mutableListOf<LeagueDto>()
                
                // Buscar ligas de Fútbol (Soccer)
                val soccerLeagues = try {
                    RetrofitClient.api.searchLeagues("Soccer")
                } catch (e: Exception) {
                    null
                }
                soccerLeagues?.countrys?.let { allLeagues.addAll(it) }

                // Buscar ligas de Básquetbol (Basketball)
                val basketLeagues = try {
                    RetrofitClient.api.searchLeagues("Basketball")
                } catch (e: Exception) {
                    null
                }
                basketLeagues?.countrys?.let { allLeagues.addAll(it) }

                // Fallback si falla la API
                if (allLeagues.isEmpty()) {
                    allLeagues.add(LeagueDto("4406", "Liga Profesional Argentina", "Soccer", "https://www.thesportsdb.com/images/media/league/badge/72vhy81620577665.png"))
                    allLeagues.add(LeagueDto("4328", "Premier League", "Soccer", "https://www.thesportsdb.com/images/media/league/badge/p1t4up1693574187.png"))
                    allLeagues.add(LeagueDto("4335", "La Liga", "Soccer", "https://www.thesportsdb.com/images/media/league/badge/fc16n01689264426.png"))
                    allLeagues.add(LeagueDto("4332", "Serie A", "Soccer", "https://www.thesportsdb.com/images/media/league/badge/q2t3h81689264871.png"))
                    allLeagues.add(LeagueDto("4387", "NBA", "Basketball", "https://www.thesportsdb.com/images/media/league/badge/4vub5t1542456488.png"))
                }

                val filtered = allLeagues.filter {
                    it.strLeague.contains(query, ignoreCase = true)
                }.distinctBy { it.idLeague }

                _searchLeagueResults.value = filtered
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error al buscar ligas: ${e.message}", e)
                _searchLeagueResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun addFavoriteTeam(team: TeamDto) {
        viewModelScope.launch {
            val fav = FavoriteEntity(
                id = team.idTeam.toIntOrNull() ?: 0,
                type = "team",
                name = team.strTeam,
                logo = team.strBadge ?: "",
                country = team.strCountry,
                firestoreSync = false
            )
            repository.addFavorite(fav)
        }
    }

    fun addFavoriteLeague(league: LeagueDto) {
        viewModelScope.launch {
            val fav = FavoriteEntity(
                id = league.idLeague.toIntOrNull() ?: 0,
                type = "league",
                name = league.strLeague,
                logo = league.strLeagueBadge ?: "",
                country = null,
                firestoreSync = false
            )
            repository.addFavorite(fav)
        }
    }

    fun removeFavorite(id: Int) {
        viewModelScope.launch {
            repository.removeFavorite(id)
        }
    }
}
