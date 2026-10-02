package com.shevault.core.design.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Immutable

/**
 * SheVault Motion & Animation Tokens.
 * Optimized for snappy feedback under normal usage and high-urgency pulsating feedback during SOS.
 */
@Immutable
data class SheVaultMotion(
    val durationInstantMs: Int = 100,
    val durationFastMs: Int = 150,
    val durationMediumMs: Int = 300,
    val durationSlowMs: Int = 500,
    val durationSosPulseMs: Int = 1000,
    val durationSosCountdownMs: Int = 1000,
    val durationBeaconFlashMs: Int = 750,

    val standardEasing: Easing = FastOutSlowInEasing,
    val emphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
    val enterEasing: Easing = LinearOutSlowInEasing,
    val linearEasing: Easing = LinearEasing
)

val DefaultSheVaultMotion = SheVaultMotion()
