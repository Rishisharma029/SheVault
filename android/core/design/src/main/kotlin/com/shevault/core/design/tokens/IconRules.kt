package com.shevault.core.design.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SheVault Iconography Rules & Sizing Specifications.
 * Ensures consistent visual weight, accessibility minimum tap targets, and recognizable safety cues.
 */
@Immutable
data class SheVaultIconRules(
    // Strict Icon Sizing Ladder
    val micro: Dp = 12.dp,
    val small: Dp = 16.dp,
    val medium: Dp = 24.dp,
    val large: Dp = 32.dp,
    val extraLarge: Dp = 48.dp,
    val sosHero: Dp = 72.dp,
    val decoyLauncher: Dp = 64.dp,

    // Accessibility standard (WCAG 2.1 AA)
    val minTouchTarget: Dp = 48.dp,

    // Stroke weights
    val hairlineStroke: Dp = 1.dp,
    val regularStroke: Dp = 2.dp,
    val boldStroke: Dp = 2.5.dp,
    val emergencyStroke: Dp = 3.dp
) {
    val hairlineStrokeWidth: Dp get() = hairlineStroke
    val regularStrokeWidth: Dp get() = regularStroke
    val boldStrokeWidth: Dp get() = boldStroke
    val emergencyStrokeWidth: Dp get() = emergencyStroke
}

val DefaultSheVaultIconRules = SheVaultIconRules()
