package com.kkt.bookmark.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kkt.bookmark.data.local.AppDatabase
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.local.entity.BookmarkTagCrossRef
import com.kkt.bookmark.data.local.entity.FolderEntity
import com.kkt.bookmark.data.local.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupFolderDto(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val color: Long,
    val iconName: String,
    val sortOrder: Int
)

data class BackupBookmarkDto(
    val url: String,
    val title: String,
    val description: String?,
    val memo: String?,
    val imageUrl: String?,
    val faviconUrl: String?,
    val folderId: Long?,
    val tagNames: List<String>,
    val isFavorite: Boolean,
    val isRead: Boolean,
    val createdAt: Long
)

data class BackupTagDto(
    val id: Long,
    val name: String,
    val color: Long
)

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val folders: List<BackupFolderDto>,
    val tags: List<BackupTagDto>,
    val bookmarks: List<BackupBookmarkDto>
)

class BackupRestoreManager(private val context: Context, private val db: AppDatabase) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * 전체 폴더 트리, 태그, 북마크 데이터를 JSON으로 변환하여 지정된 Uri에 저장합니다.
     */
    suspend fun exportBackup(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val folders = db.folderDao().getAllFoldersList().map {
                BackupFolderDto(
                    id = it.id,
                    name = it.name,
                    parentId = it.parentId,
                    color = it.color,
                    iconName = it.iconName,
                    sortOrder = it.sortOrder
                )
            }

            val tags = db.tagDao().getAllTagsList().map {
                BackupTagDto(id = it.id, name = it.name, color = it.color)
            }

            val allBookmarksWithTags = db.bookmarkDao().getAllBookmarksList()
            val bookmarks = allBookmarksWithTags.map { bwt ->
                BackupBookmarkDto(
                    url = bwt.bookmark.url,
                    title = bwt.bookmark.title,
                    description = bwt.bookmark.description,
                    memo = bwt.bookmark.memo,
                    imageUrl = bwt.bookmark.imageUrl,
                    faviconUrl = bwt.bookmark.faviconUrl,
                    folderId = bwt.bookmark.folderId,
                    tagNames = bwt.tags.map { it.name },
                    isFavorite = bwt.bookmark.isFavorite,
                    isRead = bwt.bookmark.isRead,
                    createdAt = bwt.bookmark.createdAt
                )
            }

            val backupData = BackupData(
                folders = folders,
                tags = tags,
                bookmarks = bookmarks
            )

            val jsonString = gson.toJson(backupData)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                }
            }
            Result.success(bookmarks.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * JSON 백업 파일로부터 폴더, 태그, 북마크를 읽어 데이터베이스에 병합/복원합니다.
     */
    suspend fun importBackup(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("파일을 읽을 수 없습니다."))

            val backupData = gson.fromJson(jsonString, BackupData::class.java)

            // 1. 구 ID -> 신 ID 맵핑 (폴더 트리 복원용)
            val oldToNewFolderIdMap = mutableMapOf<Long, Long>()

            // 루트 폴더부터 단계별로 복원하기 위해 정렬
            for (fDto in backupData.folders) {
                val newFolder = FolderEntity(
                    name = fDto.name,
                    parentId = fDto.parentId?.let { oldToNewFolderIdMap[it] },
                    color = fDto.color,
                    iconName = fDto.iconName,
                    sortOrder = fDto.sortOrder
                )
                val newId = db.folderDao().insertFolder(newFolder)
                oldToNewFolderIdMap[fDto.id] = newId
            }

            // 2. 태그 복원 (이름 기준 중복 방합)
            val tagNameToIdMap = mutableMapOf<String, Long>()
            for (tDto in backupData.tags) {
                var existing = db.tagDao().getTagByName(tDto.name)
                val tagId = if (existing == null) {
                    db.tagDao().insertTag(TagEntity(name = tDto.name, color = tDto.color))
                } else {
                    existing.id
                }
                tagNameToIdMap[tDto.name] = tagId
            }

            // 3. 북마크 복원 및 태그 크로스레퍼런스 연결
            var importedCount = 0
            for (bDto in backupData.bookmarks) {
                val mappedFolderId = bDto.folderId?.let { oldToNewFolderIdMap[it] }
                val bookmark = BookmarkEntity(
                    folderId = mappedFolderId,
                    url = bDto.url,
                    title = bDto.title,
                    description = bDto.description,
                    memo = bDto.memo,
                    imageUrl = bDto.imageUrl,
                    faviconUrl = bDto.faviconUrl,
                    isFavorite = bDto.isFavorite,
                    isRead = bDto.isRead,
                    createdAt = bDto.createdAt
                )
                val newBookmarkId = db.bookmarkDao().insertBookmark(bookmark)

                for (tagName in bDto.tagNames) {
                    var tagId = tagNameToIdMap[tagName]
                    if (tagId == null) {
                        tagId = db.tagDao().insertTag(TagEntity(name = tagName))
                        tagNameToIdMap[tagName] = tagId
                    }
                    db.tagDao().insertCrossRef(
                        BookmarkTagCrossRef(bookmarkId = newBookmarkId, tagId = tagId)
                    )
                }
                importedCount++
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
