package com.shevault.core.design.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Global Color Tokens for SheVault.
 *
 * CRITICAL ARCHITECTURAL RULE:
 * Never write `Color(0xFFC92A32)` anywhere in application code.
 * Always reference `SheVaultColors.emergency` (or `SheVaultTheme.colors.emergency`).
 * This guarantees the emergency safety accent is universally customizable and replaceable.
 */
object SheVaultColors {
    /**
     * Finalized high-visibility emergency red for SOS triggers and critical alerts.
     */
    val emergency: Color = Color(0xFFC92A32)
}

/**
 * Foundation Raw Palette Tokens according to SheVault Locked Palette Specification.
 */
object PaletteTokens {
    // Primary Plum Scale
    val PlumPrimary     = Color(0xFF6D2E5B) // #6D2E5B
    val PlumPrimaryDark = Color(0xFF4A1F3D) // #4A1F3D
    val PlumPrimaryTint = Color(0xFFF1E5EF) // #F1E5EF
    val RoseAccent      = Color(0xFFC85C7B) // #C85C7B
    val PeachAccent     = Color(0xFFF4B6A6) // #F4B6A6

    // Light Neutral Scale
    val LightBackground = Color(0xFFFFF9F7) // #FFF9F7
    val LightSurface    = Color(0xFFFFFFFF) // #FFFFFF
    val LightTextPrimary= Color(0xFF211D20) // #211D20
    val LightSecondary  = Color(0xFF5C5459) // #5C5459
    val LightBorder     = Color(0xFFE7DEE3) // #E7DEE3

    // Semantic Scale
    val EmergencyDark   = Color(0xFFA61B23) // #A61B23
    val EmergencyBg     = Color(0xFFFDEBEC) // #FDEBEC
    val SafeTeal        = Color(0xFF0F766E) // #0F766E
    val SafeBg          = Color(0xFFE6F5F2) // #E6F5F2
    val WarningAmber    = Color(0xFFB54708) // #B54708
    val WarningBg       = Color(0xFFFFF4E5) // #FFF4E5
    val InfoBlue        = Color(0xFF3978B8) // #3978B8
    val InfoBg          = Color(0xFFEAF2FB) // #EAF2FB

    // Dark Mode Palette
    val DarkBackground  = Color(0xFF151116) // #151116
    val DarkSurface     = Color(0xFF211A21) // #211A21
    val DarkElevated    = Color(0xFF2B222A) // #2B222A
    val DarkTextPrimary = Color(0xFFF8F2F5) // #F8F2F5
    val DarkSecondary   = Color(0xFFC8BDC5) // #C8BDC5
    val DarkBorder      = Color(0xFF3B3038) // #3B3038
    val DarkFocus       = Color(0xFFC978A8) // #C978A8

    // Standard Pure Neutral
    val White           = Color(0xFFFFFFFF)
    val Black           = Color(0xFF000000)

    // Legacy aliases for backward compatibility
    val Emergency900    = EmergencyDark
    val Emergency800    = Color(0xFF690005)
    val Emergency700    = Color(0xFF93000A)
    val Emergency600    = SheVaultColors.emergency // 0xFFC92A32
    val Emergency500    = Color(0xFFDE3730)
    val Emergency100    = EmergencyBg
    val Emergency50     = Color(0xFFFFEDE8)

    val Primary950      = Color(0xFF1E0A3C)
    val Primary900      = PlumPrimaryDark
    val Primary800      = Color(0xFF4A148C)
    val Primary700      = PlumPrimary
    val Primary600      = PlumPrimary
    val Primary100      = PlumPrimaryTint
    val Primary50       = Color(0xFFF5F3FF)

    val Secondary700    = SafeTeal
    val Secondary600    = SafeTeal
    val Secondary100    = SafeBg
    val Secondary50     = Color(0xFFF0FDFA)

    val Warning600      = WarningAmber
    val Warning500      = WarningAmber
    val Warning100      = WarningBg

