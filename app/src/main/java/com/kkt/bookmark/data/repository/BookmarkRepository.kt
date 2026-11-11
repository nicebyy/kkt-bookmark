package com.kkt.bookmark.data.repository

import com.kkt.bookmark.data.local.AppDatabase
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.local.entity.BookmarkTagCrossRef
import com.kkt.bookmark.data.local.entity.BookmarkWithTags
import com.kkt.bookmark.data.local.entity.FolderEntity
import com.kkt.bookmark.data.local.entity.FolderWithStats
import com.kkt.bookmark.data.local.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

enum class BookmarkFilterType {
    ALL,
    FAVORITES,
    UNREAD
}

class BookmarkRepository(private val db: AppDatabase) {

    private val folderDao = db.folderDao()
    private val tagDao = db.tagDao()
    private val bookmarkDao = db.bookmarkDao()

    // --- 폴더 관리 (다단계 계층 구조) ---

    /**
     * 특정 부모 폴더의 직계 하위 폴더 목록과 각각의 하위폴더/북마크 개수를 조회합니다.
     */
    fun getSubFoldersWithStats(parentId: Long?): Flow<List<FolderWithStats>> {
        val flow = if (parentId == null) folderDao.getRootFolders() else folderDao.getFoldersByParent(parentId)
        return flow.map { folders ->
            folders.map { folder ->
                val subCount = folderDao.getSubFolderCount(folder.id)
                val bmCount = folderDao.getBookmarkCountInFolder(folder.id)
                FolderWithStats(
                    folder = folder,
                    subFolderCount = subCount,
                    bookmarkCount = bmCount
                )
            }
        }
    }

    fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAllFolders()

    suspend fun getFolderById(id: Long): FolderEntity? = folderDao.getFolderByIdSync(id)

    /**
     * 현재 폴더의 상위 체인을 역추적하여 브레드크럼(경로) 리스트를 구성합니다.
     * [루트 -> 1단계 -> 2단계 -> 현재폴더] 순서로 반환
     */
    suspend fun getBreadcrumbs(folderId: Long?): List<FolderEntity> = withContext(Dispatchers.IO) {
        if (folderId == null) return@withContext emptyList()
        val list = mutableListOf<FolderEntity>()
        var curr: FolderEntity? = folderDao.getFolderByIdSync(folderId)
        val visited = mutableSetOf<Long>()

        while (curr != null && !visited.contains(curr.id)) {
            visited.add(curr.id)
            list.add(0, curr) // 앞에 추가하여 루트부터 순서 정렬
            curr = curr.parentId?.let { folderDao.getFolderByIdSync(it) }
        }
        list
    }

    suspend fun saveFolder(folder: FolderEntity): Long = withContext(Dispatchers.IO) {
        if (folder.id == 0L) {
            folderDao.insertFolder(folder)
        } else {
            folderDao.updateFolder(folder)
            folder.id
        }
    }

    suspend fun deleteFolder(folderId: Long) = withContext(Dispatchers.IO) {
        folderDao.deleteFolderById(folderId)
    }

    // --- 태그 관리 (다중 태그) ---

    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()

    suspend fun saveTag(name: String, color: Long = 0xFF00897B): Long = withContext(Dispatchers.IO) {
        val trimmed = name.trim().removePrefix("#")
        val existing = tagDao.getTagByName(trimmed)
        existing?.id ?: tagDao.insertTag(TagEntity(name = trimmed, color = color))
    }

    suspend fun deleteTag(tagId: Long) = withContext(Dispatchers.IO) {
        tagDao.deleteTagById(tagId)
    }

    // --- 북마크 관리 ---

    fun getBookmarksInFolder(folderId: Long?): Flow<List<BookmarkWithTags>> {
        return bookmarkDao.getBookmarksInFolder(folderId)
    }

    fun getAllBookmarks(): Flow<List<BookmarkWithTags>> = bookmarkDao.getAllBookmarks()

    fun getFavoriteBookmarks(): Flow<List<BookmarkWithTags>> = bookmarkDao.getFavoriteBookmarks()

    fun getUnreadBookmarks(): Flow<List<BookmarkWithTags>> = bookmarkDao.getUnreadBookmarks()

    fun getBookmarksByTag(tagId: Long): Flow<List<BookmarkWithTags>> = bookmarkDao.getBookmarksByTag(tagId)

    fun searchBookmarks(query: String): Flow<List<BookmarkWithTags>> = bookmarkDao.searchBookmarks(query.trim())

    /**
     * 북마크를 생성하거나 업데이트하고, 여러 개의 태그를 연결합니다.
     */
    suspend fun saveBookmark(
        bookmark: BookmarkEntity,
        tagNames: List<String>
    ): Long = withContext(Dispatchers.IO) {
        val bookmarkId = if (bookmark.id == 0L) {
            bookmarkDao.insertBookmark(bookmark)
        } else {
            bookmarkDao.updateBookmark(bookmark.copy(updatedAt = System.currentTimeMillis()))
            bookmark.id
        }

        // 태그 매핑 갱신
        tagDao.deleteCrossRefsByBookmarkId(bookmarkId)
        for (rawTag in tagNames) {
            val cleanTag = rawTag.trim().removePrefix("#")
            if (cleanTag.isNotBlank()) {
                val tagId = saveTag(cleanTag)
                tagDao.insertCrossRef(BookmarkTagCrossRef(bookmarkId = bookmarkId, tagId = tagId))
            }
        }

        bookmarkId
    }

    suspend fun deleteBookmark(bookmarkId: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(bookmarkId)
    }

    suspend fun toggleFavorite(bookmarkId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        bookmarkDao.updateFavoriteStatus(bookmarkId, isFavorite)
    }

    suspend fun toggleRead(bookmarkId: Long, isRead: Boolean) = withContext(Dispatchers.IO) {
        bookmarkDao.updateReadStatus(bookmarkId, isRead)
    }

    suspend fun moveBookmark(bookmarkId: Long, newFolderId: Long?) = withContext(Dispatchers.IO) {
        bookmarkDao.moveBookmarkToFolder(bookmarkId, newFolderId)
    }

    suspend fun moveBookmarkOrder(bookmarkId: Long, direction: Int) = withContext(Dispatchers.IO) {
        val target = bookmarkDao.getBookmarkByIdSync(bookmarkId)?.bookmark ?: return@withContext
        val list = bookmarkDao.getBookmarksListInFolder(target.folderId).toMutableList()
        val currentIndex = list.indexOfFirst { it.id == bookmarkId }
        if (currentIndex == -1) return@withContext

        val targetIndex = if (direction < 0) currentIndex - 1 else currentIndex + 1
        if (targetIndex < 0 || targetIndex >= list.size) return@withContext

        // 두 아이템의 순서 교체
        val temp = list[currentIndex]
        list[currentIndex] = list[targetIndex]
        list[targetIndex] = temp

        // 새로운 리스트 순서에 맞게 sortOrder를 0, 1, 2... 로 일괄 재부여하여 항상 결정론적으로 정렬 유지
        list.forEachIndexed { index, item ->
            bookmarkDao.updateSortOrder(item.id, index)
        }
    }
}
