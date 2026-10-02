package com.shevault.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.design.theme.SheVaultTheme

@Composable
fun PINIndicator(
    length: Int = 4,
    enteredCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(length) { index ->
            val filled = index < enteredCount
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) SheVaultTheme.colors.primary else SheVaultTheme.colors.outlineVariant
                    )
            )
        }
    }
}

@Composable
fun PINPad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> {
                            Box(modifier = Modifier.size(72.dp))
                        }
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(SheVaultTheme.colors.surfaceVariant)
                                    .clickable { onBackspaceClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Delete",
                                    tint = SheVaultTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(SheVaultTheme.colors.surface)
                                    .border(1.dp, SheVaultTheme.colors.outlineVariant, CircleShape)
                                    .clickable { onDigitClick(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SheVaultTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
