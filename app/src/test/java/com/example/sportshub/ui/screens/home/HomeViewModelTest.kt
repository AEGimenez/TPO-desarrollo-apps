package com.example.sportshub.ui.screens.home

import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.repository.MatchRepository
import com.example.sportshub.data.repository.FavoritesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: MatchRepository = mockk(relaxed = true)
    private val favoritesRepository: FavoritesRepository = mockk(relaxed = true)
    private val dao: com.example.sportshub.data.local.SportsDao = mockk(relaxed = true)
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default matches returning empty list
        every { repository.getMatchesByLeague(any()) } returns flowOf(emptyList())
        every { repository.searchMatchesInLeague(any(), any()) } returns flowOf(emptyList())
        every { dao.getAllFavorites() } returns flowOf(emptyList())
        every { dao.getStandingsByLeague(any()) } returns flowOf(emptyList())
        
        viewModel = HomeViewModel(repository, favoritesRepository, dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search query update changes search query state flow`() {
        // When
        viewModel.onSearchQueryChange("Boca")

        // Then
        assertEquals("Boca", viewModel.searchQuery.value)
    }

    @Test
    fun `filter selected changes selected filter flow`() {
        // When
        viewModel.onFilterSelected(MatchFilterType.PLAYED)

        // Then
        assertEquals(MatchFilterType.PLAYED, viewModel.selectedFilter.value)
    }

    @Test
    fun `league selected refreshes matches on repository`() {
        // Given
        val newLeague = League("4328", "Premier League")

        // When
        viewModel.onLeagueSelected(newLeague)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(newLeague, viewModel.selectedLeague.value)
        coVerify { repository.refreshMatches("4328") }
    }
}
