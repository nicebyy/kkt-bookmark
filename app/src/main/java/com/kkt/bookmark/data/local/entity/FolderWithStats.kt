package com.kkt.bookmark.data.local.entity

import androidx.room.Embedded

/**
 * 폴더 정보와 하위 폴더 수, 북마크 수를 함께 갖는 모델.
 */
data class FolderWithStats(
    @Embedded
    val folder: FolderEntity,
    val bookmarkCount: Int = 0,
    val subFolderCount: Int = 0
)
