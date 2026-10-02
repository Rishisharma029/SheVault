package com.shevault.feature.incident.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.common.state.GlobalStateManager
import com.shevault.core.design.components.PINIndicator
import com.shevault.core.design.components.PINPad
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.components.SheVaultSecondaryButton
import com.shevault.core.design.theme.SheVaultTheme
import kotlinx.coroutines.delay

/**
 * Sections 12, 13, 14, 15: Cancellation & Covert Duress Keypad Screen
 */
@Composable
fun CancellationFlowScreen(
    onDismissToHome: () -> Unit,
    onContinueProtection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableStateOf<CancelStage>(CancelStage.Prompt) }
    var pinInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
            .padding(24.dp)
    ) {
        when (stage) {
            CancelStage.Prompt -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Are you safe?",
                        style = SheVaultTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SheVaultTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Authentication required to disarm safety session",
                        style = SheVaultTheme.typography.bodyMedium,
                        color = SheVaultTheme.colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    SheVaultPrimaryButton(
                        text = "I'm Safe (Enter PIN)",
                        onClick = { stage = CancelStage.PinKeypad },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SheVaultSecondaryButton(
                        text = "Continue Protection",
                        onClick = onContinueProtection,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            CancelStage.PinKeypad -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 32.dp)
                    ) {
                        Text(
                            text = "Enter PIN",
                            style = SheVaultTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SheVaultTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PINIndicator(length = 4, enteredCount = pinInput.length)
                    }

                    PINPad(
                        onDigitClick = { digit ->
                            if (pinInput.length < 4) {
                                pinInput += digit
                                if (pinInput.length == 4) {
                                    if (pinInput == "9999") {
                                        // Sections 11 & 14: Duress credential entered
                                        GlobalStateManager.duressCancel()
                                        stage = CancelStage.CancelledOutcome
                                    } else {
                                        // Section 15: Safe credential entered
                                        GlobalStateManager.safeCancel()
                                        stage = CancelStage.CancelledOutcome
                                    }
                                }
                            }
                        },
                        onBackspaceClick = {
                            if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            CancelStage.CancelledOutcome -> {
                // Section 14 & 15: Strictly visually identical outcome for both Safe and Duress PINs
                LaunchedEffect(Unit) {
                    delay(2000L)
                    onDismissToHome()
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(SheVaultTheme.colors.safeContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SheVaultTheme.colors.safe,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "✓ You're Safe",
                        style = SheVaultTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SheVaultTheme.colors.safe
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Safety session ended.",
                        style = SheVaultTheme.typography.bodyLarge,
                        color = SheVaultTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Returning home...",
                        style = SheVaultTheme.typography.bodySmall,
                        color = SheVaultTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private sealed class CancelStage {
    data object Prompt : CancelStage()
    data object PinKeypad : CancelStage()
    data object CancelledOutcome : CancelStage()
}
