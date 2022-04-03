package ru.hse.vkcommunities

import com.vk.dto.common.id.UserId

data class Community(
    val id: UserId,
    val name: String?,
    val logoUrl: String?,
    var isChosen: Boolean = false
)
