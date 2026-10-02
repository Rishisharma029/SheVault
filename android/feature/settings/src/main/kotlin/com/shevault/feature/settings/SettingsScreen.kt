package com.shevault.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Section 29: Settings Directory Screen
 */
@Composable
fun SettingsScreen(
    onNavigateToAccount: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDevSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = SheVaultTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            // Section 45: Developer Simulator banner for debugging all states
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SheVaultTheme.colors.primaryContainer)
                    .clickable { onNavigateToDevSimulator() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = SheVaultTheme.colors.primary)
                        Spacer(modifier = Modifier.padding(6.dp))
                        Column {
                            Text(text = "Developer State Simulator", style = SheVaultTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SheVaultTheme.colors.primary)
                            Text(text = "Inspect every frontend state on demand", style = SheVaultTheme.typography.bodySmall, color = SheVaultTheme.colors.onSurfaceVariant)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SheVaultTheme.colors.primary)
                }
            }

            SettingsCategoryGroup("Safety & Profile") {
                SettingsItemRow("Account", "Profile & emergency phone", Icons.Default.Person, onNavigateToAccount)
                SettingsItemRow("Security", "Safe & Duress PINs, Biometrics", Icons.Default.Lock, onNavigateToSecurity)
                SettingsItemRow("Permissions", "Location, Mic & Notifications", Icons.Default.Security, onNavigateToPermissions)
            }

            SettingsCategoryGroup("Preferences") {
                SettingsItemRow("Notifications", "Alert categories & reminders", Icons.Default.Notifications, onNavigateToNotifications)
                SettingsItemRow("Privacy", "Data retention & local storage", Icons.Default.Shield, onNavigateToPrivacy)
                SettingsItemRow("About SheVault", "Version 1.0.0 & Safety Disclaimers", Icons.Default.Info, onNavigateToAbout)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SettingsCategoryGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = SheVaultTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SheVaultTheme.colors.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SheVaultTheme.colors.surface)
                .padding(vertical = 4.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = SheVaultTheme.colors.primary)
            Spacer(modifier = Modifier.padding(8.dp))
            Column {
                Text(text = title, style = SheVaultTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = SheVaultTheme.colors.onSurface)
                Text(text = subtitle, style = SheVaultTheme.typography.labelSmall, color = SheVaultTheme.colors.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SheVaultTheme.colors.onSurfaceVariant)
    }
}
