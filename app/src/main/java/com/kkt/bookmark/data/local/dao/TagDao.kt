package com.kkt.bookmark.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kkt.bookmark.data.local.entity.BookmarkTagCrossRef
import com.kkt.bookmark.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsList(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Long): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: BookmarkTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<BookmarkTagCrossRef>)

    @Query("DELETE FROM bookmark_tag_cross_ref WHERE bookmarkId = :bookmarkId")
    suspend fun deleteCrossRefsByBookmarkId(bookmarkId: Long)

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN bookmark_tag_cross_ref r ON t.id = r.tagId
        WHERE r.bookmarkId = :bookmarkId
        ORDER BY t.name ASC
    """)
    suspend fun getTagsForBookmark(bookmarkId: Long): List<TagEntity>
}
