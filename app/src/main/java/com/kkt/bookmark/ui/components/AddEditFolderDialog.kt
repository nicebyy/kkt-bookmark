package com.kkt.bookmark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kkt.bookmark.data.local.entity.FolderEntity
import com.kkt.bookmark.ui.theme.FolderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFolderDialog(
    initialFolder: FolderEntity? = null,
    defaultParentId: Long? = null,
    allFolders: List<FolderEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, parentId: Long?, color: Long) -> Unit
) {
    var name by remember { mutableStateOf(initialFolder?.name ?: "") }
    var selectedParentId by remember { mutableStateOf(initialFolder?.parentId ?: defaultParentId) }
    var selectedColor by remember { mutableLongStateOf(initialFolder?.color ?: FolderColors[0]) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }

    // 자기 자신 및 자신의 자식들은 부모로 선택할 수 없도록 필터링
    val eligibleParentFolders = remember(allFolders, initialFolder) {
        if (initialFolder == null) {
            allFolders
        } else {
            allFolders.filter { it.id != initialFolder.id }
        }
    }

    val selectedParentName = eligibleParentFolders.find { it.id == selectedParentId }?.name ?: "최상위 (루트 폴더)"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialFolder == null) "새 폴더 추가" else "폴더 수정")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 폴더 이름
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("폴더 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 상위 폴더 선택 (다단계 계층)
                Text("상위 폴더 위치", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = parentDropdownExpanded,
                    onExpandedChange = { parentDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedParentName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = parentDropdownExpanded,
                        onDismissRequest = { parentDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("최상위 (루트 폴더)") },
                            onClick = {
                                selectedParentId = null
                                parentDropdownExpanded = false
                            }
                        )
                        eligibleParentFolders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = {
                                    selectedParentId = folder.id
                                    parentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 폴더 테마 색상 선택
                Text("폴더 색상", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FolderColors.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorVal }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedParentId, selectedColor)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
