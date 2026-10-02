package com.shevault.core.design.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.design.theme.SheVaultTheme

enum class BottomNavItem(val title: String, val route: String, val icon: ImageVector) {
    HOME("Home", "home", Icons.Default.Home),
    CIRCLE("Circle", "trustedcircle", Icons.Default.People),
    HISTORY("History", "history", Icons.Default.History),
    SETTINGS("Settings", "settings", Icons.Default.Settings)
}

/**
 * 4-destination bottom navigation as specified in section 4.
 * SOS is strictly excluded from bottom navigation.
 */
@Composable
fun SheVaultBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SheVaultTheme.colors.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.values().forEach { item ->
                val selected = currentRoute == item.route
                val tint = if (selected) SheVaultTheme.colors.primary else SheVaultTheme.colors.onSurfaceVariant

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        style = SheVaultTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = tint
                    )
                }
            }
        }
    }
}
