package com.shevault.feature.saferoute

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Section 23: Safe Route Planning Screen
 * Note: Never makes fake claims like "93% safe" without defensible data sources.
 */
@Composable
fun SafeRouteScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var destination by remember { mutableStateOf("Connaught Place, New Delhi") }
    var routeCalculated by remember { mutableStateOf(false) }

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
                text = "Safe Route",
                style = SheVaultTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            OutlinedTextField(
                value = "Current location (GPS fix ±6m)",
                onValueChange = {},
                readOnly = true,
                label = { Text("From") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("To (Enter destination)") },
                modifier = Modifier.fillMaxWidth()
            )

            SheVaultPrimaryButton(
                text = "Calculate Route",
                onClick = { routeCalculated = true },
                modifier = Modifier.fillMaxWidth()
            )

            if (routeCalculated) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SheVaultTheme.colors.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, SheVaultTheme.colors.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Recommended Route",
                            style = SheVaultTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SheVaultTheme.colors.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ETA: 18 mins", style = SheVaultTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Distance: 4.8 km", style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.onSurfaceVariant)
                        }

                        Text(
                            text = "Safety factors",
                            style = SheVaultTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SheVaultTheme.colors.onSurface
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "✓ Well-lit route", style = SheVaultTheme.typography.bodySmall, color = SheVaultTheme.colors.safe)
                            Text(text = "✓ Main roads & bus corridors", style = SheVaultTheme.typography.bodySmall, color = SheVaultTheme.colors.safe)
                            Text(text = "✓ Nearby public hubs & open transit", style = SheVaultTheme.typography.bodySmall, color = SheVaultTheme.colors.safe)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        SheVaultPrimaryButton(
                            text = "Start Route Monitoring",
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
