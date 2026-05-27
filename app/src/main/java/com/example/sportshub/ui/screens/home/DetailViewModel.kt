package com.example.sportshub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.local.entities.MatchEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(private val dao: SportsDao) : ViewModel() {
    private val _match = MutableStateFlow<MatchEntity?>(null)
    val match: StateFlow<MatchEntity?> = _match.asStateFlow()

    fun loadMatch(id: Int) {
        viewModelScope.launch {
            _match.value = dao.getMatchById(id)
        }
    }
}