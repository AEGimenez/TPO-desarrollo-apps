package com.example.sportshub.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.local.entities.NewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SportsDao {

    @Query("SELECT * FROM matches ORDER BY date ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("SELECT * FROM news ORDER BY publishedAt DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertNews(news: List<NewsEntity>)

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Int): MatchEntity?

    @Query("SELECT * FROM matches WHERE homeTeamName LIKE '%' || :query || '%' OR awayTeamName LIKE '%' || :query || '%'")
    fun searchMatches(query: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE leagueId = :leagueId ORDER BY date ASC")
    fun getMatchesByLeague(leagueId: Int): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE leagueId = :leagueId AND (homeTeamName LIKE '%' || :query || '%' OR awayTeamName LIKE '%' || :query || '%') ORDER BY date ASC")
    fun searchMatchesInLeague(leagueId: Int, query: String): Flow<List<MatchEntity>>
}
