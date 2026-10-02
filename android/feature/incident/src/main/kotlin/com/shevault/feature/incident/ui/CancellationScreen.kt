package com.shevault.feature.incident.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.design.components.SheVaultCard
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.components.SheVaultSecondaryButton
import com.shevault.core.design.theme.SheVaultTheme
import com.shevault.core.design.tokens.PaletteTokens
import com.shevault.core.design.tokens.SheVaultColors
import com.shevault.core.security.CredentialVerificationResult
import com.shevault.core.security.DuressCredentialManager
import kotlinx.coroutines.delay

/**
 * Phase of the Cancellation flow.
 * Never allows one-tap cancellation.
 */
enum class CancellationFlowPhase {
    PROMPT,          // "Safety session active. Are you safe?"
    AUTHENTICATION,  // Biometric / PIN entry
    CONFIRMED_SAFE   // "✓ You're Safe" - Visually identical for both Safe and Duress PIN
}

/**
 * Cancellation Screen & Modal: Multi-step authenticated safety disarm interface.
 *
 * Requirements:
 * 1. Never provide Cancel as a one-tap permanent action.
 * 2. Step 1: Prompt [ Continue Protection ] and [ I'm Safe ].
 * 3. Step 2: Authentication with Biometric and PIN.
 * 4. Step 3: Visually IDENTICAL UI for both Safe credential and Duress credential.
 *    Client NEVER exposes "DURESS DETECTED" to protect victim from aggressor.
 */
@Composable
fun CancellationScreen(
    credentialManager: DuressCredentialManager,
    onContinueProtection: () -> Unit,
    onSafeCancelled: () -> Unit,
    onDuressCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(CancellationFlowPhase.PROMPT) }
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDuressResult by remember { mutableStateOf(false) }

    when (phase) {
        CancellationFlowPhase.PROMPT -> {
            CancellationPromptView(
                onContinueProtection = onContinueProtection,
                onProceedToAuth = {
                    phase = CancellationFlowPhase.AUTHENTICATION
                    errorMessage = null
                    enteredPin = ""
                },
                modifier = modifier
            )
        }

        CancellationFlowPhase.AUTHENTICATION -> {
            CancellationAuthenticationView(
                enteredPin = enteredPin,
                errorMessage = errorMessage,
                onPinDigitAdded = { digit ->
                    if (enteredPin.length < 4) {
                        val next = enteredPin + digit
                        enteredPin = next
                        errorMessage = null
                        if (next.length == 4) {
                            when (val result = credentialManager.verifyPin(next)) {
                                is CredentialVerificationResult.Safe -> {
                                    isDuressResult = false
                                    phase = CancellationFlowPhase.CONFIRMED_SAFE
                                }
                                is CredentialVerificationResult.Duress -> {
                                    // Hidden duress mechanism: UI enters IDENTICAL CONFIRMED_SAFE state
                                    isDuressResult = true
                                    phase = CancellationFlowPhase.CONFIRMED_SAFE
                                }
                                is CredentialVerificationResult.Invalid -> {
                                    enteredPin = ""
                                    errorMessage = if (result.isLockedOut) {
                                        "Too many failed attempts. Device temporarily locked."
                                    } else {
                                        "Incorrect PIN (${result.remainingAttempts} attempts remaining)"
                                    }
                                }
                            }
                        }
                    }
                },
                onBackspace = {
                    if (enteredPin.isNotEmpty()) {
                        enteredPin = enteredPin.dropLast(1)
                        errorMessage = null
                    }
                },
                onBiometricAuthenticate = {
                    // Biometric disarm maps to Safe credential
                    isDuressResult = false
                    phase = CancellationFlowPhase.CONFIRMED_SAFE
                },
                onCancelToProtection = onContinueProtection,
                modifier = modifier
            )
        }

        CancellationFlowPhase.CONFIRMED_SAFE -> {
            // Strictly identical outcome view for both Safe and Duress
            CancellationSafeOutcomeView(
                onFinish = {
                    if (isDuressResult) {
                        onDuressCancelled()
                    } else {
                        onSafeCancelled()
                    }
                },
                modifier = modifier
            )
        }
    }
}

/**
 * Step 1: Deliberate confirmation dialog.
 * Never allows one-tap cancellation.
 */