    val Safe600         = SafeTeal
    val Safe500         = SafeTeal
    val Safe100         = SafeBg

    val Slate950        = DarkBackground
    val Slate900        = DarkSurface
    val Slate800        = DarkElevated
    val Slate700        = DarkBorder
    val Slate600        = DarkSecondary
    val Slate400        = Color(0xFF94A3B8)
    val Slate300        = LightBorder
    val Slate200        = Color(0xFFE2E8F0)
    val Slate100        = PlumPrimaryTint
    val Slate50         = LightBackground

    val DecoyDarkSurface = Color(0xFF1E1E1E)
    val DecoyDarkBackground = Color(0xFF121212)
    val DecoyLightSurface = Color(0xFFF3F4F6)
    val DecoyLightBackground = Color(0xFFFFFFFF)
    val DecoyAccent = Color(0xFF3B82F6)
    val DecoyMuted = Color(0xFF64748B)
}

/**
 * Semantic Color Scheme contract for SheVault.
 */
@Immutable
data class SheVaultColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,

    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,

    val emergency: Color, // Always references SheVaultColors.emergency
    val onEmergency: Color,
    val emergencyContainer: Color,
    val onEmergencyContainer: Color,

    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    val safe: Color,
    val onSafe: Color,
    val safeContainer: Color,
    val onSafeContainer: Color,

    val info: Color = PaletteTokens.InfoBlue,
    val onInfo: Color = PaletteTokens.White,
    val infoContainer: Color = PaletteTokens.InfoBg,
    val onInfoContainer: Color = PaletteTokens.InfoBlue,

    val rose: Color = PaletteTokens.RoseAccent,
    val peach: Color = PaletteTokens.PeachAccent,

    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,

    val outline: Color,
    val outlineVariant: Color,

    val isDark: Boolean = false,
    val isDiscrete: Boolean = false
)

/**
 * Standard Light Theme Scheme using the SheVault Locked Palette
 */
val LightSheVaultColorScheme = SheVaultColorScheme(
    primary = PaletteTokens.PlumPrimary,
    onPrimary = PaletteTokens.White,
    primaryContainer = PaletteTokens.PlumPrimaryTint,
    onPrimaryContainer = PaletteTokens.PlumPrimaryDark,

    secondary = PaletteTokens.LightSecondary,
    onSecondary = PaletteTokens.White,
    secondaryContainer = PaletteTokens.PlumPrimaryTint,
    onSecondaryContainer = PaletteTokens.PlumPrimaryDark,

    emergency = SheVaultColors.emergency,
    onEmergency = PaletteTokens.White,
    emergencyContainer = PaletteTokens.EmergencyBg,
    onEmergencyContainer = PaletteTokens.EmergencyDark,

    warning = PaletteTokens.WarningAmber,
    onWarning = PaletteTokens.White,
    warningContainer = PaletteTokens.WarningBg,
    onWarningContainer = PaletteTokens.WarningAmber,

    safe = PaletteTokens.SafeTeal,
    onSafe = PaletteTokens.White,
    safeContainer = PaletteTokens.SafeBg,
    onSafeContainer = PaletteTokens.SafeTeal,

    info = PaletteTokens.InfoBlue,
    onInfo = PaletteTokens.White,
    infoContainer = PaletteTokens.InfoBg,
    onInfoContainer = PaletteTokens.InfoBlue,

    rose = PaletteTokens.RoseAccent,
    peach = PaletteTokens.PeachAccent,

    background = PaletteTokens.LightBackground,
    onBackground = PaletteTokens.LightTextPrimary,
    surface = PaletteTokens.LightSurface,
    onSurface = PaletteTokens.LightTextPrimary,
    surfaceVariant = PaletteTokens.PlumPrimaryTint,
    onSurfaceVariant = PaletteTokens.LightSecondary,

    outline = PaletteTokens.LightBorder,
    outlineVariant = PaletteTokens.LightBorder,
    isDark = false,
    isDiscrete = false
)

