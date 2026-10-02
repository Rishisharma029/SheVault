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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
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
import com.shevault.core.design.tokens.SheVaultSafetyStatus

enum class SafetyCardStatus {
    PROTECTED,
    LIMITED,
    ACTIVE_INCIDENT
}

/**
 * Primary Safety Status Card adhering to the invariant:
 * Never communicate safety state with color alone (Color + Icon + Text).
 */
@Composable
fun SafetyStatusCard(
    status: SheVaultSafetyStatus,
    title: String? = null,
    subtitle: String? = null,
    lastCheckedText: String = "Last checked just now",
    modifier: Modifier = Modifier
) {
    val descriptor = SheVaultTheme.status.descriptorFor(status)
    val displayTitle = title ?: descriptor.title
    val displaySubtitle = subtitle ?: descriptor.description

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(descriptor.containerColor, RoundedCornerShape(16.dp))
            .border(1.5.dp, descriptor.borderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = descriptor.icon,
                    contentDescription = descriptor.title,
                    tint = descriptor.contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = displayTitle,
                    style = SheVaultTheme.typography.titleLarge,
                    color = descriptor.contentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displaySubtitle,
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

/**
 * Backward compatibility overload for legacy SafetyCardStatus enum.
 */
@Composable
fun SafetyStatusCard(
    status: SafetyCardStatus,
    title: String,
    subtitle: String,
    lastCheckedText: String = "Last checked just now",
    modifier: Modifier = Modifier
) {
    val mappedStatus = when (status) {
        SafetyCardStatus.PROTECTED -> SheVaultSafetyStatus.PROTECTED
        SafetyCardStatus.LIMITED -> SheVaultSafetyStatus.LIMITED
        SafetyCardStatus.ACTIVE_INCIDENT -> SheVaultSafetyStatus.ACTIVE
    }
    SafetyStatusCard(
        status = mappedStatus,
        title = title,
        subtitle = subtitle,
        lastCheckedText = lastCheckedText,
        modifier = modifier
    )
}
