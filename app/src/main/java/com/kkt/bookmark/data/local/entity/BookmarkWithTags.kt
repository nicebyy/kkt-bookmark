package com.kkt.bookmark.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * 북마크 정보와 연결된 모든 태그 목록을 포함하는 릴레이션 데이터 모델.
 */
data class BookmarkWithTags(
    @Embedded
    val bookmark: BookmarkEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookmarkTagCrossRef::class,
            parentColumn = "bookmarkId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