/**
 * Standard Dark Theme Scheme using the SheVault Locked Dark Palette
 */
val DarkSheVaultColorScheme = SheVaultColorScheme(
    primary = PaletteTokens.DarkFocus,
    onPrimary = PaletteTokens.DarkBackground,
    primaryContainer = PaletteTokens.PlumPrimaryDark,
    onPrimaryContainer = PaletteTokens.DarkTextPrimary,

    secondary = PaletteTokens.DarkSecondary,
    onSecondary = PaletteTokens.DarkBackground,
    secondaryContainer = PaletteTokens.DarkElevated,
    onSecondaryContainer = PaletteTokens.DarkTextPrimary,

    emergency = SheVaultColors.emergency,
    onEmergency = PaletteTokens.White,
    emergencyContainer = PaletteTokens.EmergencyDark,
    onEmergencyContainer = PaletteTokens.EmergencyBg,

    warning = PaletteTokens.WarningAmber,
    onWarning = PaletteTokens.DarkBackground,
    warningContainer = Color(0xFF451A03),
    onWarningContainer = PaletteTokens.WarningBg,

    safe = PaletteTokens.SafeTeal,
    onSafe = PaletteTokens.White,
    safeContainer = Color(0xFF042F2E),
    onSafeContainer = PaletteTokens.SafeBg,

    info = PaletteTokens.InfoBlue,
    onInfo = PaletteTokens.White,
    infoContainer = Color(0xFF082F49),
    onInfoContainer = PaletteTokens.InfoBg,

    rose = PaletteTokens.RoseAccent,
    peach = PaletteTokens.PeachAccent,

    background = PaletteTokens.DarkBackground,
    onBackground = PaletteTokens.DarkTextPrimary,
    surface = PaletteTokens.DarkSurface,
    onSurface = PaletteTokens.DarkTextPrimary,
    surfaceVariant = PaletteTokens.DarkElevated,
    onSurfaceVariant = PaletteTokens.DarkSecondary,

    outline = PaletteTokens.DarkBorder,
    outlineVariant = PaletteTokens.DarkBorder,
    isDark = true,
    isDiscrete = false
)

/**
 * Discrete Decoy Theme Scheme (Calculator camouflage)
 */
val DiscreteSheVaultColorScheme = SheVaultColorScheme(
    primary = PaletteTokens.DecoyAccent,
    onPrimary = PaletteTokens.White,
    primaryContainer = PaletteTokens.DecoyDarkSurface,
    onPrimaryContainer = PaletteTokens.White,

    secondary = PaletteTokens.DecoyMuted,
    onSecondary = PaletteTokens.White,
    secondaryContainer = PaletteTokens.DecoyDarkSurface,
    onSecondaryContainer = PaletteTokens.White,

    emergency = SheVaultColors.emergency,
    onEmergency = PaletteTokens.White,
    emergencyContainer = PaletteTokens.DecoyDarkSurface,
    onEmergencyContainer = PaletteTokens.White,

    warning = PaletteTokens.DecoyMuted,
    onWarning = PaletteTokens.White,
    warningContainer = PaletteTokens.DecoyDarkSurface,
    onWarningContainer = PaletteTokens.White,

    safe = PaletteTokens.DecoyMuted,
    onSafe = PaletteTokens.White,
    safeContainer = PaletteTokens.DecoyDarkSurface,
    onSafeContainer = PaletteTokens.White,

    background = PaletteTokens.DecoyDarkBackground,
    onBackground = PaletteTokens.White,
    surface = PaletteTokens.DecoyDarkSurface,
    onSurface = PaletteTokens.White,
    surfaceVariant = PaletteTokens.DecoyDarkSurface,
    onSurfaceVariant = PaletteTokens.DecoyMuted,

    outline = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFF333333),
    isDark = true,
    isDiscrete = true
)
