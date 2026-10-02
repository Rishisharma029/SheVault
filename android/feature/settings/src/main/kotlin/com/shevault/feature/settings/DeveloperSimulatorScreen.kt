package com.shevault.feature.settings

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.common.state.ConnectivityState
import com.shevault.core.common.state.GlobalIncidentState
import com.shevault.core.common.state.GlobalStateManager
import com.shevault.core.common.state.MovementState
import com.shevault.core.common.state.ProtectionState
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.components.SheVaultSecondaryButton
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Section 45: Developer State Simulator Screen
 * Enables inspecting every frontend state without repeatedly reproducing real-world edge cases.
 */
@Composable
fun DeveloperSimulatorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by GlobalStateManager.appState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Developer State Simulator",
                style = SheVaultTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            // Current State Monitor Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SheVaultTheme.colors.surface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "CURRENT STATE", style = SheVaultTheme.typography.labelSmall, color = SheVaultTheme.colors.onSurfaceVariant)
                    Text(text = "Protection: ${state.protectionState}", style = SheVaultTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(text = "Incident: ${state.incidentState}", style = SheVaultTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(text = "Network: ${state.connectivityState}", style = SheVaultTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(text = "Battery: ${state.batteryPercent}% (${if (state.isBatteryCharging) "Charging" else "Discharging"})", style = SheVaultTheme.typography.bodySmall)
                    Text(text = "Movement: ${state.movementState}", style = SheVaultTheme.typography.bodySmall)
                }
            }

            // Protection State Buttons
            SimulatorSection("Protection States") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheVaultSecondaryButton(text = "Protected", onClick = { GlobalStateManager.updateState { it.copy(protectionState = ProtectionState.PROTECTED) } }, modifier = Modifier.weight(1f))
                    SheVaultSecondaryButton(text = "Limited", onClick = { GlobalStateManager.updateState { it.copy(protectionState = ProtectionState.LIMITED) } }, modifier = Modifier.weight(1f))
                    SheVaultSecondaryButton(text = "Unavailable", onClick = { GlobalStateManager.updateState { it.copy(protectionState = ProtectionState.UNAVAILABLE) } }, modifier = Modifier.weight(1f))
                }
            }

            // Incident State Transitions
            SimulatorSection("Incident Lifecycle") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheVaultSecondaryButton(text = "Trigger SOS", onClick = { GlobalStateManager.triggerSos() }, modifier = Modifier.weight(1f))
                    SheVaultSecondaryButton(text = "Escalate", onClick = { GlobalStateManager.updateState { it.copy(incidentState = GlobalIncidentState.ESCALATED) } }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheVaultSecondaryButton(text = "Safe Cancel", onClick = { GlobalStateManager.safeCancel() }, modifier = Modifier.weight(1f))
                    SheVaultSecondaryButton(text = "Duress Cancel", onClick = { GlobalStateManager.duressCancel() }, modifier = Modifier.weight(1f))
                    SheVaultSecondaryButton(text = "Reset Idle", onClick = { GlobalStateManager.resetToIdle() }, modifier = Modifier.weight(1f))
                }
            }

            // Connectivity & Battery
            SimulatorSection("Environmental & Telemetry Factors") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheVaultSecondaryButton(
                        text = if (state.connectivityState == ConnectivityState.ONLINE) "Go Offline" else "Go Online",
                        onClick = {
                            GlobalStateManager.updateState {
                                it.copy(connectivityState = if (it.connectivityState == ConnectivityState.ONLINE) ConnectivityState.OFFLINE else ConnectivityState.ONLINE)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SheVaultSecondaryButton(
                        text = if (state.batteryPercent > 20) "Low Battery (8%)" else "Normal Battery (88%)",
                        onClick = {
                            GlobalStateManager.updateState {
                                it.copy(batteryPercent = if (it.batteryPercent > 20) 8 else 88)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheVaultSecondaryButton(text = "Walking", onClick = { GlobalStateManager.updateState { it.copy(movementState = MovementState.WALKING) } }, modifier = Modifier.weight(1f))
                    SheVaultSecondaryButton(text = "Running", onClick = { GlobalStateManager.updateState { it.copy(movementState = MovementState.RUNNING) } }, modifier = Modifier.weight(1f))
                    SheVaultSecondaryButton(text = "Vehicle", onClick = { GlobalStateManager.updateState { it.copy(movementState = MovementState.VEHICLE) } }, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SheVaultPrimaryButton(
                text = "Back to Settings",
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SimulatorSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = SheVaultTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SheVaultTheme.colors.onSurface)
        content()
    }
}
