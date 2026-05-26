package com.example.sportshub.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.local.entities.FavoriteEntity
import com.example.sportshub.data.local.entities.NewsEntity
import com.example.sportshub.data.local.entities.StandingEntity
import com.example.sportshub.data.local.entities.TeamEntity

@Database(
    entities = [
        TeamEntity::class,
        MatchEntity::class,
        NewsEntity::class,
        StandingEntity::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class SportsDatabase : RoomDatabase() {

    abstract fun sportsDao(): SportsDao

    companion object {
        @Volatile
        private var INSTANCE: SportsDatabase? = null

        fun getDatabase(context: Context): SportsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SportsDatabase::class.java,
                    "sportshub_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}