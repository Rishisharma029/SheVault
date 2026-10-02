package com.shevault.feature.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.common.state.GlobalStateManager
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.theme.SheVaultTheme
import kotlinx.coroutines.delay

/**
 * Section 11: Internal Session Screen (Safety Session Active Grace Window)
 */
@Composable
fun SosScreen(
    onCancelSos: () -> Unit,
    onSosConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var graceSecondsRemaining by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        while (graceSecondsRemaining > 0) {
            delay(1000L)
            graceSecondsRemaining--
        }
        onSosConfirmed()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status Header
            Text(
                text = "● ACTIVE",
                style = SheVaultTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = SheVaultTheme.colors.emergency,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Central Session Card (Section 11)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SheVaultTheme.colors.surface, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SAFETY SESSION ACTIVE",
                        style = SheVaultTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = SheVaultTheme.colors.onSurface
                    )

                    Text(
                        text = "! Session Started",
                        style = SheVaultTheme.typography.titleMedium,
                        color = SheVaultTheme.colors.emergency,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Escalating in 00:0$graceSecondsRemaining",
                        style = SheVaultTheme.typography.headlineSmall,
                        color = SheVaultTheme.colors.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "✓ Session recorded",
                            style = SheVaultTheme.typography.bodyMedium,
                            color = SheVaultTheme.colors.safe
                        )
                        Text(
                            text = "✓ Protection active",
                            style = SheVaultTheme.typography.bodyMedium,
                            color = SheVaultTheme.colors.safe
                        )
                    }
                }
            }

            // Bottom Action
            SheVaultPrimaryButton(
                text = "I'M SAFE",
                onClick = onCancelSos,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}
