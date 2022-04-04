package ru.hse.vkcommunities.model.entity

import androidx.room.*
import com.vk.dto.common.id.UserId

@Entity(
    tableName = "community",
    indices = [Index("value")]
)
data class Community(
    @PrimaryKey
    @Embedded
    val id: UserId,
    val name: String?,
    @ColumnInfo(name = "logo_url")
    val logoUrl: String?,
    @Ignore
    var isChosen: Boolean = false,
    @Ignore
    var isSubscribed: Boolean = false
) {
    constructor(
        id: UserId,
        name: String?,
        logoUrl: String?
    ) : this(id, name, logoUrl, false, false)
}
