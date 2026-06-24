package com.example.sportshub.ui.screens.home.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportshub.data.local.entities.NewsEntity
import com.example.sportshub.data.repository.NewsRepository
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

class NewsViewModel(private val repository: NewsRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory = _selectedCategory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val newsList = combine(_selectedCategory, _searchQuery) { category, query ->
        Pair(category, query)
    }.flatMapLatest { (category, query) ->
        repository.allNews.map { list ->
            var filtered = list
            // Filtrar por categoría
            if (category != "Todos") {
                val apiQueryName = when (category) {
                    "Fútbol" -> "futbol"
                    "Básquetbol" -> "basketball"
                    "Tenis" -> "tennis"
                    else -> category.lowercase()
                }
                filtered = filtered.filter { it.category.contains(apiQueryName, ignoreCase = true) }
            }
            // Filtrar por texto de búsqueda
            if (query.isNotEmpty()) {
                filtered = filtered.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            (it.description?.contains(query, ignoreCase = true) ?: false)
                }
            }
            // Ordenar por fecha más reciente
            filtered.sortedByDescending { it.publishedAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshAllNews()
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun refreshAllNews() {
        viewModelScope.launch {
            repository.refreshNews("futbol")
            repository.refreshNews("basketball")
            repository.refreshNews("tennis")
        }
    }
}
