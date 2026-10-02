package com.shevault.core.design.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.design.theme.SheVaultTheme
import com.shevault.core.design.tokens.SheVaultColors

/**
 * Standard Primary Action Button for SheVault.
 */
@Composable
fun SheVaultPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(
                minHeight = SheVaultTheme.iconRules.minTouchTarget
            ),
        enabled = enabled,
        shape = SheVaultTheme.radius.shapeMd,
        colors = ButtonDefaults.buttonColors(
            containerColor = SheVaultTheme.colors.primary,
            contentColor = SheVaultTheme.colors.onPrimary
        ),
        contentPadding = PaddingValues(
            horizontal = SheVaultTheme.spacing.buttonPaddingHorizontal,
            vertical = SheVaultTheme.spacing.buttonPaddingVertical
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(SheVaultTheme.spacing.sm))
            }
            Text(
                text = text,
                style = SheVaultTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Secondary Outlined Button.
 */
@Composable
fun SheVaultSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = SheVaultTheme.iconRules.minTouchTarget),
        enabled = enabled,
        shape = SheVaultTheme.radius.shapeMd,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SheVaultTheme.colors.onSurface
        ),
        contentPadding = PaddingValues(
            horizontal = SheVaultTheme.spacing.buttonPaddingHorizontal,
            vertical = SheVaultTheme.spacing.buttonPaddingVertical
        )
    ) {
        Text(
            text = text,
            style = SheVaultTheme.typography.labelLarge
        )
    }
}

/**
 * High-Visibility Pulsating Emergency SOS Button.
 *
 * NOTE: Uses `SheVaultColors.emergency` and `SheVaultTheme.colors.emergency` exclusively!
 */
@Composable
fun SheVaultSosEmergencyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = SheVaultTheme.motion.durationSosPulseMs,
                easing = SheVaultTheme.motion.standardEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_pulse_scale"
    )

    val emergencyColor = SheVaultColors.emergency

    Box(
        modifier = modifier.size(size + 40.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        if (isPulsing) {
            Box(
                modifier = Modifier
                    .size(size * pulseScale)
                    .clip(SheVaultTheme.radius.shapeCircle)
                    .background(emergencyColor.copy(alpha = 0.22f))
            )
        }

        // Inner glowing border
        Box(
            modifier = Modifier
                .size(size + 12.dp)
                .clip(SheVaultTheme.radius.shapeCircle)
                .border(
                    width = SheVaultTheme.iconRules.boldStrokeWidth,
                    color = emergencyColor.copy(alpha = 0.5f),
                    shape = SheVaultTheme.radius.shapeCircle
                )
        )

        // Main SOS Button Target
        Box(
            modifier = Modifier
                .size(size)
                .clip(SheVaultTheme.radius.shapeCircle)
                .background(emergencyColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SOS",
                style = SheVaultTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            )
        }
    }
}
