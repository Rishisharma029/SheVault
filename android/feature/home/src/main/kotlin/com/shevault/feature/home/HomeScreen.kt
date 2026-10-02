package com.shevault.feature.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.common.mock.MockRepositories
import com.shevault.core.common.state.ConnectivityState
import com.shevault.core.common.state.GlobalStateManager
import com.shevault.core.common.state.ProtectionState
import com.shevault.core.design.components.BatteryIndicator
import com.shevault.core.design.components.ConnectionIndicator
import com.shevault.core.design.components.OfflineBanner
import com.shevault.core.design.components.SafetyCardStatus
import com.shevault.core.design.components.SafetyStatusCard
import com.shevault.core.design.components.SheVaultSosComponent
import com.shevault.core.design.theme.SheVaultTheme

@Composable
fun HomeScreen(
    onNavigateToSos: () -> Unit,
    onNavigateToSafeRoute: () -> Unit,
    onNavigateToTrustedCircle: () -> Unit,
    onNavigateToDiscreet: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appState by GlobalStateManager.appState.collectAsState()
    val incidents by MockRepositories.incidents.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Global Offline Banner
            OfflineBanner(visible = appState.connectivityState == ConnectivityState.OFFLINE)

            // Header Section (Section 8)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SheVault",
                        style = SheVaultTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SheVaultTheme.colors.onSurface
                    )
                    Text(
                        text = "Good evening, Rishi",
                        style = SheVaultTheme.typography.bodyMedium,
                        color = SheVaultTheme.colors.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BatteryIndicator(percentage = appState.batteryPercent, isCharging = appState.isBatteryCharging)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SheVaultTheme.colors.surfaceVariant)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "R",
                            fontWeight = FontWeight.Bold,
                            color = SheVaultTheme.colors.primary
                        )
                    }
                }
            }

            // Safety Status Card (Section 8)
            val cardStatus = when (appState.protectionState) {
                ProtectionState.PROTECTED -> SafetyCardStatus.PROTECTED
                ProtectionState.LIMITED -> SafetyCardStatus.LIMITED
                ProtectionState.UNAVAILABLE -> SafetyCardStatus.LIMITED
            }
            SafetyStatusCard(
                status = cardStatus,
                title = if (appState.protectionState == ProtectionState.PROTECTED) "✓ You're Protected" else "△ Limited Protection",
                subtitle = if (appState.protectionState == ProtectionState.PROTECTED) "Your safety tools are ready" else "Core local safety tools active",
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Primary Centered SOS Area (Section 9 & 10)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SheVaultSosComponent(
                    onSosTriggered = {
                        GlobalStateManager.triggerSos()
                        onNavigateToSos()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Tools Section (Section 19)
            Text(
                text = "Quick Safety Tools",
                style = SheVaultTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickToolCard(
                    title = "Trusted Circle",
                    desc = "3 contacts",
                    icon = Icons.Default.People,
                    onClick = onNavigateToTrustedCircle,
                    modifier = Modifier.weight(1f)
                )
                QuickToolCard(
                    title = "Safe Route",
                    desc = "Plan route",
                    icon = Icons.Default.AltRoute,
                    onClick = onNavigateToSafeRoute,
                    modifier = Modifier.weight(1f)
                )
                QuickToolCard(
                    title = "Discreet",
                    desc = "Hide app",
                    icon = Icons.Default.VisibilityOff,
                    onClick = onNavigateToDiscreet,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Safety Check-In Shortcut (Section 24)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SheVaultTheme.colors.surface)
                    .clickable { onNavigateToCheckIn() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Safety Check-In",
                            style = SheVaultTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SheVaultTheme.colors.onSurface
                        )
                        Text(
                            text = "Let someone know you're expected to arrive safely.",
                            style = SheVaultTheme.typography.bodySmall,
                            color = SheVaultTheme.colors.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Start →",
                        style = SheVaultTheme.typography.labelLarge,
                        color = SheVaultTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Safety Activity Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent safety activity",
                    style = SheVaultTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SheVaultTheme.colors.onSurface
                )
                Text(
                    text = "View all",
                    style = SheVaultTheme.typography.labelSmall,
                    color = SheVaultTheme.colors.primary,
                    modifier = Modifier.clickable { onNavigateToHistory() }
                )
            }

            incidents.take(2).forEach { inc ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SheVaultTheme.colors.surface)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = inc.title, style = SheVaultTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = inc.status, style = SheVaultTheme.typography.labelSmall, color = SheVaultTheme.colors.onSurfaceVariant)
                        }
                        Text(text = inc.date, style = SheVaultTheme.typography.labelSmall, color = SheVaultTheme.colors.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun QuickToolCard(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SheVaultTheme.colors.surface)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SheVaultTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SheVaultTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = SheVaultTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )
            Text(
                text = desc,
                style = SheVaultTheme.typography.labelSmall,
                color = SheVaultTheme.colors.onSurfaceVariant
            )
        }
    }
}
