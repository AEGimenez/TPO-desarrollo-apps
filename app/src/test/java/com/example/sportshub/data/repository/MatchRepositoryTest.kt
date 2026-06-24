package com.example.sportshub.data.repository

import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.remote.TheSportsDbApi
import com.example.sportshub.data.remote.dto.MatchesResponse
import com.example.sportshub.data.remote.dto.MatchDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchRepositoryTest {

    private val api: TheSportsDbApi = mockk(relaxed = true)
    private val dao: SportsDao = mockk(relaxed = true)
    private lateinit var repository: MatchRepository

    @Before
    fun setUp() {
        io.mockk.mockkStatic(android.util.Log::class)
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any(), any()) } returns 0
        repository = MatchRepository(api, dao)
    }

    @Test
    fun `refreshMatches fetches from api and saves to room database`() = runTest {
        // Given
        val leagueId = "4406"
        val season = "2025"
        val mockDto = MatchDto(
            idEvent = "123",
            idHomeTeam = "1",
            idAwayTeam = "2",
            strHomeTeam = "River Plate",
            strAwayTeam = "Boca Juniors",
            strHomeTeamBadge = null,
            strAwayTeamBadge = null,
            intHomeScore = "2",
            intAwayScore = "1",
            strStatus = "FT",
            idLeague = "4406",
            strLeague = "Liga Argentina",
            dateEvent = "2025-05-10",
            strTime = "18:00:00",
            strVenue = "Monumental",
            intRound = "1"
        )
        val mockResponse = MatchesResponse(events = listOf(mockDto))
        
        coEvery { api.getMatchesBySeason(leagueId, season) } returns mockResponse

        // When
        repository.refreshMatches(leagueId)

        // Then
        coVerify { dao.insertMatches(any()) }
    }
}
