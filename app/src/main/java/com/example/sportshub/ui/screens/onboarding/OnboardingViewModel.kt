package com.example.sportshub.ui.screens.onboarding

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.local.entities.FavoriteEntity
import com.example.sportshub.data.remote.TheSportsDbApi
import com.example.sportshub.data.remote.LeagueDto
import com.example.sportshub.data.remote.TeamDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class OnboardingState {
    object Idle : OnboardingState()
    object Loading : OnboardingState()
    object Success : OnboardingState()
    data class Error(val message: String) : OnboardingState()
}

class OnboardingViewModel(
    private val api: TheSportsDbApi,
    private val dao: SportsDao
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    // Paso 1: Deportes seleccionados
    private val _selectedSports = MutableStateFlow<Set<String>>(emptySet())
    val selectedSports: StateFlow<Set<String>> = _selectedSports.asStateFlow()

    // Paso 2: Ligas cargadas y seleccionadas
    private val _leagues = MutableStateFlow<List<LeagueDto>>(emptyList())
    val leagues: StateFlow<List<LeagueDto>> = _leagues.asStateFlow()

    private val _selectedLeagues = MutableStateFlow<Set<LeagueDto>>(emptySet())
    val selectedLeagues: StateFlow<Set<LeagueDto>> = _selectedLeagues.asStateFlow()

    // Paso 3: Equipos cargados y seleccionados
    private val _teams = MutableStateFlow<List<TeamDto>>(emptyList())
    val teams: StateFlow<List<TeamDto>> = _teams.asStateFlow()

    private val _selectedTeams = MutableStateFlow<Set<TeamDto>>(emptySet())
    val selectedTeams: StateFlow<Set<TeamDto>> = _selectedTeams.asStateFlow()

    // Control de paso actual (1, 2, 3)
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    fun toggleSport(sport: String) {
        val current = _selectedSports.value
        if (current.contains(sport)) {
            _selectedSports.value = current - sport
        } else {
            _selectedSports.value = current + sport
        }
    }

    fun nextStep(context: Context) {
        viewModelScope.launch {
            when (_currentStep.value) {
                1 -> {
                    if (_selectedSports.value.isNotEmpty()) {
                        loadLeagues()
                        _currentStep.value = 2
                    }
                }
                2 -> {
                    if (_selectedLeagues.value.isNotEmpty()) {
                        loadTeams()
                        _currentStep.value = 3
                    }
                }
                3 -> {
                    saveOnboardingData(context)
                }
            }
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun toggleLeague(league: LeagueDto) {
        val current = _selectedLeagues.value
        if (current.contains(league)) {
            _selectedLeagues.value = current - league
        } else {
            _selectedLeagues.value = current + league
        }
    }

    fun toggleTeam(team: TeamDto) {
        val current = _selectedTeams.value
        if (current.contains(team)) {
            _selectedTeams.value = current - team
        } else {
            _selectedTeams.value = current + team
        }
    }

    private suspend fun loadLeagues() {
        _state.value = OnboardingState.Loading
        try {
            val fetchedLeagues = mutableListOf<LeagueDto>()
            val sports = _selectedSports.value
            
            // Hacemos la consulta a la API para cada deporte
            for (sport in sports) {
                val apiSportName = when (sport) {
                    "Fútbol" -> "Soccer"
                    "Básquetbol" -> "Basketball"
                    "Tenis" -> "Tennis"
                    else -> sport
                }
                try {
                    val response = api.searchLeagues(apiSportName, "Argentina")
                    response.countrys?.let { fetchedLeagues.addAll(it) }
                } catch (e: Exception) {
                    Log.w("OnboardingViewModel", "Error al buscar ligas de $sport: ${e.message}")
                }
            }

            // Fallback estático si la API no devuelve nada para asegurar offline/robustez
            if (fetchedLeagues.isEmpty()) {
                fetchedLeagues.addAll(getMockLeagues())
            }

            _leagues.value = fetchedLeagues.distinctBy { it.idLeague }
            _state.value = OnboardingState.Idle
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error al cargar ligas", e)
            _leagues.value = getMockLeagues()
            _state.value = OnboardingState.Idle
        }
    }

    private suspend fun loadTeams() {
        _state.value = OnboardingState.Loading
        try {
            val fetchedTeams = mutableListOf<TeamDto>()
            val leagues = _selectedLeagues.value

            for (league in leagues) {
                try {
                    val response = api.getTeamsByLeague(league.idLeague)
                    response.teams?.let { fetchedTeams.addAll(it) }
                } catch (e: Exception) {
                    Log.w("OnboardingViewModel", "Error al buscar equipos para liga ${league.strLeague}: ${e.message}")
                }
            }

            // Fallback estático de equipos si la API no devuelve nada
            if (fetchedTeams.isEmpty()) {
                fetchedTeams.addAll(getMockTeams())
            }

            _teams.value = fetchedTeams.distinctBy { it.idTeam }
            _state.value = OnboardingState.Idle
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error al cargar equipos", e)
            _teams.value = getMockTeams()
            _state.value = OnboardingState.Idle
        }
    }

    private suspend fun saveOnboardingData(context: Context) {
        _state.value = OnboardingState.Loading
        try {
            val uid = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")

            // Convertimos las selecciones a FavoriteEntity
            val favoriteEntities = mutableListOf<FavoriteEntity>()

            // Ligas favoritas
            _selectedLeagues.value.forEach { league ->
                favoriteEntities.add(
                    FavoriteEntity(
                        id = league.idLeague.toIntOrNull() ?: 0,
                        type = "league",
                        name = league.strLeague,
                        logo = league.strLeagueBadge ?: "",
                        country = "Argentina",
                        firestoreSync = false
                    )
                )
            }

            // Equipos favoritos
            _selectedTeams.value.forEach { team ->
                favoriteEntities.add(
                    FavoriteEntity(
                        id = team.idTeam.toIntOrNull() ?: 0,
                        type = "team",
                        name = team.strTeam,
                        logo = team.strBadge ?: "",
                        country = team.strCountry,
                        firestoreSync = false
                    )
                )
            }

            // 1. Guardar localmente en Room
            dao.insertFavorites(favoriteEntities)

            // 2. Guardar remotamente en Firestore de forma asíncrona (en segundo plano) para no bloquear al usuario
            viewModelScope.launch {
                try {
                    // Escribir favoritos en subcolección users/{uid}/favorites
                    for (fav in favoriteEntities) {
                        firestore.collection("users").document(uid)
                            .collection("favorites").document(fav.id.toString())
                            .set(fav.copy(firestoreSync = true))
                            .await()
                    }

                    // Marcar onboarding como completo en el documento del usuario
                    firestore.collection("users").document(uid)
                        .update("onboardingCompleted", true)
                        .await()
                } catch (fsEx: Exception) {
                    Log.w("OnboardingViewModel", "No se pudo sincronizar en Firestore (offline): ${fsEx.message}")
                }
            }

            // 3. Guardar en SharedPreferences locales
            val sharedPrefs = context.getSharedPreferences("sportshub_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("onboarding_completed_${uid}", true).apply()

            _state.value = OnboardingState.Success
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error al guardar el onboarding", e)
            _state.value = OnboardingState.Error(e.localizedMessage ?: "Error al guardar favoritos")
        }
    }

    private fun getMockLeagues(): List<LeagueDto> {
        val selected = _selectedSports.value
        val list = mutableListOf<LeagueDto>()
        if (selected.contains("Fútbol")) {
            list.add(LeagueDto("4406", "Liga Profesional Argentina", "Soccer", ""))
            list.add(LeagueDto("4328", "Premier League", "Soccer", ""))
            list.add(LeagueDto("4335", "La Liga", "Soccer", ""))
        }
        if (selected.contains("Básquetbol")) {
            list.add(LeagueDto("4387", "NBA", "Basketball", ""))
        }
        if (selected.contains("Tenis")) {
            list.add(LeagueDto("4480", "ATP Tour", "Tennis", ""))
        }
        return list
    }

    private fun getMockTeams(): List<TeamDto> {
        val selectedLeaguesIds = _selectedLeagues.value.map { it.idLeague }
        val list = mutableListOf<TeamDto>()

        if (selectedLeaguesIds.contains("4406")) {
            list.add(TeamDto("133739", "Boca Juniors", "BOC", "", "Argentina", "Liga Argentina", "4406", "1905", "La Bombonera"))
            list.add(TeamDto("133738", "River Plate", "RIV", "", "Argentina", "Liga Argentina", "4406", "1901", "El Monumental"))
            list.add(TeamDto("133740", "Racing Club", "RAC", "", "Argentina", "Liga Argentina", "4406", "1903", "El Cilindro"))
            list.add(TeamDto("133741", "San Lorenzo", "SLO", "", "Argentina", "Liga Argentina", "4406", "1908", "Nuevo Gasometro"))
        }
        if (selectedLeaguesIds.contains("4328")) {
            list.add(TeamDto("133612", "Manchester United", "MUN", "", "England", "Premier League", "4328", "1878", "Old Trafford"))
            list.add(TeamDto("133613", "Manchester City", "MCI", "", "England", "Premier League", "4328", "1880", "Etihad Stadium"))
            list.add(TeamDto("133602", "Liverpool", "LIV", "", "England", "Premier League", "4328", "1892", "Anfield"))
            list.add(TeamDto("133604", "Chelsea", "CHE", "", "England", "Premier League", "4328", "1905", "Stamford Bridge"))
        }
        if (selectedLeaguesIds.contains("4335")) {
            list.add(TeamDto("133759", "Real Madrid", "RMA", "", "Spain", "La Liga", "4335", "1902", "Santiago Bernabeu"))
            list.add(TeamDto("133760", "Barcelona", "FCB", "", "Spain", "La Liga", "4335", "1899", "Camp Nou"))
        }
        if (selectedLeaguesIds.contains("4387")) {
            list.add(TeamDto("134858", "Los Angeles Lakers", "LAL", "", "USA", "NBA", "4387", "1947", "Crypto.com Arena"))
            list.add(TeamDto("134860", "Golden State Warriors", "GSW", "", "USA", "NBA", "4387", "1946", "Chase Center"))
        }
        if (selectedLeaguesIds.contains("4480")) {
            list.add(TeamDto("136000", "Carlos Alcaraz", "ALC", "", "Spain", "ATP Tour", "4480", "2003", ""))
            list.add(TeamDto("136001", "Jannik Sinner", "SIN", "", "Italy", "ATP Tour", "4480", "2001", ""))
        }
        
        return list
    }
}
