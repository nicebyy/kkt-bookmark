package com.kkt.bookmark.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kkt.bookmark.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders WHERE parentId IS :parentId ORDER BY sortOrder ASC, name ASC")
    fun getFoldersByParent(parentId: Long?): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY sortOrder ASC, name ASC")
    fun getRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY id ASC")
    suspend fun getAllFoldersList(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id")
    fun getFolderById(id: Long): Flow<FolderEntity?>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderByIdSync(id: Long): FolderEntity?

    @Query("SELECT COUNT(*) FROM folders WHERE parentId = :parentId")
    suspend fun getSubFolderCount(parentId: Long): Int

    @Query("SELECT COUNT(*) FROM bookmarks WHERE folderId = :folderId")
    suspend fun getBookmarkCountInFolder(folderId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: Long)
}
