package com.example.sportshub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.local.entities.FavoriteEntity
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    private val dao: SportsDao,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    private val _match = MutableStateFlow<MatchEntity?>(null)
    val match: StateFlow<MatchEntity?> = _match.asStateFlow()

    val favorites: StateFlow<List<FavoriteEntity>> = favoritesRepository.favorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadMatch(id: Int) {
        viewModelScope.launch {
            _match.value = dao.getMatchById(id)
        }
    }

    fun toggleFavoriteTeam(teamId: Int, teamName: String, teamLogo: String, country: String?) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.id == teamId }
            if (isFav) {
                favoritesRepository.removeFavorite(teamId)
            } else {
                val fav = FavoriteEntity(
                    id = teamId,
                    type = "team",
                    name = teamName,
                    logo = teamLogo,
                    country = country ?: "Desconocido",
                    firestoreSync = false
                )
                favoritesRepository.addFavorite(fav)
            }
        }
    }

    fun toggleFavoriteLeague(leagueId: Int, leagueName: String, leagueLogo: String) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.id == leagueId }
            if (isFav) {
                favoritesRepository.removeFavorite(leagueId)
            } else {
                val fav = FavoriteEntity(
                    id = leagueId,
                    type = "league",
                    name = leagueName,
                    logo = leagueLogo,
                    country = null,
                    firestoreSync = false
                )
                favoritesRepository.addFavorite(fav)
            }
        }
    }
}