@Composable
fun CancellationPromptView(
    onContinueProtection: () -> Unit,
    onProceedToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
            .padding(SheVaultTheme.spacing.screenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = SheVaultTheme.spacing.xl)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SheVaultColors.emergency.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SheVaultColors.emergency,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(SheVaultTheme.spacing.lg))

            Text(
                text = "Safety session active",
                style = SheVaultTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = SheVaultTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(SheVaultTheme.spacing.sm))

            Text(
                text = "Are you safe?",
                style = SheVaultTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SheVaultTheme.colors.primary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(SheVaultTheme.spacing.md))

            SheVaultCard {
                Text(
                    text = "Emergency protection and evidence telemetry are currently recording. " +
                            "To disarm and stand down emergency services, authentication is required.",
                    style = SheVaultTheme.typography.bodyMedium,
                    color = SheVaultTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Two explicit action buttons: Continue Protection or Proceed to Auth
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SheVaultTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SheVaultTheme.spacing.md)
        ) {
            // Dominant action: Stay protected!
            SheVaultPrimaryButton(
                text = "Continue Protection",
                onClick = onContinueProtection,
                modifier = Modifier.fillMaxWidth()
            )

            // Secondary action: Request disarming
            SheVaultSecondaryButton(
                text = "I'm Safe",
                onClick = onProceedToAuth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Step 2: Authentication screen with Biometrics and Numeric PIN keypad.
 */
@Composable
fun CancellationAuthenticationView(
    enteredPin: String,
    errorMessage: String?,
    onPinDigitAdded: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometricAuthenticate: () -> Unit,
    onCancelToProtection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
            .padding(SheVaultTheme.spacing.screenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = SheVaultTheme.spacing.lg)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = SheVaultTheme.colors.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(SheVaultTheme.spacing.sm))

            Text(
                text = "Enter Security PIN",
                style = SheVaultTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = SheVaultTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(SheVaultTheme.spacing.xs))

            Text(
                text = "Authenticate to disarm active safety session",
                style = SheVaultTheme.typography.bodySmall,
                color = SheVaultTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SheVaultTheme.spacing.lg))

            // PIN Dots Indicator (4 dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) SheVaultTheme.colors.primary
                                else SheVaultTheme.colors.outlineVariant
                            )
                    )
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = SheVaultTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = SheVaultColors.emergency,
                    modifier = Modifier.padding(top = SheVaultTheme.spacing.sm),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Numeric Keypad (Discreet, secure keypad)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        when (key) {
                            "BIO" -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SheVaultTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { onBiometricAuthenticate() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biometric Disarm",
                                        tint = SheVaultTheme.colors.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            "DEL" -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SheVaultTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { onBackspace() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        tint = SheVaultTheme.colors.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SheVaultTheme.colors.surfaceVariant)
                                        .clickable { onPinDigitAdded(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = SheVaultTheme.typography.titleLarge.copy(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = SheVaultTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SheVaultTheme.spacing.xs))

            SheVaultSecondaryButton(
                text = "Back to Protection",
                onClick = onCancelToProtection,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Step 3: Resolution outcome view.
 *
 * CRITICAL SAFETY REQUIREMENT:
 * Visually IDENTICAL UI outcome for BOTH Safe and Duress PIN!
 * Safe: User marked safe, Session terminated.
 * Duress: UI: "✓ You're Safe", Session terminated appearance.
 * The client does NOT expose "DURESS DETECTED" anywhere visible to an aggressor.
 */
@Composable
fun CancellationSafeOutcomeView(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-complete navigation after 1.5 seconds or on tap
    LaunchedEffect(Unit) {
        delay(1500L)
        onFinish()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
            .padding(SheVaultTheme.spacing.screenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(SheVaultTheme.colors.safeContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SheVaultTheme.colors.safe,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(SheVaultTheme.spacing.lg))

        Text(
            text = "✓ You're Safe",
            style = SheVaultTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = SheVaultTheme.colors.safe,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(SheVaultTheme.spacing.sm))

        Text(
            text = "User marked safe",
            style = SheVaultTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = SheVaultTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(SheVaultTheme.spacing.xs))

        Text(
            text = "Session terminated",
            style = SheVaultTheme.typography.bodyMedium,
            color = SheVaultTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
