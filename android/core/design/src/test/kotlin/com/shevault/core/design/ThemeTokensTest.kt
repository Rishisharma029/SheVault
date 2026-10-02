package com.shevault.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shevault.core.design.tokens.DarkSheVaultColorScheme
import com.shevault.core.design.tokens.DarkSheVaultStatusTokens
import com.shevault.core.design.tokens.DefaultSheVaultElevation
import com.shevault.core.design.tokens.DefaultSheVaultIconRules
import com.shevault.core.design.tokens.DefaultSheVaultRadius
import com.shevault.core.design.tokens.DefaultSheVaultSpacing
import com.shevault.core.design.tokens.DefaultSheVaultTypography
import com.shevault.core.design.tokens.DiscreteSheVaultColorScheme
import com.shevault.core.design.tokens.LightSheVaultColorScheme
import com.shevault.core.design.tokens.LightSheVaultStatusTokens
import com.shevault.core.design.tokens.SafetyState
import com.shevault.core.design.tokens.SheVaultColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTokensTest {

    @Test
    fun testEmergencyColorExactValue() {
        // Critical requirement: SheVaultColors.emergency is 0xFFC92A32
        val expected = Color(0xFFC92A32)
        assertEquals(expected, SheVaultColors.emergency)
    }

    @Test
    fun testColorSchemesReferenceEmergencyToken() {
        assertEquals("Light scheme emergency must match SheVaultColors.emergency",
            SheVaultColors.emergency, LightSheVaultColorScheme.emergency)
        assertEquals("Dark scheme emergency must match SheVaultColors.emergency",
            SheVaultColors.emergency, DarkSheVaultColorScheme.emergency)
        assertEquals("Discrete scheme emergency must match SheVaultColors.emergency",
            SheVaultColors.emergency, DiscreteSheVaultColorScheme.emergency)
    }

    @Test
    fun testStatusTokensCoverAllSafetyStates() {
        val lightTokens = LightSheVaultStatusTokens
        val darkTokens = DarkSheVaultStatusTokens

        for (state in SafetyState.values()) {
            val lightStyle = lightTokens.forState(state)
            val darkStyle = darkTokens.forState(state)

            assertNotNull("Light style for $state must exist", lightStyle)
            assertNotNull("Dark style for $state must exist", darkStyle)
            assertTrue("Label text for $state must not be blank", lightStyle.labelText.isNotBlank())
        }

        // Verify SOS emergency status uses SheVaultColors.emergency
        assertEquals(SheVaultColors.emergency, lightTokens.emergencySos.content)
        assertEquals(SheVaultColors.emergency, darkTokens.emergencySos.border)
    }

    @Test
    fun testSpacingGridConsistency() {
        val spacing = DefaultSheVaultSpacing
        assertEquals(0.dp, spacing.none)
        assertEquals(2.dp, spacing.xxs)
        assertEquals(4.dp, spacing.xs)
        assertEquals(8.dp, spacing.sm)
        assertEquals(12.dp, spacing.md)
        assertEquals(16.dp, spacing.lg)
        assertEquals(24.dp, spacing.xl)
        assertEquals(32.dp, spacing.xxl)
        assertEquals(48.dp, spacing.xxxl)
        assertEquals(64.dp, spacing.huge)
    }

    @Test
    fun testRadiusTokensMonotonic() {
        val radius = DefaultSheVaultRadius
        assertTrue(radius.xs < radius.sm)
        assertTrue(radius.sm < radius.md)
        assertTrue(radius.md < radius.lg)
        assertTrue(radius.lg < radius.xl)
        assertTrue(radius.xl < radius.xxl)
    }

    @Test
    fun testElevationTokensMonotonic() {
        val elevation = DefaultSheVaultElevation
        assertTrue(elevation.level0 < elevation.level1)
        assertTrue(elevation.level1 < elevation.level2)
        assertTrue(elevation.level2 < elevation.level3)
        assertTrue(elevation.level3 < elevation.level4)
        assertTrue(elevation.level4 < elevation.level5)
        assertTrue(elevation.level5 <= elevation.emergencySos)
    }

    @Test
    fun testIconRulesAccessibilityMinimum() {
        val iconRules = DefaultSheVaultIconRules
        assertEquals(48.dp, iconRules.minTouchTarget)
        assertTrue(iconRules.micro < iconRules.small)
        assertTrue(iconRules.small < iconRules.medium)
        assertTrue(iconRules.medium < iconRules.large)
        assertTrue(iconRules.large < iconRules.extraLarge)
        assertTrue(iconRules.extraLarge < iconRules.sosHero)
        assertEquals(iconRules.boldStroke, iconRules.boldStrokeWidth)
        assertEquals(iconRules.hairlineStroke, iconRules.hairlineStrokeWidth)
    }

    @Test
    fun testTypographyScaleDefinitions() {
        val typography = DefaultSheVaultTypography
        assertNotNull(typography.displayLarge)
        assertNotNull(typography.headlineLarge)
        assertNotNull(typography.bodyLarge)
        assertNotNull(typography.sosCounter)
        assertNotNull(typography.alertBanner)
    }
}
