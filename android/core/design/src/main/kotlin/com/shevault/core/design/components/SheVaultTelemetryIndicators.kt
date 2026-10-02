package com.shevault.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.design.theme.SheVaultTheme

@Composable
fun BatteryIndicator(
    percentage: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        percentage < 15 -> SheVaultTheme.colors.emergency
        percentage < 30 -> SheVaultTheme.colors.warning
        else -> SheVaultTheme.colors.safe
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(12.dp)
                .border(1.5.dp, color, RoundedCornerShape(2.dp))
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(1.dp))
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$percentage%${if (isCharging) " ⚡" else ""}",
            style = SheVaultTheme.typography.labelSmall,
            color = SheVaultTheme.colors.onSurface
        )
    }
}

@Composable
fun ConnectionIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isOnline) SheVaultTheme.colors.safe else SheVaultTheme.colors.warning)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isOnline) "Connected" else "Offline",
            style = SheVaultTheme.typography.labelSmall,
            color = if (isOnline) SheVaultTheme.colors.safe else SheVaultTheme.colors.warning
        )
    }
}
