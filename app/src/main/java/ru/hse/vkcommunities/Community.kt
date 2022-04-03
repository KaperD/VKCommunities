package ru.hse.vkcommunities

data class Community(
    val name: String?,
    val logoUrl: String?,
    var isChosen: Boolean = false
)
