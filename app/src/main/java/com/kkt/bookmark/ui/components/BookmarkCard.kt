package com.kkt.bookmark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kkt.bookmark.data.local.entity.BookmarkWithTags
import com.kkt.bookmark.data.local.entity.TagEntity
import java.net.URI

@Composable
fun BookmarkCard(
    bookmarkWithTags: BookmarkWithTags,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onTagClick: (TagEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookmark = bookmarkWithTags.bookmark
    val tags = bookmarkWithTags.tags
    var showMenu by remember { mutableStateOf(false) }

    val host = remember(bookmark.url) {
        try {
            URI(bookmark.url).host ?: bookmark.url
        } catch (e: Exception) {
            bookmark.url
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 상단 행: 파비콘 + 호스트 도메인 + 더보기/즐겨찾기 액션들
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 파비콘
                if (bookmark.faviconUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bookmark.faviconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = host,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 읽음 표시 토글
                IconButton(
                    onClick = onToggleRead,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (bookmark.isRead) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (bookmark.isRead) "읽음" else "안읽음",
                        modifier = Modifier.size(18.dp),
                        tint = if (bookmark.isRead) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
                    )
                }

                // 즐겨찾기 토글
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (bookmark.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "즐겨찾기",
                        modifier = Modifier.size(20.dp),
                        tint = if (bookmark.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline
                    )
                }

                // 더보기 메뉴
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "메뉴",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("북마크 수정") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("다른 폴더로 이동") },
                            onClick = {
                                showMenu = false
                                onMove()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("링크 공유하기") },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 본문 콘텐츠: 썸네일(있으면 우측) + 제목 & 메모(좌측)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bookmark.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    val note = bookmark.memo ?: bookmark.description
                    if (!note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!bookmark.imageUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bookmark.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "썸네일",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            // 하단: 태그 목록
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        SuggestionChip(
                            onClick = { onTagClick(tag) },
                            label = {
                                Text(
                                    text = "#${tag.name}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(tag.color).copy(alpha = 0.12f),
                                labelColor = Color(tag.color)
                            ),
                            border = null,
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }
        }
    }
}
