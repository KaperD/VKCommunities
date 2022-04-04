package ru.hse.vkcommunities.model.repository

import android.content.Context
import ru.hse.vkcommunities.model.database.RecentCommunitiesDatabase
import ru.hse.vkcommunities.model.entity.Community

class RecentCommunitiesRepository(context: Context) {
    private val recentCommunitiesDao = RecentCommunitiesDatabase(context).recentCommunitiesDao()

    suspend fun getAll(): List<Community> = recentCommunitiesDao.getAll()

    suspend fun insert(community: Community) = recentCommunitiesDao.insert(community)

    suspend fun delete(community: Community) = recentCommunitiesDao.delete(community)
}
