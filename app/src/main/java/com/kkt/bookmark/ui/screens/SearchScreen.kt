package com.kkt.bookmark.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.local.entity.BookmarkWithTags
import com.kkt.bookmark.ui.components.AddEditBookmarkDialog
import com.kkt.bookmark.ui.components.BookmarkCard
import com.kkt.bookmark.ui.components.FolderSelectDialog
import com.kkt.bookmark.ui.util.LinkOpener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    var editingBookmarkWithTags by remember { mutableStateOf<BookmarkWithTags?>(null) }
    var bookmarkToMove by remember { mutableStateOf<BookmarkEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("제목, URL, 메모, 태그 검색...") },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "지우기")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "검색어를 입력해 주세요." else "검색 결과가 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "검색 결과 (${searchResults.size}개)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    items(searchResults, key = { it.bookmark.id }) { bmWithTags ->
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
                                viewModel.deleteBookmark(bmWithTags.bookmark.id)
                            },
                            onTagClick = { tag ->
                                viewModel.setSearchQuery(tag.name)
                            }
                        )
                    }
                }
            }
        }
    }

    // 수정 다이얼로그
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

    // 이동 다이얼로그
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
}
