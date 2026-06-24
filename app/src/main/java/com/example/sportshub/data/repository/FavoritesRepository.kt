package com.example.sportshub.data.repository

import android.util.Log
import com.example.sportshub.data.local.SportsDao
import com.example.sportshub.data.local.entities.FavoriteEntity
import com.example.sportshub.data.local.entities.StandingEntity
import com.example.sportshub.data.remote.TheSportsDbApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class FavoritesRepository(
    private val api: TheSportsDbApi,
    private val dao: SportsDao
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val favorites: Flow<List<FavoriteEntity>> = dao.getAllFavorites()

    suspend fun addFavorite(favorite: FavoriteEntity) {
        // 1. Guardar localmente
        dao.insertFavorite(favorite)

        // 2. Guardar en Firestore
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(uid)
                .collection("favorites").document(favorite.id.toString())
                .set(favorite.copy(firestoreSync = true))
                .await()
            // Marcar como sincronizado
            dao.insertFavorite(favorite.copy(firestoreSync = true))
        } catch (e: Exception) {
            Log.w("FavoritesRepository", "Error al sincronizar con Firestore: ${e.message}")
        }
    }

    suspend fun removeFavorite(id: Int) {
        // 1. Eliminar localmente
        dao.deleteFavoriteById(id)

        // 2. Eliminar en Firestore
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(uid)
                .collection("favorites").document(id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w("FavoritesRepository", "Error al eliminar en Firestore: ${e.message}")
        }
    }

    suspend fun syncFavorites() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("favorites").get().await()
            
            val firestoreFavs = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                val type = doc.getString("type") ?: "team"
                val name = doc.getString("name") ?: ""
                val logo = doc.getString("logo") ?: ""
                val country = doc.getString("country")
                FavoriteEntity(id, type, name, logo, country, firestoreSync = true)
            }

            if (firestoreFavs.isNotEmpty()) {
                dao.insertFavorites(firestoreFavs)
            }
        } catch (e: Exception) {
            Log.w("FavoritesRepository", "Error al traer favoritos de Firestore: ${e.message}")
        }
    }

    suspend fun refreshStandings(leagueId: String, season: String) {
        try {
            Log.d("FavoritesRepository", "Refrescando tabla de posiciones para liga $leagueId...")
            val response = api.getStandingsTable(leagueId, season)
            val dtos = response.table ?: emptyList()

            val leagueIdInt = leagueId.toIntOrNull() ?: 0
            val seasonInt = season.substringBefore("-").toIntOrNull() ?: 2024

            val entities = dtos.map { dto ->
                StandingEntity(
                    leagueId = leagueIdInt,
                    season = seasonInt,
                    rank = dto.intRank?.toIntOrNull() ?: 0,
                    teamId = dto.idTeam?.toIntOrNull() ?: 0,
                    teamName = dto.strTeam ?: "",
                    teamLogo = dto.strBadge ?: "",
                    points = dto.intPoints?.toIntOrNull() ?: 0,
                    played = dto.intPlayed?.toIntOrNull() ?: 0,
                    won = dto.intWin?.toIntOrNull() ?: 0,
                    drawn = dto.intDraw?.toIntOrNull() ?: 0,
                    lost = dto.intLoss?.toIntOrNull() ?: 0,
                    goalsFor = dto.intGoalsFor?.toIntOrNull() ?: 0,
                    goalsAgainst = dto.intGoalsAgainst?.toIntOrNull() ?: 0,
                    goalDiff = dto.intGoalDifference?.toIntOrNull() ?: 0,
                    updatedAt = System.currentTimeMillis()
                )
            }

            // Guardar localmente
            // Borramos lo previo de esa liga y reinsertamos (según requerimiento de PDF p.9)
            dao.deleteStandingsByLeague(leagueIdInt)
            dao.insertStandings(entities)
            Log.d("FavoritesRepository", "Se guardaron ${entities.size} posiciones en Room")
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error al refrescar tabla: ${e.message}", e)
        }
    }
}
