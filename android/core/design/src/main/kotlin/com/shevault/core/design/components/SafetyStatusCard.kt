package com.shevault.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.design.theme.SheVaultTheme

enum class SafetyCardStatus {
    PROTECTED,
    LIMITED,
    ACTIVE_INCIDENT
}

@Composable
fun SafetyStatusCard(
    status: SafetyCardStatus,
    title: String,
    subtitle: String,
    lastCheckedText: String = "Last checked just now",
    modifier: Modifier = Modifier
) {
    val (bgColor, strokeColor, contentColor, icon) = when (status) {
        SafetyCardStatus.PROTECTED -> Quadruple(
            SheVaultTheme.colors.safeContainer,
            SheVaultTheme.colors.safe,
            SheVaultTheme.colors.safe,
            Icons.Default.CheckCircle
        )
        SafetyCardStatus.LIMITED -> Quadruple(
            SheVaultTheme.colors.warningContainer,
            SheVaultTheme.colors.warning,
            SheVaultTheme.colors.warning,
            Icons.Default.Warning
        )
        SafetyCardStatus.ACTIVE_INCIDENT -> Quadruple(
            SheVaultTheme.colors.emergencyContainer,
            SheVaultTheme.colors.emergency,
            SheVaultTheme.colors.emergency,
            Icons.Default.Warning
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.5.dp, strokeColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = SheVaultTheme.typography.titleLarge,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = SheVaultTheme.typography.bodyMedium,
                color = SheVaultTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastCheckedText,
                style = SheVaultTheme.typography.labelSmall,
                color = SheVaultTheme.colors.onSurfaceVariant
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
