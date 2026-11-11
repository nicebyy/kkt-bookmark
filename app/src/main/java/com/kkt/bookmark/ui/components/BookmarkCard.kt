package com.kkt.bookmark.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kkt.bookmark.data.local.entity.BookmarkWithTags
import com.kkt.bookmark.data.local.entity.TagEntity
import com.kkt.bookmark.data.model.BrowserType
import com.kkt.bookmark.ui.util.LinkOpener
import java.net.URI

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkCard(
    bookmarkWithTags: BookmarkWithTags,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onMoveOrder: ((direction: Int) -> Unit)? = null,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onTagClick: (TagEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bookmark = bookmarkWithTags.bookmark
    val tags = bookmarkWithTags.tags
    var showMenu by remember { mutableStateOf(false) }
    var showBrowserSubmenu by remember { mutableStateOf(false) }

    val rawHost = remember(bookmark.url) {
        try {
            URI(bookmark.url).host ?: bookmark.url
        } catch (e: Exception) {
            bookmark.url
        }
    }
    val cleanHost = rawHost.removePrefix("www.")
    val firstLetter = cleanHost.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "W"
    val faviconUrl = bookmark.faviconUrl ?: "https://www.google.com/s2/favicons?domain=$cleanHost&sz=128"
    val browserType = BrowserType.fromId(bookmark.preferredBrowser)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onEdit() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 좌측: 직관적인 사이트 파비콘/로고 아이콘 (40x40dp 둥근 박스)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstLetter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(faviconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 우측 전체 영역: [도메인 + 브라우저 뱃지 + 액션버튼], [제목 + 우측 썸네일], [태그]
            Column(modifier = Modifier.weight(1f)) {
                // 상단 헤더: 도메인 + 브라우저 뱃지 + 읽음/즐겨찾기/더보기
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cleanHost,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 🌐 특정 브라우저 지정 뱃지 (기본 브라우저가 아닐 때 눈에 띄게 표시)
                    if (browserType != BrowserType.DEFAULT) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(browserType.badgeColor).copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = browserType.displayName.split(" ")[0],
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(browserType.badgeColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // ✏️ 북마크 수정 바로가기 버튼
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "수정",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }

                    // 읽음 토글
                    IconButton(
                        onClick = onToggleRead,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (bookmark.isRead) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (bookmark.isRead) "읽음" else "안읽음",
                            modifier = Modifier.size(17.dp),
                            tint = if (bookmark.isRead) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
                        )
                    }

                    // 즐겨찾기 토글
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (bookmark.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "즐겨찾기",
                            modifier = Modifier.size(19.dp),
                            tint = if (bookmark.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline
                        )
                    }

                    // 더보기 메뉴
                    Box {
                        IconButton(
                            onClick = {
                                showMenu = true
                                showBrowserSubmenu = false
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "메뉴",
                                modifier = Modifier.size(17.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = {
                                showMenu = false
                                showBrowserSubmenu = false
                            }
                        ) {
                            if (!showBrowserSubmenu) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    text = { Text("브라우저 선택 실행 ▶") },
                                    onClick = { showBrowserSubmenu = true }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("북마크 수정") },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    }
                                )
                                if (onMoveOrder != null) {
                                    DropdownMenuItem(
                                        text = { Text("위로 이동 (▲)") },
                                        onClick = {
                                            showMenu = false
                                            onMoveOrder(-1)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("아래로 이동 (▼)") },
                                        onClick = {
                                            showMenu = false
                                            onMoveOrder(1)
                                        }
                                    )
                                }
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
                            } else {
                                // 브라우저 선택 서브메뉴
                                Text(
                                    text = "실행할 브라우저 선택",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                HorizontalDivider()
                                BrowserType.entries.forEach { b ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = b.displayName,
                                                    fontWeight = if (b == browserType) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (b == browserType) MaterialTheme.colorScheme.primary else Color.Unspecified
                                                )
                                                if (b == browserType) {
                                                    Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            showBrowserSubmenu = false
                                            LinkOpener.openUrl(context, bookmark.url, b.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 제목 + 우측 OG 썸네일 (있는 경우)
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

                        if (!bookmark.memo.isNullOrBlank() || !bookmark.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bookmark.memo ?: bookmark.description ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!bookmark.imageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(bookmark.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                // 태그 목록
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { onTagClick(tag) },
                                label = { Text("#${tag.name}", style = MaterialTheme.typography.labelSmall) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
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
}
