package com.kkt.bookmark.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.local.entity.BookmarkWithTags
import com.kkt.bookmark.data.local.entity.FolderEntity
import com.kkt.bookmark.data.repository.BookmarkFilterType
import com.kkt.bookmark.ui.components.AddEditBookmarkDialog
import com.kkt.bookmark.ui.components.AddEditFolderDialog
import com.kkt.bookmark.ui.components.BookmarkCard
import com.kkt.bookmark.ui.components.BreadcrumbBar
import com.kkt.bookmark.ui.components.FolderCard
import com.kkt.bookmark.ui.components.FolderSelectDialog
import com.kkt.bookmark.ui.components.TagFilterRow
import com.kkt.bookmark.ui.util.LinkOpener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToFolderTree: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current

    val currentFolderId by viewModel.currentFolderId.collectAsState()
    val breadcrumbs by viewModel.breadcrumbs.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val selectedTagId by viewModel.selectedTagId.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val subFolders by viewModel.subFolders.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    // 다이얼로그 상태들
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var editingBookmarkWithTags by remember { mutableStateOf<BookmarkWithTags?>(null) }
    var bookmarkToMove by remember { mutableStateOf<BookmarkEntity?>(null) }
    var bookmarkToDelete by remember { mutableStateOf<Long?>(null) }

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var editingFolder by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<Long?>(null) }

    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KKT 북마크",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "검색")
                    }
                    IconButton(onClick = onNavigateToFolderTree) {
                        Icon(imageVector = Icons.Default.FolderSpecial, contentDescription = "폴더 트리 관리")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "설정 및 백업")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showFabMenu) {
                    // 폴더 생성 보조 FAB
                    FloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            showAddFolderDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("폴더 생성", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    // 북마크 추가 보조 FAB
                    FloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            showAddBookmarkDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("북마크 추가", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                // 메인 FAB 토글
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Add else Icons.Default.Add,
                        contentDescription = "추가"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 필터 탭 (전체, 즐겨찾기, 읽지 않음)
            PrimaryTabRow(
                selectedTabIndex = currentFilter.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = currentFilter == BookmarkFilterType.ALL,
                    onClick = { viewModel.setFilter(BookmarkFilterType.ALL) },
                    text = { Text("전체") }
                )
                Tab(
                    selected = currentFilter == BookmarkFilterType.FAVORITES,
                    onClick = { viewModel.setFilter(BookmarkFilterType.FAVORITES) },
                    text = { Text("★ 즐겨찾기") }
                )
                Tab(
                    selected = currentFilter == BookmarkFilterType.UNREAD,
                    onClick = { viewModel.setFilter(BookmarkFilterType.UNREAD) },
                    text = { Text("읽지 않음") }
                )
            }

            // 폴더 계층 구조 브레드크럼 바 (홈 > 개발 > 안드로이드...)
            BreadcrumbBar(
                breadcrumbs = breadcrumbs,
                onFolderClick = { targetFolderId ->
                    viewModel.navigateToFolder(targetFolderId)
                }
            )

            // 다중 태그 필터 바
            TagFilterRow(
                tags = allTags,
                selectedTagId = selectedTagId,
                onSelectTag = { tagId ->
                    viewModel.selectTag(tagId)
                }
            )

            // 메인 콘텐츠 리스트 (하위 폴더 + 북마크)
            val isEmpty = subFolders.isEmpty() && bookmarks.isEmpty()

            if (isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTagId != null) "선택한 태그에 속한 북마크가 없습니다."
                            else if (currentFilter == BookmarkFilterType.FAVORITES) "즐겨찾기한 북마크가 없습니다."
                            else if (currentFilter == BookmarkFilterType.UNREAD) "모든 북마크를 읽었습니다!"
                            else "이 폴더는 비어 있습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showAddBookmarkDialog = true }) {
                            Text("+ 첫 번째 북마크 추가하기")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. 하위 폴더 섹션 (필터가 ALL일 때만 표시)
                    if (currentFilter == BookmarkFilterType.ALL && selectedTagId == null && subFolders.isNotEmpty()) {
                        item {
                            Text(
                                text = "하위 폴더 (${subFolders.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(subFolders, key = { "folder_${it.folder.id}" }) { folderWithStats ->
                            FolderCard(
                                folderWithStats = folderWithStats,
                                onClick = {
                                    viewModel.navigateToFolder(folderWithStats.folder.id)
                                },
                                onEdit = {
                                    editingFolder = folderWithStats.folder
                                },
                                onDelete = {
                                    folderToDelete = folderWithStats.folder.id
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // 2. 북마크 섹션
                    if (bookmarks.isNotEmpty()) {
                        item {
                            Text(
                                text = "북마크 (${bookmarks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(bookmarks, key = { "bm_${it.bookmark.id}" }) { bmWithTags ->
                            BookmarkCard(
                                bookmarkWithTags = bmWithTags,
                                onClick = {
                                    viewModel.toggleRead(bmWithTags.bookmark.id, true)
                                    LinkOpener.openUrl(context, bmWithTags.bookmark.url)
                                },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(bmWithTags.bookmark.id, !bmWithTags.bookmark.isFavorite)
                                },
                                onToggleRead = {
                                    viewModel.toggleRead(bmWithTags.bookmark.id, !bmWithTags.bookmark.isRead)
                                },
                                onEdit = {
                                    editingBookmarkWithTags = bmWithTags
                                },
                                onMove = {
                                    bookmarkToMove = bmWithTags.bookmark
                                },
                                onShare = {
                                    LinkOpener.shareLink(context, bmWithTags.bookmark.title, bmWithTags.bookmark.url)
                                },
                                onDelete = {
                                    bookmarkToDelete = bmWithTags.bookmark.id
                                },
                                onTagClick = { tag ->
                                    viewModel.selectTag(tag.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- 다이얼로그 팝업들 ---

    // 새 북마크 추가
    if (showAddBookmarkDialog) {
        AddEditBookmarkDialog(
            defaultFolderId = currentFolderId,
            allFolders = allFolders,
            allTags = allTags,
            onDismiss = { showAddBookmarkDialog = false },
            onConfirm = { newBm, tags ->
                viewModel.saveBookmark(newBm, tags)
                showAddBookmarkDialog = false
            }
        )
    }

    // 북마크 수정
    editingBookmarkWithTags?.let { bmWithTags ->
        AddEditBookmarkDialog(
            initialBookmarkWithTags = bmWithTags,
            defaultFolderId = bmWithTags.bookmark.folderId,
            allFolders = allFolders,
            allTags = allTags,
            onDismiss = { editingBookmarkWithTags = null },
            onConfirm = { updatedBm, tags ->
                viewModel.saveBookmark(updatedBm, tags)
                editingBookmarkWithTags = null
            }
        )
    }

    // 새 폴더 추가
    if (showAddFolderDialog) {
        AddEditFolderDialog(
            defaultParentId = currentFolderId,
            allFolders = allFolders,
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name, parentId, color ->
                viewModel.saveFolder(name, parentId, color)
                showAddFolderDialog = false
            }
        )
    }

    // 폴더 수정
    editingFolder?.let { folder ->
        AddEditFolderDialog(
            initialFolder = folder,
            allFolders = allFolders,
            onDismiss = { editingFolder = null },
            onConfirm = { name, parentId, color ->
                viewModel.saveFolder(name, parentId, color, initialFolder = folder)
                editingFolder = null
            }
        )
    }

    // 폴더 이동 다이얼로그
    bookmarkToMove?.let { bm ->
        FolderSelectDialog(
            allFolders = allFolders,
            currentFolderId = bm.folderId,
            onDismiss = { bookmarkToMove = null },
            onSelectFolder = { newFolderId ->
                viewModel.moveBookmark(bm.id, newFolderId)
                bookmarkToMove = null
            }
        )
    }

    // 폴더 삭제 확인 다이얼로그
    folderToDelete?.let { folderId ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("폴더 삭제") },
            text = { Text("이 폴더를 삭제하시겠습니까? 하위 폴더도 함께 삭제되며, 포함된 북마크는 홈(미분류)으로 이동됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFolder(folderId)
                        folderToDelete = null
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    // 북마크 삭제 확인 다이얼로그
    bookmarkToDelete?.let { bmId ->
        AlertDialog(
            onDismissRequest = { bookmarkToDelete = null },
            title = { Text("북마크 삭제") },
            text = { Text("이 북마크를 정말 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBookmark(bmId)
                        bookmarkToDelete = null
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookmarkToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }
}
