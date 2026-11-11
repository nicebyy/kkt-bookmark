package com.kkt.bookmark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 다단계 폴더(트리 구조)를 지원하는 폴더 엔티티.
 * parentId가 null이면 최상위 루트 폴더이며,
 * 부모 폴더 삭제 시 하위 폴더의 parentId를 CASCADE(동반 삭제) 또는 SetNull로 처리할 수 있습니다.
 */
@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["parentId"])
    ]
)
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val color: Long = 0xFF3D5AFE, // 기본 블루 컬러
    val iconName: String = "folder",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
