package ru.yourname.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

private val PRESET_COLORS = listOf(
    "#FFFFFF", "#FCE4EC", "#F8BBD0", "#F48FB1", "#E91E63",
    "#E3F2FD", "#90CAF9", "#2196F3", "#0D47A1", "#E0F7FA",
    "#B2EBF2", "#4DD0E1", "#0097A7", "#E8F5E9", "#A5D6A7",
    "#4CAF50", "#2E7D32", "#FFF3E0", "#FFCC80", "#FF9800",
    "#E65100", "#F3E5F5", "#CE93D8", "#9C27B0", "#4A148C",
    "#FBE9E7", "#FFAB91", "#FF5722", "#D84315", "#ECEFF1",
    "#B0BEC5", "#607D8B", "#37474F", "#212121", "#000000"
)

private val PRESET_GRADIENTS = listOf(
    "#FF9A9E,#FAD0C4",
    "#A18CD1,#FBC2EB",
    "#FAD0C4,#FFD1FF",
    "#FFECD2,#FCB69F",
    "#A1C4FD,#C2E9FB",
    "#D4FC79,#96E6A1",
    "#84FAB0,#8FD3F4",
    "#F093FB,#F5576C",
    "#4FACFE,#00F2FE",
    "#43E97B,#38F9D7",
    "#FA709A,#FEE140",
    "#30CFD0,#330867",
    "#000000,#434343",
    "#1A2A6C,#B21F1F",
    "#0F2027,#203A43"
)

@Composable
fun CustomColorDialog(
    initial: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    var hex1 by remember {
        mutableStateOf(initial?.split(",")?.getOrNull(0)?.trim() ?: "#FFFFFF")
    }
    var hex2 by remember {
        mutableStateOf(initial?.split(",")?.getOrNull(1)?.trim() ?: "#000000")
    }
    var gradient by remember {
        mutableStateOf(initial?.contains(",") == true)
    }

    // Валидация: чистим и нормализуем
    fun normalize(input: String): String {
        val clean = input.trim().removePrefix("#").uppercase()
        val filtered = clean.filter { it in "0123456789ABCDEF" }.take(6)
        return "#$filtered"
    }

    val normalized1 = normalize(hex1)
    val normalized2 = normalize(hex2)
    val valid1 = normalized1.length == 7
    val valid2 = normalized2.length == 7
    val canSave = valid1 && (!gradient || valid2)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Свой цвет карточки") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Превью
                val previewBrush: Brush = if (gradient && valid1 && valid2) {
                    Brush.verticalGradient(
                        listOf(
                            parseCustomColor(normalized1).first(),
                            parseCustomColor(normalized2).first()
                        )
                    )
                } else if (valid1) {
                    Brush.verticalGradient(
                        listOf(
                            parseCustomColor(normalized1).first(),
                            parseCustomColor(normalized1).first()
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.Gray, Color.Gray))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(previewBrush)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (gradient) "$normalized1 → $normalized2" else normalized1,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (valid1 && parseCustomColor(normalized1).first().isLight())
                            Color.Black else Color.White
                    )
                }

                // Тумблер "градиент"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = gradient, onCheckedChange = { gradient = it })
                    Spacer(Modifier.width(12.dp))
                    Text("Градиент (2 цвета)")
                }

                // Основной цвет
                OutlinedTextField(
                    value = hex1,
                    onValueChange = { hex1 = it },
                    label = { Text(if (gradient) "Цвет 1 (верх)" else "Цвет") },
                    placeholder = { Text("#RRGGBB") },
                    singleLine = true,
                    isError = !valid1,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )

                if (gradient) {
                    OutlinedTextField(
                        value = hex2,
                        onValueChange = { hex2 = it },
                        label = { Text("Цвет 2 (низ)") },
                        placeholder = { Text("#RRGGBB") },
                        singleLine = true,
                        isError = !valid2,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Палитра", style = MaterialTheme.typography.titleSmall)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PRESET_COLORS) { c ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(parseCustomColor(c).first())
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    if (gradient) {
                                        // если градиент — заливка по очереди: сначала цвет 1, потом цвет 2
                                        val cur1 = normalize(hex1)
                                        val cur2 = normalize(hex2)
                                        if (cur1 == "#FFFFFF") hex1 = c else hex2 = c
                                    } else {
                                        hex1 = c
                                    }
                                }
                        )
                    }
                }

                if (gradient) {
                    Text("Готовые градиенты", style = MaterialTheme.typography.titleSmall)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PRESET_GRADIENTS) { g ->
                            val parts = g.split(",")
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                parseCustomColor(parts[0]).first(),
                                                parseCustomColor(parts[1]).first()
                                            )
                                        )
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .clickable {
                                        hex1 = parts[0]
                                        hex2 = parts[1]
                                    }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val result = if (gradient) "$normalized1,$normalized2" else normalized1
                    onSave(result)
                },
                enabled = canSave
            ) { Text("Сохранить") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onSave(null) }) { Text("Сбросить") }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        }
    )
}
