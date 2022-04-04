package ru.hse.vkcommunities.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.hse.vkcommunities.model.dao.RecentCommunitiesDao
import ru.hse.vkcommunities.model.entity.Community

@Database(
    entities = [Community::class],
    version = 1
)
abstract class RecentCommunitiesDatabase : RoomDatabase() {
    abstract fun recentCommunitiesDao(): RecentCommunitiesDao

    companion object {
        @Volatile private var instance: RecentCommunitiesDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                RecentCommunitiesDatabase::class.java, "recent_communities.db")
                .build()
    }
}
