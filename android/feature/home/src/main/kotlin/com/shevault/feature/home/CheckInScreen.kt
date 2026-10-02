package com.shevault.feature.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.shevault.core.common.state.GlobalStateManager
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.components.SheVaultSecondaryButton
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Section 24: Safety Check-In Screen (Setup, Active, and Overdue states)
 */
@Composable
fun CheckInScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isStarted by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("Home (Metro Station)") }
    var expectedTime by remember { mutableStateOf("18:30 IST") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Safety Check-In",
                style = SheVaultTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            if (!isStarted) {
                Text(
                    text = "Let someone know you're expected to arrive safely. If you don't check in before the timer expires, an alert is prepared.",
                    style = SheVaultTheme.typography.bodyMedium,
                    color = SheVaultTheme.colors.onSurfaceVariant
                )

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = expectedTime,
                    onValueChange = { expectedTime = it },
                    label = { Text("Expected arrival time") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = "Mom (+91 98765 00001)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Designated Guardian") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SheVaultPrimaryButton(
                    text = "Start Check-In Timer",
                    onClick = {
                        isStarted = true
                        GlobalStateManager.updateState { it.copy(isCheckInActive = true, activeCheckInDestination = destination) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Active Check-in State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SheVaultTheme.colors.surface, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CHECK-IN ACTIVE",
                            style = SheVaultTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SheVaultTheme.colors.primary
                        )
                        Text(
                            text = "Destination: $destination",
                            style = SheVaultTheme.typography.bodyMedium,
                            color = SheVaultTheme.colors.onSurface
                        )
                        Text(
                            text = "Expected by $expectedTime",
                            style = SheVaultTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SheVaultTheme.colors.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SheVaultPrimaryButton(
                            text = "I'm Here / Arrived Safely",
                            onClick = {
                                isStarted = false
                                GlobalStateManager.updateState { it.copy(isCheckInActive = false, activeCheckInDestination = null) }
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
