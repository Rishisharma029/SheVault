package com.shevault.core.design.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Global safety states representing user protection level.
 */
enum class SafetyState {
    SAFE,
    ACTIVE_MONITORING,
    CAUTION_ALERT,
    EMERGENCY_SOS,
    DISCRETE_DECOY,
    OFFLINE_STANDALONE
}

/**
 * Status tokens defining visual styles for active application safety states.
 */
@Immutable
data class StatusColorStyle(
    val background: Color,
    val content: Color,
    val border: Color,
    val glow: Color,
    val labelText: String
)

@Immutable
data class SheVaultStatusTokens(
    val safe: StatusColorStyle,
    val activeMonitoring: StatusColorStyle,
    val cautionAlert: StatusColorStyle,
    val emergencySos: StatusColorStyle,
    val discreteDecoy: StatusColorStyle,
    val offlineStandalone: StatusColorStyle
) {
    fun forState(state: SafetyState): StatusColorStyle = forSafetyState(state)

    fun forSafetyState(state: SafetyState): StatusColorStyle {
        return when (state) {
            SafetyState.SAFE -> safe
            SafetyState.ACTIVE_MONITORING -> activeMonitoring
            SafetyState.CAUTION_ALERT -> cautionAlert
            SafetyState.EMERGENCY_SOS -> emergencySos
            SafetyState.DISCRETE_DECOY -> discreteDecoy
            SafetyState.OFFLINE_STANDALONE -> offlineStandalone
        }
    }
}

val LightSheVaultStatusTokens = SheVaultStatusTokens(
    safe = StatusColorStyle(
        background = PaletteTokens.SafeBg,
        content = PaletteTokens.SafeTeal,
        border = PaletteTokens.SafeTeal.copy(alpha = 0.3f),
        glow = PaletteTokens.SafeTeal.copy(alpha = 0.25f),
        labelText = "Protected & Safe"
    ),
    activeMonitoring = StatusColorStyle(
        background = PaletteTokens.PlumPrimaryTint,
        content = PaletteTokens.PlumPrimary,
        border = PaletteTokens.PlumPrimary.copy(alpha = 0.3f),
        glow = PaletteTokens.PlumPrimary.copy(alpha = 0.3f),
        labelText = "Live SafeRoute Tracking"
    ),
    cautionAlert = StatusColorStyle(
        background = PaletteTokens.WarningBg,
        content = PaletteTokens.WarningAmber,
        border = PaletteTokens.WarningAmber.copy(alpha = 0.4f),
        glow = PaletteTokens.WarningAmber.copy(alpha = 0.35f),
        labelText = "Check-in Expiring"
    ),
    // Emergency SOS uses SheVaultColors.emergency
    emergencySos = StatusColorStyle(
        background = PaletteTokens.EmergencyBg,
        content = SheVaultColors.emergency,
        border = PaletteTokens.EmergencyDark,
        glow = SheVaultColors.emergency.copy(alpha = 0.45f),
        labelText = "SOS Dispatched"
    ),
    discreteDecoy = StatusColorStyle(
        background = PaletteTokens.Slate100,
        content = PaletteTokens.Slate700,
        border = PaletteTokens.Slate300,
        glow = Color.Transparent,
        labelText = "Decoy Active"
    ),
    offlineStandalone = StatusColorStyle(
        background = PaletteTokens.Slate100,
        content = PaletteTokens.Slate600,
        border = PaletteTokens.Slate300,
        glow = PaletteTokens.Slate400.copy(alpha = 0.2f),
        labelText = "Offline (SMS Mode)"
    )
)

val DarkSheVaultStatusTokens = SheVaultStatusTokens(
    safe = StatusColorStyle(
        background = Color(0xFF042F2E),
        content = PaletteTokens.SafeTeal,
        border = PaletteTokens.SafeTeal,
        glow = PaletteTokens.SafeTeal.copy(alpha = 0.3f),
        labelText = "Protected & Safe"
    ),
    activeMonitoring = StatusColorStyle(
        background = PaletteTokens.PlumPrimaryDark,
        content = PaletteTokens.DarkFocus,
        border = PaletteTokens.DarkFocus,
        glow = PaletteTokens.DarkFocus.copy(alpha = 0.35f),
        labelText = "Live SafeRoute Tracking"
    ),
    cautionAlert = StatusColorStyle(
        background = Color(0xFF451A03),
        content = PaletteTokens.WarningAmber,
        border = PaletteTokens.WarningAmber,
        glow = PaletteTokens.WarningAmber.copy(alpha = 0.4f),
        labelText = "Check-in Expiring"
    ),
    // Emergency SOS in dark mode
    emergencySos = StatusColorStyle(
        background = PaletteTokens.EmergencyDark,
        content = PaletteTokens.White,
        border = SheVaultColors.emergency,
        glow = SheVaultColors.emergency.copy(alpha = 0.6f),
        labelText = "SOS Dispatched"
    ),
    discreteDecoy = StatusColorStyle(
        background = PaletteTokens.DarkSurface,
        content = PaletteTokens.Slate400,
        border = PaletteTokens.DarkBorder,
        glow = Color.Transparent,
        labelText = "Decoy Active"
    ),
    offlineStandalone = StatusColorStyle(
        background = PaletteTokens.DarkSurface,
        content = PaletteTokens.DarkSecondary,
        border = PaletteTokens.DarkBorder,
        glow = PaletteTokens.Slate400.copy(alpha = 0.2f),
        labelText = "Offline (SMS Mode)"
    )
)
