package com.kkt.bookmark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 북마크(N)와 태그(M) 간의 다대다(Many-to-Many) 매핑 테이블.
 * 북마크 또는 태그가 삭제되면 매핑도 CASCADE로 자동 삭제됩니다.
 */
@Entity(
    tableName = "bookmark_tag_cross_ref",
    primaryKeys = ["bookmarkId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = BookmarkEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookmarkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookmarkId"]),
        Index(value = ["tagId"])
    ]
)
data class BookmarkTagCrossRef(
    val bookmarkId: Long,
    val tagId: Long
)
