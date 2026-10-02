package com.shevault.feature.history

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.common.mock.MockIncident
import com.shevault.core.common.mock.MockRepositories
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Section 25: History Screen grouped by Month with status indicators
 */
@Composable
fun HistoryScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Safety History",
                style = SheVaultTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            // Group: October
            Text(
                text = "October 2026",
                style = SheVaultTheme.typography.labelLarge,
                color = SheVaultTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )

            incidents.filter { it.date.contains("Oct") }.forEach { item ->
                HistoryCardItem(item = item, onClick = { onNavigateToDetail(item.id) })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Group: September
            Text(
                text = "September 2026",
                style = SheVaultTheme.typography.labelLarge,
                color = SheVaultTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )

            incidents.filter { it.date.contains("Sep") }.forEach { item ->
                HistoryCardItem(item = item, onClick = { onNavigateToDetail(item.id) })
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun HistoryCardItem(
    item: MockIncident,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SheVaultTheme.colors.surface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = item.title, style = SheVaultTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${item.date} • ${item.status}", style = SheVaultTheme.typography.bodySmall, color = SheVaultTheme.colors.onSurfaceVariant)
            }
            Text(
                text = "View →",
                style = SheVaultTheme.typography.labelMedium,
                color = SheVaultTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
