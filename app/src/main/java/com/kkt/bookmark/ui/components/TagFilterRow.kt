package com.kkt.bookmark.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kkt.bookmark.data.local.entity.TagEntity

@Composable
fun TagFilterRow(
    tags: List<TagEntity>,
    selectedTagId: Long?,
    onSelectTag: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 전체 태그 칩
        FilterChip(
            selected = selectedTagId == null,
            onClick = { onSelectTag(null) },
            label = { Text("전체 태그") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // 각 태그 칩
        tags.forEach { tag ->
            val isSelected = selectedTagId == tag.id
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) onSelectTag(null) else onSelectTag(tag.id)
                },
                label = { Text("#${tag.name}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(tag.color).copy(alpha = 0.25f),
                    selectedLabelColor = Color(tag.color)
                )
            )
        }
    }
}
