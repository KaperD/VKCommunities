package ru.hse.vkcommunities.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.hse.vkcommunities.model.entity.Community

@Dao
interface RecentCommunitiesDao {
    @Query("SELECT * FROM community ORDER BY name")
    suspend fun getAll(): List<Community>

    @Insert
    suspend fun insert(community: Community)

    @Delete
    suspend fun delete(community: Community)
}
