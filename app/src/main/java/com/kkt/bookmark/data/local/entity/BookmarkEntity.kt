package com.kkt.bookmark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 북마크 엔티티.
 * 특정 폴더(folderId)에 소속될 수 있으며, null일 경우 루트/미분류 상태입니다.
 * 폴더 삭제 시 SET_NULL 처리하여 북마크 데이터가 유실되지 않도록 보호합니다.
 */
@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["folderId"]),
        Index(value = ["url"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isRead"]),
        Index(value = ["createdAt"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folderId: Long? = null,
    val url: String,
    val title: String,
    val description: String? = null,
    val memo: String? = null,
    val imageUrl: String? = null,
    val faviconUrl: String? = null,
    val isFavorite: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
