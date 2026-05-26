package com.example.sportshub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.repository.MatchRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MatchRepository) : ViewModel() {

    val matches: StateFlow<List<MatchEntity>> = repository.matches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Al arrancar, forzamos una actualización de los datos desde la API
        viewModelScope.launch {
            repository.refreshMatches("4406") // ej: id liga argentina
        }
    }
}