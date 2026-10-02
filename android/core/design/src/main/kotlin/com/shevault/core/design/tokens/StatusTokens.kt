package com.shevault.core.design.tokens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * High-Priority Primary Safety Statuses as defined by SheVault Master Palette.
 *
 * CRITICAL INVARIANT:
 * Never communicate safety state with color alone.
 * Every status is intrinsically bound to: (Color + Icon + Text).
 */
enum class SheVaultSafetyStatus {
    PROTECTED,
    LIMITED,
    ACTIVE,
    DISCREET,
    OFFLINE
}

/**
 * Immutable descriptor binding Color + Icon + Text for safety states.
 */
@Immutable
data class SafetyStatusDescriptor(
    val status: SheVaultSafetyStatus,
    val title: String,
    val description: String,
    val contentColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val icon: ImageVector
)

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
    val labelText: String,
    val icon: ImageVector = Icons.Default.CheckCircle
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

    /**
     * Resolves the complete Color + Icon + Text descriptor for a primary safety status.
     */
    fun descriptorFor(status: SheVaultSafetyStatus): SafetyStatusDescriptor {
        return when (status) {
            SheVaultSafetyStatus.PROTECTED -> SafetyStatusDescriptor(
                status = SheVaultSafetyStatus.PROTECTED,
                title = "You're Protected",
                description = "Continuous mesh monitoring & location fix active",
                contentColor = safe.content,
                containerColor = safe.background,
                borderColor = safe.border,
                icon = safe.icon
            )
            SheVaultSafetyStatus.LIMITED -> SafetyStatusDescriptor(
                status = SheVaultSafetyStatus.LIMITED,
                title = "Protection Partially Limited",
                description = "Critical permission or location accuracy constrained",
                contentColor = cautionAlert.content,
                containerColor = cautionAlert.background,
                borderColor = cautionAlert.border,
                icon = cautionAlert.icon
            )
            SheVaultSafetyStatus.ACTIVE -> SafetyStatusDescriptor(
                status = SheVaultSafetyStatus.ACTIVE,
                title = "Emergency Session Active",
                description = "SOS dispatched • Streaming encrypted location & telemetry",
                contentColor = emergencySos.content,
                containerColor = emergencySos.background,
                borderColor = emergencySos.border,
                icon = emergencySos.icon
            )
            SheVaultSafetyStatus.DISCREET -> SafetyStatusDescriptor(
                status = SheVaultSafetyStatus.DISCREET,
                title = "Discreet Mode Active",
                description = "Calculator camouflage running • Zero safety indicators exposed",
                contentColor = discreteDecoy.content,
                containerColor = discreteDecoy.background,
                borderColor = discreteDecoy.border,
                icon = discreteDecoy.icon
            )
            SheVaultSafetyStatus.OFFLINE -> SafetyStatusDescriptor(
                status = SheVaultSafetyStatus.OFFLINE,
                title = "Offline Mode",
                description = "Cellular/Cloud disconnected • Local SMS and Room journal operational",
                contentColor = offlineStandalone.content,
                containerColor = offlineStandalone.background,
                borderColor = offlineStandalone.border,
                icon = offlineStandalone.icon
            )
        }
    }
}

val LightSheVaultStatusTokens = SheVaultStatusTokens(
    safe = StatusColorStyle(
        background = SafeTokens.surface,
        content = SafeTokens.default,
        border = SafeTokens.default.copy(alpha = 0.3f),
        glow = SafeTokens.default.copy(alpha = 0.25f),
        labelText = "You're Protected",
        icon = Icons.Default.Shield
    ),
    activeMonitoring = StatusColorStyle(
        background = BrandTokens.primaryTint,
        content = BrandTokens.primary,
        border = BrandTokens.primary.copy(alpha = 0.3f),
        glow = BrandTokens.primary.copy(alpha = 0.3f),
        labelText = "Live SafeRoute Tracking",
        icon = Icons.Default.Shield
    ),
    cautionAlert = StatusColorStyle(
        background = WarningTokens.surface,
        content = WarningTokens.default,
        border = WarningTokens.default.copy(alpha = 0.4f),
        glow = WarningTokens.default.copy(alpha = 0.35f),
        labelText = "Protection Partially Limited",
        icon = Icons.Default.Warning
    ),
    // Emergency SOS uses SheVaultColors.emergency
    emergencySos = StatusColorStyle(
        background = EmergencyTokens.surface,
        content = SheVaultColors.emergency,
        border = EmergencyTokens.dark,
        glow = SheVaultColors.emergency.copy(alpha = 0.45f),
        labelText = "Emergency Session Active",
        icon = Icons.Default.Warning
    ),
    discreteDecoy = StatusColorStyle(
        background = BrandTokens.primaryTint,
        content = LightNeutralTokens.textSecondary,
        border = LightNeutralTokens.border,
        glow = Color.Transparent,
        labelText = "Discreet Mode Active",
        icon = Icons.Default.VisibilityOff
    ),
    offlineStandalone = StatusColorStyle(
        background = WarningTokens.surface,
        content = WarningTokens.default,
        border = WarningTokens.default.copy(alpha = 0.3f),
        glow = WarningTokens.default.copy(alpha = 0.2f),
        labelText = "Offline Mode",
        icon = Icons.Default.WifiOff
    )
)

val DarkSheVaultStatusTokens = SheVaultStatusTokens(
    safe = StatusColorStyle(
        background = SafeTokens.surfaceDark,
        content = SafeTokens.default,
        border = SafeTokens.default,
        glow = SafeTokens.default.copy(alpha = 0.3f),
        labelText = "You're Protected",
        icon = Icons.Default.Shield
    ),
    activeMonitoring = StatusColorStyle(
        background = BrandTokens.primaryDark,
        content = FocusRingTokens.dark,
        border = FocusRingTokens.dark,
        glow = FocusRingTokens.dark.copy(alpha = 0.35f),
        labelText = "Live SafeRoute Tracking",
        icon = Icons.Default.Shield
    ),
    cautionAlert = StatusColorStyle(
        background = WarningTokens.surfaceDark,
        content = WarningTokens.default,
        border = WarningTokens.default,
        glow = WarningTokens.default.copy(alpha = 0.4f),
        labelText = "Protection Partially Limited",
        icon = Icons.Default.Warning
    ),
    // Emergency SOS in dark mode
    emergencySos = StatusColorStyle(
        background = EmergencyTokens.surfaceDark,
        content = EmergencyTokens.surface,
        border = SheVaultColors.emergency,
        glow = SheVaultColors.emergency.copy(alpha = 0.6f),
        labelText = "Emergency Session Active",
        icon = Icons.Default.Warning
    ),
    discreteDecoy = StatusColorStyle(
        background = DarkNeutralTokens.surface,
        content = DarkNeutralTokens.textSecondary,
        border = DarkNeutralTokens.border,
        glow = Color.Transparent,
        labelText = "Discreet Mode Active",
        icon = Icons.Default.VisibilityOff
    ),
    offlineStandalone = StatusColorStyle(
        background = WarningTokens.surfaceDark,
        content = WarningTokens.default,
        border = WarningTokens.default.copy(alpha = 0.5f),
        glow = WarningTokens.default.copy(alpha = 0.2f),
        labelText = "Offline Mode",
        icon = Icons.Default.WifiOff
    )
)
