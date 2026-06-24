package com.example.sportshub.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.local.entities.NewsEntity
import com.example.sportshub.data.local.entities.ChatMessageEntity
import com.example.sportshub.data.local.entities.FavoriteEntity
import com.example.sportshub.data.local.entities.StandingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SportsDao {

    @Query("SELECT * FROM matches ORDER BY date ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("SELECT * FROM news ORDER BY publishedAt DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE id = :newsId")
    suspend fun getNewsById(newsId: String): NewsEntity?

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

    // Chatbot queries
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // Favorites queries
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: Int)

    // Standings queries
    @Query("DELETE FROM standings WHERE leagueId = :leagueId")
    suspend fun deleteStandingsByLeague(leagueId: Int)

    @Query("SELECT * FROM standings WHERE leagueId = :leagueId ORDER BY rank ASC")
    fun getStandingsByLeague(leagueId: Int): Flow<List<StandingEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertStandings(standings: List<StandingEntity>)
}
