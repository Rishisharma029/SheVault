package com.shevault.core.design.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SheVault Elevation Tokens for visual hierarchy and depth layers.
 */
@Immutable
data class SheVaultElevation(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp,
    val emergencySos: Dp = 16.dp,
    val bottomSheet: Dp = 16.dp,
    val dialog: Dp = 24.dp
)

val DefaultSheVaultElevation = SheVaultElevation()
