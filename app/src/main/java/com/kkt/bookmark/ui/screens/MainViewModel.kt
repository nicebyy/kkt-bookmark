package com.kkt.bookmark.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kkt.bookmark.BookmarkApp
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.local.entity.BookmarkWithTags
import com.kkt.bookmark.data.local.entity.FolderEntity
import com.kkt.bookmark.data.local.entity.FolderWithStats
import com.kkt.bookmark.data.local.entity.TagEntity
import com.kkt.bookmark.data.repository.BookmarkFilterType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BookmarkApp).repository

    // 현재 진입한 폴더 ID (null이면 루트)
    private val _currentFolderId = MutableStateFlow<Long?>(null)
    val currentFolderId: StateFlow<Long?> = _currentFolderId.asStateFlow()

    // 브레드크럼 경로 리스트
    private val _breadcrumbs = MutableStateFlow<List<FolderEntity>>(emptyList())
    val breadcrumbs: StateFlow<List<FolderEntity>> = _breadcrumbs.asStateFlow()

    // 필터 (전체, 즐겨찾기, 읽지 않음)
    private val _currentFilter = MutableStateFlow(BookmarkFilterType.ALL)
    val currentFilter: StateFlow<BookmarkFilterType> = _currentFilter.asStateFlow()

    // 선택된 태그 필터 (null이면 전체)
    private val _selectedTagId = MutableStateFlow<Long?>(null)
    val selectedTagId: StateFlow<Long?> = _selectedTagId.asStateFlow()

    // 검색어
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 모든 폴더 목록
    val allFolders: StateFlow<List<FolderEntity>> = repository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 모든 태그 목록
    val allTags: StateFlow<List<TagEntity>> = repository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 현재 폴더의 직계 하위 폴더들 (트리 구조)
    @OptIn(ExperimentalCoroutinesApi::class)
    val subFolders: StateFlow<List<FolderWithStats>> = _currentFolderId
        .flatMapLatest { folderId ->
            repository.getSubFoldersWithStats(folderId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 현재 조건에 맞게 필터링된 북마크 목록
    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarks: StateFlow<List<BookmarkWithTags>> = combine(
        _currentFolderId,
        _currentFilter,
        _selectedTagId
    ) { folderId, filter, tagId ->
        Triple(folderId, filter, tagId)
    }.flatMapLatest { (folderId, filter, tagId) ->
        when {
            tagId != null -> repository.getBookmarksByTag(tagId)
            filter == BookmarkFilterType.FAVORITES -> repository.getFavoriteBookmarks()
            filter == BookmarkFilterType.UNREAD -> repository.getUnreadBookmarks()
            else -> repository.getBookmarksInFolder(folderId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 검색 결과
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<BookmarkWithTags>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllBookmarks()
            } else {
                repository.searchBookmarks(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        updateBreadcrumbs(null)
    }

    fun navigateToFolder(folderId: Long?) {
        _currentFolderId.value = folderId
        _selectedTagId.value = null // 폴더 이동 시 태그 필터 초기화
        updateBreadcrumbs(folderId)
    }

    private fun updateBreadcrumbs(folderId: Long?) {
        viewModelScope.launch {
            _breadcrumbs.value = repository.getBreadcrumbs(folderId)
        }
    }

    fun setFilter(filter: BookmarkFilterType) {
        _currentFilter.value = filter
        _selectedTagId.value = null
    }

    fun selectTag(tagId: Long?) {
        _selectedTagId.value = tagId
        if (tagId != null) {
            _currentFilter.value = BookmarkFilterType.ALL
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveFolder(name: String, parentId: Long?, color: Long, initialFolder: FolderEntity? = null) {
        viewModelScope.launch {
            val folder = initialFolder?.copy(
                name = name,
                parentId = parentId,
                color = color
            ) ?: FolderEntity(
                name = name,
                parentId = parentId,
                color = color
            )
            repository.saveFolder(folder)
            updateBreadcrumbs(_currentFolderId.value)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            if (_currentFolderId.value == folderId) {
                val curr = repository.getFolderById(folderId)
                navigateToFolder(curr?.parentId)
            }
            repository.deleteFolder(folderId)
        }
    }

    fun saveBookmark(bookmark: BookmarkEntity, tagNames: List<String>) {
        viewModelScope.launch {
            repository.saveBookmark(bookmark, tagNames)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmarkId)
        }
    }

    fun toggleFavorite(bookmarkId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(bookmarkId, isFavorite)
        }
    }

    fun toggleRead(bookmarkId: Long, isRead: Boolean) {
        viewModelScope.launch {
            repository.toggleRead(bookmarkId, isRead)
        }
    }

    fun moveBookmark(bookmarkId: Long, targetFolderId: Long?) {
        viewModelScope.launch {
            repository.moveBookmark(bookmarkId, targetFolderId)
        }
    }

    fun moveBookmarkOrder(bookmarkId: Long, direction: Int) {
        viewModelScope.launch {
            repository.moveBookmarkOrder(bookmarkId, direction)
        }
    }
}
