package com.kkt.bookmark.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kkt.bookmark.BookmarkApp
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.repository.MetadataFetcher
import com.kkt.bookmark.ui.theme.KKTBookmarkTheme
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rawText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        val initialUrl = MetadataFetcher.extractUrlFromText(rawText) ?: rawText.trim()

        setContent {
            KKTBookmarkTheme {
                ShareBottomSheet(
                    initialUrl = initialUrl,
                    onDismiss = { finish() },
                    onSaved = {
                        Toast.makeText(this, "북마크가 저장되었습니다!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    initialUrl: String,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = (context.applicationContext as BookmarkApp).repository

    val allFolders by repository.getAllFolders().collectAsState(initial = emptyList())
    val allTags by repository.getAllTags().collectAsState(initial = emptyList())

    var url by remember { mutableStateOf(initialUrl) }
    var title by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var faviconUrl by remember { mutableStateOf<String?>(null) }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var tagInput by remember { mutableStateOf("") }
    var currentTags by remember { mutableStateOf(listOf<String>()) }
    var isLoadingMeta by remember { mutableStateOf(true) }
    var folderDropdownExpanded by remember { mutableStateOf(false) }

    // 시작 시 메타데이터 자동 추출
    LaunchedEffect(initialUrl) {
        if (initialUrl.isNotBlank()) {
            val meta = MetadataFetcher.fetch(initialUrl)
            title = meta.title
            imageUrl = meta.imageUrl
            faviconUrl = meta.faviconUrl
            isLoadingMeta = false
        } else {
            isLoadingMeta = false
        }
    }

    fun addTag(tag: String) {
        val clean = tag.trim().removePrefix("#")
        if (clean.isNotBlank() && !currentTags.contains(clean)) {
            currentTags = currentTags + clean
        }
        tagInput = ""
    }

    // 반투명 배경 위에 뜬 바텀시트 컨테이너
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {}, // 클릭 이벤트 차단
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 바텀시트 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdded,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "북마크 즉시 저장",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (isLoadingMeta) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 썸네일 미리보기 (있을 경우)
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 제목 입력
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 보관할 폴더 선택 (드롭다운)
                val selectedFolderName = allFolders.find { it.id == selectedFolderId }?.name ?: "미분류 (홈)"

                ExposedDropdownMenuBox(
                    expanded = folderDropdownExpanded,
                    onExpandedChange = { folderDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedFolderName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("보관할 폴더") },
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
                            text = { Text("미분류 (홈)") },
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

                Spacer(modifier = Modifier.height(8.dp))

                // 태그 입력
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

                // 태그 칩들
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
                                onClick = { currentTags = currentTags.filter { it != tag } },
                                label = { Text("#$tag ✕") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 저장 및 취소 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val bookmark = BookmarkEntity(
                                    url = url.trim(),
                                    title = if (title.isNotBlank()) title.trim() else url,
                                    memo = memo.trim().ifBlank { null },
                                    imageUrl = imageUrl,
                                    faviconUrl = faviconUrl,
                                    folderId = selectedFolderId
                                )
                                repository.saveBookmark(bookmark, currentTags)
                                onSaved()
                            }
                        },
                        enabled = url.isNotBlank()
                    ) {
                        Text("저장하기")
                    }
                }
            }
        }
    }
}
