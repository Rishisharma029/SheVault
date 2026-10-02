package com.shevault.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.shevault.core.design.tokens.DarkSheVaultColorScheme
import com.shevault.core.design.tokens.DarkSheVaultStatusTokens
import com.shevault.core.design.tokens.DefaultSheVaultElevation
import com.shevault.core.design.tokens.DefaultSheVaultIconRules
import com.shevault.core.design.tokens.DefaultSheVaultMotion
import com.shevault.core.design.tokens.DefaultSheVaultRadius
import com.shevault.core.design.tokens.DefaultSheVaultSpacing
import com.shevault.core.design.tokens.DefaultSheVaultTypography
import com.shevault.core.design.tokens.DiscreteSheVaultColorScheme
import com.shevault.core.design.tokens.LightSheVaultColorScheme
import com.shevault.core.design.tokens.LightSheVaultStatusTokens
import com.shevault.core.design.tokens.SheVaultColorScheme
import com.shevault.core.design.tokens.SheVaultElevation
import com.shevault.core.design.tokens.SheVaultIconRules
import com.shevault.core.design.tokens.SheVaultMotion
import com.shevault.core.design.tokens.SheVaultRadius
import com.shevault.core.design.tokens.SheVaultSpacing
import com.shevault.core.design.tokens.SheVaultStatusTokens
import com.shevault.core.design.tokens.SheVaultTypography

// Composition locals for design system tokens
val LocalSheVaultColors = staticCompositionLocalOf { LightSheVaultColorScheme }
val LocalSheVaultTypography = staticCompositionLocalOf { DefaultSheVaultTypography }
val LocalSheVaultSpacing = staticCompositionLocalOf { DefaultSheVaultSpacing }
val LocalSheVaultRadius = staticCompositionLocalOf { DefaultSheVaultRadius }
val LocalSheVaultElevation = staticCompositionLocalOf { DefaultSheVaultElevation }
val LocalSheVaultMotion = staticCompositionLocalOf { DefaultSheVaultMotion }
val LocalSheVaultIconRules = staticCompositionLocalOf { DefaultSheVaultIconRules }
val LocalSheVaultStatus = staticCompositionLocalOf { LightSheVaultStatusTokens }

/**
 * Global entry point to access the SheVault Design System.
 * Usage: `SheVaultTheme.colors.emergency`, `SheVaultTheme.spacing.md`, etc.
 */
object SheVaultTheme {
    val colors: SheVaultColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalSheVaultColors.current

    val typography: SheVaultTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalSheVaultTypography.current

    val spacing: SheVaultSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSheVaultSpacing.current

    val radius: SheVaultRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalSheVaultRadius.current

    val elevation: SheVaultElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalSheVaultElevation.current

    val motion: SheVaultMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalSheVaultMotion.current

    val iconRules: SheVaultIconRules
        @Composable
        @ReadOnlyComposable
        get() = LocalSheVaultIconRules.current

    val status: SheVaultStatusTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSheVaultStatus.current
}

/**
 * Root Composable Theme Provider for SheVault.
 * Wraps Jetpack Compose Material 3 and injects custom SheVault Design Tokens.
 *
 * @param darkTheme Whether dark mode is active
 * @param discreteMode When true, activates decoy camouflage mode (calculator/neutral utility)
 * @param content The composable tree
 */
