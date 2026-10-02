package com.shevault.feature.incident

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.components.SheVaultSecondaryButton
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Sections 16, 17, 18: Dedicated Full-Screen Active Incident Screen
 * Note: Per section 3, this screen strictly omits the normal bottom navigation.
 */
@Composable
fun IncidentScreen(
    onNavigateToCancel: () -> Unit,
    onContinueProtection: () -> Unit,
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
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SheVaultTheme.colors.emergencyContainer, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SheVaultTheme.colors.emergency,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🚨 SAFETY SESSION ACTIVE",
                            style = SheVaultTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SheVaultTheme.colors.emergency
                        )
                        Text(
                            text = "Started 17:34 • Session #SV-9F82",
                            style = SheVaultTheme.typography.labelSmall,
                            color = SheVaultTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 17: Active Incident Map Deck
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SheVaultTheme.colors.surface)
                    .border(1.dp, SheVaultTheme.colors.outlineVariant, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(SheVaultTheme.colors.emergency)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "● current location",
                        style = SheVaultTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SheVaultTheme.colors.emergency
                    )
                    Text(
                        text = "Location updated 4 sec ago • Accuracy ±8m",
                        style = SheVaultTheme.typography.labelSmall,
                        color = SheVaultTheme.colors.onSurfaceVariant
                    )
                }
            }

            // Telemetry Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TelemetryBox(label = "Battery", value = "34%", modifier = Modifier.weight(1f))
                TelemetryBox(label = "Network", value = "Connected", modifier = Modifier.weight(1f))
                TelemetryBox(label = "Circle", value = "2 notified", modifier = Modifier.weight(1f))
            }

            // Section 18: Incident Timeline
            Text(
                text = "Incident Timeline",
                style = SheVaultTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SheVaultTheme.colors.surface, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimelineRow("17:34:02", "SOS activated", Icons.Default.Warning)
                TimelineRow("17:34:03", "Incident session started", Icons.Default.Security)
                TimelineRow("17:34:04", "Location captured (±8m)", Icons.Default.LocationOn)
                TimelineRow("17:34:06", "Trusted contact notified", Icons.Default.NotificationsActive)
                TimelineRow("17:34:12", "Location updated", Icons.Default.LocationOn)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            SheVaultSecondaryButton(
                text = "Continue Protection",
                onClick = onContinueProtection,
                modifier = Modifier.fillMaxWidth()
            )

            SheVaultPrimaryButton(
                text = "End Session",
                onClick = onNavigateToCancel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TelemetryBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(SheVaultTheme.colors.surface, RoundedCornerShape(8.dp))
            .border(1.dp, SheVaultTheme.colors.outlineVariant, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, style = SheVaultTheme.typography.labelSmall, color = SheVaultTheme.colors.onSurfaceVariant)
            Text(text = value, style = SheVaultTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SheVaultTheme.colors.onSurface)
        }
    }
}

@Composable
private fun TimelineRow(time: String, desc: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = SheVaultTheme.colors.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = time, style = SheVaultTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SheVaultTheme.colors.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = desc, style = SheVaultTheme.typography.bodySmall, color = SheVaultTheme.colors.onSurface)
    }
}
