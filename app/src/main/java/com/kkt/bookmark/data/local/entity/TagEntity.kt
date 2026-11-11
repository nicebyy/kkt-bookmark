package com.kkt.bookmark.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 다중 태그 분류를 위한 태그 엔티티.
 * 태그명은 중복될 수 없도록 UNIQUE 인덱스를 설정합니다.
 */
@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF00897B, // 기본 틸(Teal) 컬러
    val createdAt: Long = System.currentTimeMillis()
)