@Composable
fun SheVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    discreteMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        discreteMode -> DiscreteSheVaultColorScheme
        darkTheme -> DarkSheVaultColorScheme
        else -> LightSheVaultColorScheme
    }

    val statusTokens = if (darkTheme || discreteMode) {
        DarkSheVaultStatusTokens
    } else {
        LightSheVaultStatusTokens
    }

    // Material 3 Color Scheme Bridge
    val material3ColorScheme = if (darkTheme || discreteMode) {
        darkColorScheme(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            primaryContainer = colorScheme.primaryContainer,
            onPrimaryContainer = colorScheme.onPrimaryContainer,
            secondary = colorScheme.secondary,
            onSecondary = colorScheme.onSecondary,
            secondaryContainer = colorScheme.secondaryContainer,
            onSecondaryContainer = colorScheme.onSecondaryContainer,
            error = colorScheme.emergency,
            onError = colorScheme.onEmergency,
            errorContainer = colorScheme.emergencyContainer,
            onErrorContainer = colorScheme.onEmergencyContainer,
            background = colorScheme.background,
            onBackground = colorScheme.onBackground,
            surface = colorScheme.surface,
            onSurface = colorScheme.onSurface,
            surfaceVariant = colorScheme.surfaceVariant,
            onSurfaceVariant = colorScheme.onSurfaceVariant,
            outline = colorScheme.outline,
            outlineVariant = colorScheme.outlineVariant
        )
    } else {
        lightColorScheme(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            primaryContainer = colorScheme.primaryContainer,
            onPrimaryContainer = colorScheme.onPrimaryContainer,
            secondary = colorScheme.secondary,
            onSecondary = colorScheme.onSecondary,
            secondaryContainer = colorScheme.secondaryContainer,
            onSecondaryContainer = colorScheme.onSecondaryContainer,
            error = colorScheme.emergency,
            onError = colorScheme.onEmergency,
            errorContainer = colorScheme.emergencyContainer,
            onErrorContainer = colorScheme.onEmergencyContainer,
            background = colorScheme.background,
            onBackground = colorScheme.onBackground,
            surface = colorScheme.surface,
            onSurface = colorScheme.onSurface,
            surfaceVariant = colorScheme.surfaceVariant,
            onSurfaceVariant = colorScheme.onSurfaceVariant,
            outline = colorScheme.outline,
            outlineVariant = colorScheme.outlineVariant
        )
    }

    // Material 3 Typography Bridge
    val material3Typography = Typography(
        displayLarge = DefaultSheVaultTypography.displayLarge,
        displayMedium = DefaultSheVaultTypography.displayMedium,
        displaySmall = DefaultSheVaultTypography.displaySmall,
        headlineLarge = DefaultSheVaultTypography.headlineLarge,
        headlineMedium = DefaultSheVaultTypography.headlineMedium,
        headlineSmall = DefaultSheVaultTypography.headlineSmall,
        titleLarge = DefaultSheVaultTypography.titleLarge,
        titleMedium = DefaultSheVaultTypography.titleMedium,
        titleSmall = DefaultSheVaultTypography.titleSmall,
        bodyLarge = DefaultSheVaultTypography.bodyLarge,
        bodyMedium = DefaultSheVaultTypography.bodyMedium,
        bodySmall = DefaultSheVaultTypography.bodySmall,
        labelLarge = DefaultSheVaultTypography.labelLarge,
        labelMedium = DefaultSheVaultTypography.labelMedium,
        labelSmall = DefaultSheVaultTypography.labelSmall
    )

    // Material 3 Shapes Bridge
    val material3Shapes = Shapes(
        extraSmall = RoundedCornerShape(DefaultSheVaultRadius.xs),
        small = RoundedCornerShape(DefaultSheVaultRadius.sm),
        medium = RoundedCornerShape(DefaultSheVaultRadius.md),
        large = RoundedCornerShape(DefaultSheVaultRadius.lg),
        extraLarge = RoundedCornerShape(DefaultSheVaultRadius.xl)
    )

    CompositionLocalProvider(
        LocalSheVaultColors provides colorScheme,
        LocalSheVaultTypography provides DefaultSheVaultTypography,
        LocalSheVaultSpacing provides DefaultSheVaultSpacing,
        LocalSheVaultRadius provides DefaultSheVaultRadius,
        LocalSheVaultElevation provides DefaultSheVaultElevation,
        LocalSheVaultMotion provides DefaultSheVaultMotion,
        LocalSheVaultIconRules provides DefaultSheVaultIconRules,
        LocalSheVaultStatus provides statusTokens
    ) {
        MaterialTheme(
            colorScheme = material3ColorScheme,
            typography = material3Typography,
            shapes = material3Shapes,
            content = content
        )
    }
}
