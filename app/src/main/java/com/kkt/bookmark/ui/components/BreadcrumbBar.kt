package com.kkt.bookmark.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kkt.bookmark.data.local.entity.FolderEntity

@Composable
fun BreadcrumbBar(
    breadcrumbs: List<FolderEntity>,
    onFolderClick: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 경로가 바뀔 때 가장 오른쪽(현재 폴더)으로 자동 스크롤
    LaunchedEffect(breadcrumbs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 루트 (홈)
            Row(
                modifier = Modifier
                    .clickable { onFolderClick(null) }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "홈",
                    modifier = Modifier.size(18.dp),
                    tint = if (breadcrumbs.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " 홈",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (breadcrumbs.isEmpty()) FontWeight.Bold else FontWeight.Normal,
                    color = if (breadcrumbs.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 폴더 경로 체인
            breadcrumbs.forEachIndexed { index, folder ->
                val isLast = index == breadcrumbs.lastIndex

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(12.dp),
                    tint = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                    color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { onFolderClick(folder.id) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }
        }
    }
}
