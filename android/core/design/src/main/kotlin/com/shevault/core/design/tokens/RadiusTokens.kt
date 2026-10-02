package com.shevault.core.design.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SheVault Radius Tokens and Corner Shapes.
 */
@Immutable
data class SheVaultRadius(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val pill: Dp = 50.dp,
    val circle: Dp = 9999.dp
) {
    val shapeNone: Shape get() = RoundedCornerShape(none)
    val shapeXs: Shape get() = RoundedCornerShape(xs)
    val shapeSm: Shape get() = RoundedCornerShape(sm)
    val shapeMd: Shape get() = RoundedCornerShape(md)
    val shapeLg: Shape get() = RoundedCornerShape(lg)
    val shapeXl: Shape get() = RoundedCornerShape(xl)
    val shapeXxl: Shape get() = RoundedCornerShape(xxl)
    val shapePill: Shape get() = RoundedCornerShape(pill)
    val shapeCircle: Shape get() = CircleShape
}

val DefaultSheVaultRadius = SheVaultRadius()
