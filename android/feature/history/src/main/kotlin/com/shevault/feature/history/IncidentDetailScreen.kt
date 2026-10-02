package com.shevault.feature.history

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.design.components.SheVaultSecondaryButton
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Section 26: Incident Detail Screen
 * Note: Never displays raw cryptographic hashes to ordinary users.
 */
@Composable
fun IncidentDetailScreen(
    incidentId: String,
    onNavigateBack: () -> Unit,
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
                text = "Incident #$incidentId",
                style = SheVaultTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SheVaultTheme.colors.surface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow("Date & Time", "Oct 2, 2026 (17:34 – 17:41 IST)")
                    DetailRow("Activation Method", "SOS Hold (1.4 sec)")
                    DetailRow("Resolution Status", "Completed / Safe Pin Cancelled")
                    DetailRow("Last Known Location", "Connaught Place, Delhi (±6m)")
                }
            }

            Text(
                text = "Delivery Audit",
                style = SheVaultTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SheVaultTheme.colors.surface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Backend Cloud Ingestion", style = SheVaultTheme.typography.bodyMedium)
                        Text(text = "✓ Acknowledged", style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.safe, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Trusted Circle SMS Broadcast", style = SheVaultTheme.typography.bodyMedium)
                        Text(text = "✓ Notifications sent", style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.safe, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SheVaultSecondaryButton(
                text = "Back to History",
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = SheVaultTheme.typography.bodySmall, color = SheVaultTheme.colors.onSurfaceVariant)
        Text(text = value, style = SheVaultTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SheVaultTheme.colors.onSurface)
    }
}
