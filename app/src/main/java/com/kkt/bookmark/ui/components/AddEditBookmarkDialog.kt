package com.kkt.bookmark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.local.entity.BookmarkWithTags
import com.kkt.bookmark.data.local.entity.FolderEntity
import com.kkt.bookmark.data.local.entity.TagEntity
import com.kkt.bookmark.data.repository.MetadataFetcher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookmarkDialog(
    initialBookmarkWithTags: BookmarkWithTags? = null,
    defaultFolderId: Long? = null,
    allFolders: List<FolderEntity>,
    allTags: List<TagEntity>,
    onDismiss: () -> Unit,
    onConfirm: (bookmark: BookmarkEntity, tagNames: List<String>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val initialBm = initialBookmarkWithTags?.bookmark

    var url by remember { mutableStateOf(initialBm?.url ?: "") }
    var title by remember { mutableStateOf(initialBm?.title ?: "") }
    var memo by remember { mutableStateOf(initialBm?.memo ?: "") }
    var imageUrl by remember { mutableStateOf(initialBm?.imageUrl) }
    var faviconUrl by remember { mutableStateOf(initialBm?.faviconUrl) }
    var selectedFolderId by remember { mutableStateOf(initialBm?.folderId ?: defaultFolderId) }
    var tagInput by remember { mutableStateOf("") }
    var currentTags by remember {
        mutableStateOf(initialBookmarkWithTags?.tags?.map { it.name } ?: emptyList())
    }

    var isLoadingMeta by remember { mutableStateOf(false) }
    var folderDropdownExpanded by remember { mutableStateOf(false) }

    fun addTag(tag: String) {
        val clean = tag.trim().removePrefix("#")
        if (clean.isNotBlank() && !currentTags.contains(clean)) {
            currentTags = currentTags + clean
        }
        tagInput = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialBm == null) "새 북마크 추가" else "북마크 수정")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // URL 입력 필드 + 메타데이터 자동 추출 버튼
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("링크 URL (https://...)") },
                    singleLine = true,
                    trailingIcon = {
                        if (isLoadingMeta) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(
                                onClick = {
                                    if (url.isNotBlank()) {
                                        coroutineScope.launch {
                                            isLoadingMeta = true
                                            val meta = MetadataFetcher.fetch(url)
                                            if (title.isBlank()) title = meta.title
                                            if (imageUrl == null) imageUrl = meta.imageUrl
                                            if (faviconUrl == null) faviconUrl = meta.faviconUrl
                                            isLoadingMeta = false
                                        }
                                    }
                                },
                                enabled = url.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "정보 자동 추출",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 썸네일 미리보기
                if (!imageUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "썸네일 미리보기",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 제목
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 메모 / 노트
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("메모 (선택사항)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 폴더 선택
                Text("보관할 폴더", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))

                val selectedFolderName = allFolders.find { it.id == selectedFolderId }?.name ?: "미분류 (루트)"

                ExposedDropdownMenuBox(
                    expanded = folderDropdownExpanded,
                    onExpandedChange = { folderDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedFolderName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = folderDropdownExpanded,
                        onDismissRequest = { folderDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("미분류 (루트)") },
                            onClick = {
                                selectedFolderId = null
                                folderDropdownExpanded = false
                            }
                        )
                        allFolders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = {
                                    selectedFolderId = folder.id
                                    folderDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 다중 태그 추가 및 선택
                Text("다중 태그 분류", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = {
                            if (it.endsWith(" ") || it.endsWith(",")) {
                                addTag(it.replace(",", "").trim())
                            } else {
                                tagInput = it
                            }
                        },
                        label = { Text("태그 입력 (#개발, #팁)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = { addTag(tagInput) }) {
                        Text("추가")
                    }
                }

                // 현재 추가된 태그 칩들
                if (currentTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentTags.forEach { tag ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    // 칩 클릭 시 제거
                                    currentTags = currentTags.filter { it != tag }
                                },
                                label = { Text("#$tag ✕") }
                            )
                        }
                    }
                }

                // 기존 태그 추천 칩들
                val remainingTags = allTags.filter { !currentTags.contains(it.name) }
                if (remainingTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("빠른 태그 선택:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        remainingTags.take(8).forEach { tag ->
                            SuggestionChip(
                                onClick = { addTag(tag.name) },
                                label = { Text("+ #${tag.name}") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (url.isNotBlank() && title.isNotBlank()) {
                        val entity = initialBm?.copy(
                            url = url.trim(),
                            title = title.trim(),
                            memo = memo.trim().ifBlank { null },
                            imageUrl = imageUrl,
                            faviconUrl = faviconUrl,
                            folderId = selectedFolderId,
                            updatedAt = System.currentTimeMillis()
                        ) ?: BookmarkEntity(
                            url = url.trim(),
                            title = title.trim(),
                            memo = memo.trim().ifBlank { null },
                            imageUrl = imageUrl,
                            faviconUrl = faviconUrl,
                            folderId = selectedFolderId
                        )
                        onConfirm(entity, currentTags)
                    }
                },
                enabled = url.isNotBlank() && title.isNotBlank()
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
