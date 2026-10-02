package com.shevault.core.design.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SheVault Spacing Tokens (4dp baseline grid).
 */
@Immutable
data class SheVaultSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    val huge: Dp = 64.dp,

    // Component-specific layout spacing
    val screenHorizontalPadding: Dp = 16.dp,
    val cardContentPadding: Dp = 16.dp,
    val dialogPadding: Dp = 24.dp,
    val buttonPaddingVertical: Dp = 12.dp,
    val buttonPaddingHorizontal: Dp = 24.dp,
    val sosPulseRadiusMax: Dp = 140.dp
)

val DefaultSheVaultSpacing = SheVaultSpacing()
