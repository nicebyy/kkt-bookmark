package com.kkt.bookmark.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.local.entity.BookmarkWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE (:folderId IS NULL AND folderId IS NULL) OR folderId = :folderId ORDER BY sortOrder ASC, createdAt DESC")
    fun getBookmarksInFolder(folderId: Long?): Flow<List<BookmarkWithTags>>

    @Transaction
    @Query("SELECT * FROM bookmarks ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkWithTags>>

    @Transaction
    @Query("SELECT * FROM bookmarks ORDER BY sortOrder ASC, createdAt DESC")
    suspend fun getAllBookmarksList(): List<BookmarkWithTags>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isFavorite = 1 ORDER BY sortOrder ASC, createdAt DESC")
    fun getFavoriteBookmarks(): Flow<List<BookmarkWithTags>>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isRead = 0 ORDER BY sortOrder ASC, createdAt DESC")
    fun getUnreadBookmarks(): Flow<List<BookmarkWithTags>>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    fun getBookmarkById(id: Long): Flow<BookmarkWithTags?>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    suspend fun getBookmarkByIdSync(id: Long): BookmarkWithTags?

    @Query("SELECT * FROM bookmarks WHERE (:folderId IS NULL AND folderId IS NULL) OR folderId = :folderId ORDER BY sortOrder ASC, createdAt DESC")
    suspend fun getBookmarksListInFolder(folderId: Long?): List<BookmarkEntity>

    @Transaction
    @Query("""
        SELECT b.* FROM bookmarks b
        INNER JOIN bookmark_tag_cross_ref r ON b.id = r.bookmarkId
        WHERE r.tagId = :tagId
        ORDER BY b.sortOrder ASC, b.createdAt DESC
    """)
    fun getBookmarksByTag(tagId: Long): Flow<List<BookmarkWithTags>>

    @Transaction
    @Query("""
        SELECT DISTINCT b.* FROM bookmarks b
        LEFT JOIN bookmark_tag_cross_ref r ON b.id = r.bookmarkId
        LEFT JOIN tags t ON r.tagId = t.id
        WHERE b.title LIKE '%' || :query || '%'
           OR b.description LIKE '%' || :query || '%'
           OR b.memo LIKE '%' || :query || '%'
           OR b.url LIKE '%' || :query || '%'
           OR t.name LIKE '%' || :query || '%'
        ORDER BY b.sortOrder ASC, b.createdAt DESC
    """)
    fun searchBookmarks(query: String): Flow<List<BookmarkWithTags>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("UPDATE bookmarks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE bookmarks SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: Long, isRead: Boolean)

    @Query("UPDATE bookmarks SET folderId = :newFolderId WHERE id = :id")
    suspend fun moveBookmarkToFolder(id: Long, newFolderId: Long?)

    @Query("UPDATE bookmarks SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
}
