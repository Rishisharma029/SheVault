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
 * 1. Brand Palette Tokens
 * Rule: Plum = SheVault (#6D2E5B).
 * Rose and peach are supporting accents, not replacements for plum.
 */
object BrandTokens {
    val primary     = Color(0xFF6D2E5B) // Main brand, primary actions, active navigation
    val primaryDark = Color(0xFF4A1F3D) // Deep brand surfaces, pressed states, headers
    val primaryTint = Color(0xFFF1E5EF) // Selected cards, soft brand backgrounds
    val rose        = Color(0xFFC85C7B) // Accent, illustrations, large visual elements
    val peach       = Color(0xFFF4B6A6) // Warm decorative highlight
}

/**
 * 2. Light Theme Neutrals
 * Note: textSecondary is strictly #5C5459 (rather than #70686D).
 */
object LightNeutralTokens {
    val background      = Color(0xFFFFF9F7) // Application background
    val surface         = Color(0xFFFFFFFF) // Cards, sheets, dialogs
    val surfaceElevated = Color(0xFFFFFFFF) // Elevated surfaces
    val textPrimary     = Color(0xFF211D20) // Main text
    val textSecondary   = Color(0xFF5C5459) // Secondary/helper text
    val textDisabled    = Color(0xFF9D959A) // Disabled text
    val border          = Color(0xFFE7DEE3) // Borders
    val divider         = Color(0xFFEEE7EB) // Soft separators
}

/**
 * 3. Dark Theme Neutrals
 */
object DarkNeutralTokens {
    val background      = Color(0xFF151116) // Main background
    val surface         = Color(0xFF211A21) // Cards
    val surfaceElevated = Color(0xFF2B222A) // Dialogs/sheets/elevated cards
    val textPrimary     = Color(0xFFF8F2F5) // Main text
    val textSecondary   = Color(0xFFC8BDC5) // Secondary text
    val textDisabled    = Color(0xFF756A73) // Disabled text
    val border          = Color(0xFF3B3038) // Borders
    val divider         = Color(0xFF332933) // Soft separators
}

/**
 * 4. Safety Semantic Palette
 */
object SafeTokens {
    val default     = Color(0xFF0F766E) // Safe confirmation
    val dark        = Color(0xFF0A5B55) // Safe dark
    val surface     = Color(0xFFE6F5F2) // Safe light background container
    val surfaceDark = Color(0x2E0F766E) // rgba(15, 118, 110, 0.18)
}

object EmergencyTokens {
    val default     = SheVaultColors.emergency // #C92A32 - Never decorative
    val dark        = Color(0xFFA61B23)
    val surface     = Color(0xFFFDEBEC)
    val surfaceDark = Color(0x2EC92A32) // rgba(201, 42, 50, 0.18)
}

object WarningTokens {
    val default     = Color(0xFFB54708) // Warning default (never amber #D9911E)
    val dark        = Color(0xFF8C3405)
    val surface     = Color(0xFFFFF4E5)
    val surfaceDark = Color(0x2EB54708) // rgba(181, 71, 8, 0.18)
}

object InfoTokens {
    val default     = Color(0xFF3978B8)
    val dark        = Color(0xFF2E6093)
    val surface     = Color(0xFFEAF2FB)
    val surfaceDark = Color(0x2E3978B8) // rgba(57, 120, 184, 0.18)
}

/**
 * 5. Focus Rings
 */
object FocusRingTokens {
    val light = Color(0xFF6D2E5B) // 3px, 2px offset
    val dark  = Color(0xFFC978A8) // 3px, 2px offset
}

/**
 * 6. Discreet Calculator Decoy Palette
 * Zero SheVault branding / plum / emergency red.
 */
object DecoyCalculatorTokens {
    val background    = Color(0xFF101010)
    val surface       = Color(0xFF1C1C1C)
    val key           = Color(0xFF2A2A2A)
    val keyPressed     = Color(0xFF3A3A3A)
    val text          = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val operator      = Color(0xFFD0D0D0)
}

/**
 * 7. Shadows, Scrims & Skeletons
 */
object ShadowTokens {
    val soft  = Color(0x0D4A1F3D) // rgba(74, 31, 61, 0.05)
    val card  = Color(0x144A1F3D) // rgba(74, 31, 61, 0.08)
    val modal = Color(0x1F4A1F3D) // rgba(74, 31, 61, 0.12)
}

object ScrimTokens {
    val light = Color(0x52211D20) // rgba(33, 29, 32, 0.32)
    val dark  = Color(0x7A000000) // rgba(0, 0, 0, 0.48)
}

object SkeletonTokens {
    val lightBase      = Color(0xFFF1ECEF) // Section 29
    val lightHighlight = Color(0xFFF8F4F6)
    val darkBase       = Color(0xFF2B222A)
    val darkHighlight  = Color(0xFF342B33)
}

/**
 * 8. Bottom Navigation Tokens
 * SOS button is strictly excluded from bottom navigation.
 */
object BottomNavTokens {
    val lightInactive   = Color(0xFF706970)
    val lightActive     = Color(0xFF6D2E5B)
    val lightActivePill = Color(0xFFF1E5EF)
    val darkInactive    = Color(0xFFAFA3AC)
    val darkActive      = Color(0xFFC978A8)
    val darkActivePill  = Color(0x24C978A8) // rgba(201, 120, 168, 0.14)
}

/**
 * 9. Button Palette Tokens (Section 9)
 */
object ButtonTokens {
    // Primary Button
    val primaryBackground = BrandTokens.primary      // #6D2E5B
    val primaryText       = Color(0xFFFFFFFF)
    val primaryPressed    = BrandTokens.primaryDark  // #4A1F3D

    // Emergency Button
    val emergencyBackground = SheVaultColors.emergency // #C92A32
    val emergencyText       = Color(0xFFFFFFFF)
    val emergencyPressed    = EmergencyTokens.dark    // #A61B23

    // Safe Button
    val safeBackground = SafeTokens.default    // #0F766E
    val safeText       = Color(0xFFFFFFFF)
    val safePressed    = SafeTokens.dark       // #0A5B55

    // Secondary (Light)
    val secondaryBackground = Color(0xFFFFFFFF)
    val secondaryText       = BrandTokens.primary      // #6D2E5B
    val secondaryBorder     = LightNeutralTokens.border // #E7DEE3
    val secondaryPressedBg  = BrandTokens.primaryTint  // #F1E5EF

    // Secondary (Dark)
    val darkSecondaryBackground = DarkNeutralTokens.surfaceElevated // #2B222A
    val darkSecondaryText       = FocusRingTokens.dark             // #C978A8
    val darkSecondaryBorder     = DarkNeutralTokens.border         // #3B3038
}

/**
 * 10. Text Hierarchy Tokens (Section 10)
 */
object TextHierarchyTokens {
    val primaryHeading = LightNeutralTokens.textPrimary // #211D20
    val body           = LightNeutralTokens.textPrimary // #211D20
    val secondary      = LightNeutralTokens.textSecondary // #5C5459
    val disabled       = LightNeutralTokens.textDisabled // #9D959A
    val emergency      = EmergencyTokens.dark          // #A61B23
    val safe           = SafeTokens.dark               // #0A5B55
    val warning        = WarningTokens.default         // #B54708
    val info           = InfoTokens.dark               // #2E6093

    // Dark Mode Text
    val darkPrimary    = DarkNeutralTokens.textPrimary   // #F8F2F5
    val darkSecondary  = DarkNeutralTokens.textSecondary // #C8BDC5
    val darkDisabled   = DarkNeutralTokens.textDisabled  // #756A73
}

/**
 * 12. Selection Tokens (Section 12)
 */
object SelectionTokens {
    // Light
    val lightBackground = BrandTokens.primaryTint // #F1E5EF
    val lightBorder     = BrandTokens.primary     // #6D2E5B
    val lightIcon       = BrandTokens.primary     // #6D2E5B

    // Dark
    val darkBackground  = Color(0x26C978A8)       // rgba(201,120,168,0.15)
    val darkBorder      = FocusRingTokens.dark    // #C978A8
    val darkIcon        = FocusRingTokens.dark    // #C978A8
}

/**
 * 13. Input Field Tokens (Section 13)
 */
object InputFieldTokens {
    val normalBackground  = LightNeutralTokens.surface      // #FFFFFF
    val normalBorder      = LightNeutralTokens.border       // #E7DEE3
    val normalText        = LightNeutralTokens.textPrimary  // #211D20
    val normalPlaceholder = LightNeutralTokens.textSecondary// #5C5459

    val focusBorder       = BrandTokens.primary             // #6D2E5B

    val errorBorder       = SheVaultColors.emergency        // #C92A32
    val errorBackground   = EmergencyTokens.surface         // #FDEBEC

    val successBorder     = SafeTokens.default              // #0F766E
    val successBackground = SafeTokens.surface              // #E6F5F2
}

/**
 * 14. Card Palette Tokens (Section 14)
 */
object CardTokens {
    // Normal Card
    val normalSurface = LightNeutralTokens.surface // #FFFFFF
    val normalBorder  = LightNeutralTokens.border  // #E7DEE3
    val normalShadow  = ShadowTokens.card          // rgba(74,31,61,0.08)

    // Brand Card
    val brandBackground = BrandTokens.primaryTint // #F1E5EF
    val brandBorder     = Color(0x1F6D2E5B)       // rgba(109,46,91,0.12)

    // Safe Card
    val safeBackground  = SafeTokens.surface      // #E6F5F2
    val safeBorder      = Color(0x260F766E)       // rgba(15,118,110,0.15)

    // Warning Card
    val warningBackground = WarningTokens.surface // #FFF4E5
    val warningBorder     = Color(0x26B54708)     // rgba(181,71,8,0.15)

    // Emergency Card
    val emergencyBackground = EmergencyTokens.surface // #FDEBEC
    val emergencyBorder     = Color(0x26C92A32)       // rgba(201,42,50,0.15)
}

/**
 * 15. SOS Visual Language Tokens (Section 15)
 */
object SosVisualTokens {
    val idleColor    = SheVaultColors.emergency // #C92A32
    val idleRing     = Color(0x1FC92A32)        // rgba(201,42,50,0.12)
    val idleText     = LightNeutralTokens.textPrimary // #211D20

    val holdingColor = SheVaultColors.emergency // #C92A32
    val holdingRing  = EmergencyTokens.dark     // #A61B23
    val holdingBg    = EmergencyTokens.surface  // #FDEBEC

    val activePrimary= SheVaultColors.emergency // #C92A32
    val activeCritical= EmergencyTokens.dark    // #A61B23
    val activeSurface = EmergencyTokens.surface // #FDEBEC
}

/**
 * 16. Incident Timeline Tokens (Section 16)
 */
object IncidentTimelineTokens {
    val started     = BrandTokens.primary      // Plum #6D2E5B
    val location    = InfoTokens.default       // Blue #3978B8
    val contact     = SafeTokens.default       // Teal #0F766E
    val networkLost = WarningTokens.default    // Amber #B54708
    val escalation  = SheVaultColors.emergency // Red #C92A32
    val ended       = SafeTokens.default       // Teal #0F766E
}

/**
 * 17. Connectivity Tokens (Section 17)
 */
object ConnectivityTokens {
    val connected = SafeTokens.default               // #0F766E
    val degraded  = WarningTokens.default            // #B54708
    val offline   = LightNeutralTokens.textSecondary // #5C5459
    val restoring = InfoTokens.default               // #3978B8
}

/**
 * 18. Battery Tokens (Section 18)
 */
object BatteryTokens {
    val safe            = SafeTokens.default               // 80–100%: #0F766E
    val neutral         = LightNeutralTokens.textSecondary // 40–79%: #5C5459
    val warning         = WarningTokens.default            // 20–39%: #B54708
    val warningCritical = WarningTokens.default            // 10–19%: #B54708
    val emergency       = SheVaultColors.emergency         // <10%: #C92A32
}

/**
 * 19. Location Status Tokens (Section 19)
 */
object LocationStatusTokens {
    val excellent   = SafeTokens.default       // #0F766E
    val approximate = InfoTokens.default       // #3978B8
    val degraded    = WarningTokens.default    // #B54708
    val unavailable = SheVaultColors.emergency // #C92A32
}

/**
 * 20. Map Palette Tokens (Section 20)
 */
object MapTokens {
    val markerOuterRing = EmergencyTokens.surface  // #FDEBEC
    val markerPin       = SheVaultColors.emergency // #C92A32
    val markerCenter    = Color(0xFFFFFFFF)

    val routeNormal     = BrandTokens.primary      // #6D2E5B
    val routeRecommended= SafeTokens.default       // #0F766E
    val routeAlternative= InfoTokens.default       // #3978B8
    val routeUncertain  = WarningTokens.default    // #B54708

    val accuracyCircle  = Color(0x1FC92A32)        // rgba(201,42,50,0.12)
}

/**
 * 21. Trusted Circle Palette Tokens (Section 21)
 */
object TrustedCircleTokens {
    val activeContact = SafeTokens.default               // #0F766E
    val activeText    = LightNeutralTokens.textPrimary   // #211D20
    val pending       = WarningTokens.default            // #B54708
    val failed        = SheVaultColors.emergency         // #C92A32
    val disabled      = LightNeutralTokens.textDisabled  // #9D959A
}

/**
 * 22. Check-In Palette Tokens (Section 22)
 */
object CheckInTokens {
    val active    = BrandTokens.primary      // #6D2E5B
    val completed = SafeTokens.default       // #0F766E
    val overdue   = WarningTokens.default    // #B54708
    val escalated = SheVaultColors.emergency // #C92A32
}

/**
 * 23. History Palette Tokens (Section 23)
 */
object HistoryTokens {
    val completed = SafeTokens.default               // #0F766E
    val cancelled = LightNeutralTokens.textSecondary // #5C5459
    val escalated = SheVaultColors.emergency         // #C92A32
    val unknown   = LightNeutralTokens.textDisabled  // #9D959A
}

/**
 * 28. Overlay & Glass Tokens (Section 28)
 */
object OverlayTokens {
    val scrimLight       = ScrimTokens.light        // rgba(33,29,32,0.32)
    val scrimDark        = ScrimTokens.dark         // rgba(0,0,0,0.48)
    val surfaceGlass     = Color(0xB8FFFFFF)        // rgba(255,255,255,0.72)
    val surfaceDarkGlass = Color(0xCC2B222A)        // rgba(43,34,42,0.80)
}

/**
 * 30. Chart / Analytics Palette Tokens (Section 30)
 */
object ChartTokens {
    val primary   = BrandTokens.primary      // #6D2E5B
    val secondary = InfoTokens.default       // #3978B8
    val safe      = SafeTokens.default       // #0F766E
    val warning   = WarningTokens.default    // #B54708
    val emergency = SheVaultColors.emergency // #C92A32
}

/**
 * 32. Approved Gradient Tokens (Section 32)
 * Rule: Never use gradients on SOS.
 */
object GradientTokens {
    val brandStart = BrandTokens.primary // #6D2E5B
    val brandEnd   = BrandTokens.rose    // #C85C7B

    val warmStart  = BrandTokens.primaryTint // #F1E5EF
    val warmEnd    = BrandTokens.peach       // #F4B6A6
}

/**
 * Foundation Raw Palette Tokens (Maintained for unified namespace and backwards-compatibility).
 */
object PaletteTokens {
    // Primary Plum Scale
    val PlumPrimary     = BrandTokens.primary
    val PlumPrimaryDark = BrandTokens.primaryDark
    val PlumPrimaryTint = BrandTokens.primaryTint
    val RoseAccent      = BrandTokens.rose
    val PeachAccent     = BrandTokens.peach

    // Light Neutral Scale
    val LightBackground = LightNeutralTokens.background
    val LightSurface    = LightNeutralTokens.surface
    val LightTextPrimary= LightNeutralTokens.textPrimary
    val LightSecondary  = LightNeutralTokens.textSecondary
    val LightBorder     = LightNeutralTokens.border
    val LightDivider    = LightNeutralTokens.divider

    // Semantic Scale
    val EmergencyDark   = EmergencyTokens.dark
    val EmergencyBg     = EmergencyTokens.surface
    val SafeTeal        = SafeTokens.default
    val SafeBg          = SafeTokens.surface
    val WarningAmber    = WarningTokens.default
    val WarningBg       = WarningTokens.surface
    val InfoBlue        = InfoTokens.default
    val InfoBg          = InfoTokens.surface

    // Dark Mode Palette
    val DarkBackground  = DarkNeutralTokens.background
    val DarkSurface     = DarkNeutralTokens.surface
    val DarkElevated    = DarkNeutralTokens.surfaceElevated
    val DarkTextPrimary = DarkNeutralTokens.textPrimary
    val DarkSecondary   = DarkNeutralTokens.textSecondary
    val DarkBorder      = DarkNeutralTokens.border
    val DarkDivider     = DarkNeutralTokens.divider
    val DarkFocus       = FocusRingTokens.dark

    // Standard Pure Neutral
    val White           = Color(0xFFFFFFFF)
    val Black           = Color(0xFF000000)

    // Decoy Calculator Aliases
    val DecoyDarkSurface    = DecoyCalculatorTokens.surface
    val DecoyDarkBackground = DecoyCalculatorTokens.background
    val DecoyLightSurface   = LightNeutralTokens.surface
    val DecoyLightBackground= LightNeutralTokens.background
    val DecoyAccent         = DecoyCalculatorTokens.operator
    val DecoyMuted          = DecoyCalculatorTokens.textSecondary

    // Legacy aliases for backward compatibility
    val Emergency900    = EmergencyDark
    val Emergency800    = Color(0xFF690005)
    val Emergency700    = Color(0xFF93000A)
    val Emergency600    = SheVaultColors.emergency
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
    val emergencyDark: Color = EmergencyTokens.dark,

    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val warningDark: Color = WarningTokens.dark,

    val safe: Color,
    val onSafe: Color,
    val safeContainer: Color,
    val onSafeContainer: Color,
    val safeDark: Color = SafeTokens.dark,

    val info: Color = InfoTokens.default,
    val onInfo: Color = PaletteTokens.White,
    val infoContainer: Color = InfoTokens.surface,
    val onInfoContainer: Color = InfoTokens.default,
    val infoDark: Color = InfoTokens.dark,

    val rose: Color = BrandTokens.rose,
    val peach: Color = BrandTokens.peach,

    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceElevated: Color = surface,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,

    val outline: Color,
    val outlineVariant: Color,
    val divider: Color = outlineVariant,

    val textPrimary: Color = onBackground,
    val textSecondary: Color = onSurfaceVariant,
    val textDisabled: Color = LightNeutralTokens.textDisabled,

    val focusRing: Color = primary,
    val scrim: Color = ScrimTokens.light,

    val navInactive: Color = BottomNavTokens.lightInactive,
    val navActive: Color = BottomNavTokens.lightActive,
    val navActivePill: Color = BottomNavTokens.lightActivePill,

    val isDark: Boolean = false,
    val isDiscrete: Boolean = false
)

/**
 * Standard Light Theme Scheme using the SheVault Locked Palette
 */
val LightSheVaultColorScheme = SheVaultColorScheme(
    primary = BrandTokens.primary,
    onPrimary = PaletteTokens.White,
    primaryContainer = BrandTokens.primaryTint,
    onPrimaryContainer = BrandTokens.primaryDark,

    secondary = LightNeutralTokens.textSecondary,
    onSecondary = PaletteTokens.White,
    secondaryContainer = BrandTokens.primaryTint,
    onSecondaryContainer = BrandTokens.primaryDark,

    emergency = SheVaultColors.emergency,
    onEmergency = PaletteTokens.White,
    emergencyContainer = EmergencyTokens.surface,
    onEmergencyContainer = EmergencyTokens.dark,
    emergencyDark = EmergencyTokens.dark,

    warning = WarningTokens.default,
    onWarning = PaletteTokens.White,
    warningContainer = WarningTokens.surface,
    onWarningContainer = WarningTokens.dark,
    warningDark = WarningTokens.dark,

    safe = SafeTokens.default,
    onSafe = PaletteTokens.White,
    safeContainer = SafeTokens.surface,
    onSafeContainer = SafeTokens.dark,
    safeDark = SafeTokens.dark,

    info = InfoTokens.default,
    onInfo = PaletteTokens.White,
    infoContainer = InfoTokens.surface,
    onInfoContainer = InfoTokens.dark,
    infoDark = InfoTokens.dark,

    rose = BrandTokens.rose,
    peach = BrandTokens.peach,

    background = LightNeutralTokens.background,
    onBackground = LightNeutralTokens.textPrimary,
    surface = LightNeutralTokens.surface,
    onSurface = LightNeutralTokens.textPrimary,
    surfaceElevated = LightNeutralTokens.surfaceElevated,
    surfaceVariant = BrandTokens.primaryTint,
    onSurfaceVariant = LightNeutralTokens.textSecondary,

    outline = LightNeutralTokens.border,
    outlineVariant = LightNeutralTokens.border,
    divider = LightNeutralTokens.divider,

    textPrimary = LightNeutralTokens.textPrimary,
    textSecondary = LightNeutralTokens.textSecondary,
    textDisabled = LightNeutralTokens.textDisabled,

    focusRing = FocusRingTokens.light,
    scrim = ScrimTokens.light,

    navInactive = BottomNavTokens.lightInactive,
    navActive = BottomNavTokens.lightActive,
    navActivePill = BottomNavTokens.lightActivePill,

    isDark = false,
    isDiscrete = false
)

/**
 * Standard Dark Theme Scheme using the SheVault Locked Dark Palette
 */
val DarkSheVaultColorScheme = SheVaultColorScheme(
    primary = FocusRingTokens.dark,
    onPrimary = DarkNeutralTokens.background,
    primaryContainer = BrandTokens.primaryDark,
    onPrimaryContainer = DarkNeutralTokens.textPrimary,

    secondary = DarkNeutralTokens.textSecondary,
    onSecondary = DarkNeutralTokens.background,
    secondaryContainer = DarkNeutralTokens.surfaceElevated,
    onSecondaryContainer = DarkNeutralTokens.textPrimary,

    emergency = SheVaultColors.emergency,
    onEmergency = PaletteTokens.White,
    emergencyContainer = EmergencyTokens.surfaceDark,
    onEmergencyContainer = EmergencyTokens.surface,
    emergencyDark = EmergencyTokens.dark,

    warning = WarningTokens.default,
    onWarning = DarkNeutralTokens.background,
    warningContainer = WarningTokens.surfaceDark,
    onWarningContainer = WarningTokens.surface,
    warningDark = WarningTokens.dark,

    safe = SafeTokens.default,
    onSafe = PaletteTokens.White,
    safeContainer = SafeTokens.surfaceDark,
    onSafeContainer = SafeTokens.surface,
    safeDark = SafeTokens.dark,

    info = InfoTokens.default,
    onInfo = PaletteTokens.White,
    infoContainer = InfoTokens.surfaceDark,
    onInfoContainer = InfoTokens.surface,
    infoDark = InfoTokens.dark,

    rose = BrandTokens.rose,
    peach = BrandTokens.peach,

    background = DarkNeutralTokens.background,
    onBackground = DarkNeutralTokens.textPrimary,
    surface = DarkNeutralTokens.surface,
    onSurface = DarkNeutralTokens.textPrimary,
    surfaceElevated = DarkNeutralTokens.surfaceElevated,
    surfaceVariant = DarkNeutralTokens.surfaceElevated,
    onSurfaceVariant = DarkNeutralTokens.textSecondary,

    outline = DarkNeutralTokens.border,
    outlineVariant = DarkNeutralTokens.border,
    divider = DarkNeutralTokens.divider,

    textPrimary = DarkNeutralTokens.textPrimary,
    textSecondary = DarkNeutralTokens.textSecondary,
    textDisabled = DarkNeutralTokens.textDisabled,

    focusRing = FocusRingTokens.dark,
    scrim = ScrimTokens.dark,

    navInactive = BottomNavTokens.darkInactive,
    navActive = BottomNavTokens.darkActive,
    navActivePill = BottomNavTokens.darkActivePill,

    isDark = true,
    isDiscrete = false
)

/**
 * Discrete Decoy Theme Scheme (Calculator camouflage)
 * Zero SheVault branding / plum / emergency colors exposed to aggressor.
 */
val DiscreteSheVaultColorScheme = SheVaultColorScheme(
    primary = DecoyCalculatorTokens.operator,
    onPrimary = DecoyCalculatorTokens.background,
    primaryContainer = DecoyCalculatorTokens.surface,
    onPrimaryContainer = DecoyCalculatorTokens.text,

    secondary = DecoyCalculatorTokens.textSecondary,
    onSecondary = DecoyCalculatorTokens.background,
    secondaryContainer = DecoyCalculatorTokens.surface,
    onSecondaryContainer = DecoyCalculatorTokens.text,

    emergency = SheVaultColors.emergency,
    onEmergency = DecoyCalculatorTokens.text,
    emergencyContainer = DecoyCalculatorTokens.surface,
    onEmergencyContainer = DecoyCalculatorTokens.text,

    warning = DecoyCalculatorTokens.textSecondary,
    onWarning = DecoyCalculatorTokens.text,
    warningContainer = DecoyCalculatorTokens.surface,
    onWarningContainer = DecoyCalculatorTokens.text,

    safe = DecoyCalculatorTokens.textSecondary,
    onSafe = DecoyCalculatorTokens.text,
    safeContainer = DecoyCalculatorTokens.surface,
    onSafeContainer = DecoyCalculatorTokens.text,

    background = DecoyCalculatorTokens.background,
    onBackground = DecoyCalculatorTokens.text,
    surface = DecoyCalculatorTokens.surface,
    onSurface = DecoyCalculatorTokens.text,
    surfaceElevated = DecoyCalculatorTokens.key,
    surfaceVariant = DecoyCalculatorTokens.surface,
    onSurfaceVariant = DecoyCalculatorTokens.textSecondary,

    outline = DecoyCalculatorTokens.key,
    outlineVariant = DecoyCalculatorTokens.key,
    divider = DecoyCalculatorTokens.key,

    textPrimary = DecoyCalculatorTokens.text,
    textSecondary = DecoyCalculatorTokens.textSecondary,
    textDisabled = DecoyCalculatorTokens.textSecondary,

    focusRing = DecoyCalculatorTokens.keyPressed,
    scrim = ScrimTokens.dark,

    navInactive = DecoyCalculatorTokens.textSecondary,
    navActive = DecoyCalculatorTokens.text,
    navActivePill = DecoyCalculatorTokens.key,

    isDark = true,
    isDiscrete = true
)
