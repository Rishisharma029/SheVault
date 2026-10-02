package com.shevault.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shevault.core.design.tokens.BottomNavTokens
import com.shevault.core.design.tokens.BrandTokens
import com.shevault.core.design.tokens.DarkNeutralTokens
import com.shevault.core.design.tokens.DarkSheVaultColorScheme
import com.shevault.core.design.tokens.DarkSheVaultStatusTokens
import com.shevault.core.design.tokens.DecoyCalculatorTokens
import com.shevault.core.design.tokens.DefaultSheVaultElevation
import com.shevault.core.design.tokens.DefaultSheVaultIconRules
import com.shevault.core.design.tokens.DefaultSheVaultRadius
import com.shevault.core.design.tokens.DefaultSheVaultSpacing
import com.shevault.core.design.tokens.DefaultSheVaultTypography
import com.shevault.core.design.tokens.DiscreteSheVaultColorScheme
import com.shevault.core.design.tokens.LightNeutralTokens
import com.shevault.core.design.tokens.LightSheVaultColorScheme
import com.shevault.core.design.tokens.LightSheVaultStatusTokens
import com.shevault.core.design.tokens.SafeTokens
import com.shevault.core.design.tokens.SafetyState
import com.shevault.core.design.tokens.SheVaultColors
import com.shevault.core.design.tokens.SheVaultSafetyStatus
import com.shevault.core.design.tokens.WarningTokens
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
    fun testMasterBrandTokens() {
        assertEquals("Plum Primary must be #6D2E5B", Color(0xFF6D2E5B), BrandTokens.primary)
        assertEquals("Plum PrimaryDark must be #4A1F3D", Color(0xFF4A1F3D), BrandTokens.primaryDark)
        assertEquals("Plum PrimaryTint must be #F1E5EF", Color(0xFFF1E5EF), BrandTokens.primaryTint)
        assertEquals("Rose Accent must be #C85C7B", Color(0xFFC85C7B), BrandTokens.rose)
        assertEquals("Peach Accent must be #F4B6A6", Color(0xFFF4B6A6), BrandTokens.peach)
    }

    @Test
    fun testLightNeutralTokens() {
        assertEquals("Light Background must be #FFF9F7", Color(0xFFFFF9F7), LightNeutralTokens.background)
        assertEquals("Light Surface must be #FFFFFF", Color(0xFFFFFFFF), LightNeutralTokens.surface)
        assertEquals("Light Text Primary must be #211D20", Color(0xFF211D20), LightNeutralTokens.textPrimary)
        // Rule: Strictly #5C5459, not #70686D
        assertEquals("Light Text Secondary must be #5C5459", Color(0xFF5C5459), LightNeutralTokens.textSecondary)
        assertEquals("Light Border must be #E7DEE3", Color(0xFFE7DEE3), LightNeutralTokens.border)
        assertEquals("Light Divider must be #EEE7EB", Color(0xFFEEE7EB), LightNeutralTokens.divider)
    }

    @Test
    fun testDarkNeutralTokens() {
        assertEquals("Dark Background must be #151116", Color(0xFF151116), DarkNeutralTokens.background)
        assertEquals("Dark Surface must be #211A21", Color(0xFF211A21), DarkNeutralTokens.surface)
        assertEquals("Dark Elevated must be #2B222A", Color(0xFF2B222A), DarkNeutralTokens.surfaceElevated)
        assertEquals("Dark Text Primary must be #F8F2F5", Color(0xFFF8F2F5), DarkNeutralTokens.textPrimary)
        assertEquals("Dark Text Secondary must be #C8BDC5", Color(0xFFC8BDC5), DarkNeutralTokens.textSecondary)
        assertEquals("Dark Border must be #3B3038", Color(0xFF3B3038), DarkNeutralTokens.border)
        assertEquals("Dark Divider must be #332933", Color(0xFF332933), DarkNeutralTokens.divider)
    }

    @Test
    fun testSafetySemanticTokens() {
        assertEquals("Safe Default must be #0F766E", Color(0xFF0F766E), SafeTokens.default)
        assertEquals("Safe Dark must be #0A5B55", Color(0xFF0A5B55), SafeTokens.dark)
        assertEquals("Safe Surface must be #E6F5F2", Color(0xFFE6F5F2), SafeTokens.surface)

        // Warning Default must be #B54708 (never amber #D9911E)
        assertEquals("Warning Default must be #B54708", Color(0xFFB54708), WarningTokens.default)
        assertEquals("Warning Surface must be #FFF4E5", Color(0xFFFFF4E5), WarningTokens.surface)
    }

    @Test
    fun testDecoyCalculatorTokens() {
        assertEquals(Color(0xFF101010), DecoyCalculatorTokens.background)
        assertEquals(Color(0xFF1C1C1C), DecoyCalculatorTokens.surface)
        assertEquals(Color(0xFF2A2A2A), DecoyCalculatorTokens.key)
        assertEquals(Color(0xFF3A3A3A), DecoyCalculatorTokens.keyPressed)
        assertEquals(Color(0xFFFFFFFF), DecoyCalculatorTokens.text)
        assertEquals(Color(0xFFAAAAAA), DecoyCalculatorTokens.textSecondary)
        assertEquals(Color(0xFFD0D0D0), DecoyCalculatorTokens.operator)
    }

    @Test
    fun testBottomNavTokens() {
        assertEquals(Color(0xFF706970), BottomNavTokens.lightInactive)
        assertEquals(Color(0xFF6D2E5B), BottomNavTokens.lightActive)
        assertEquals(Color(0xFFF1E5EF), BottomNavTokens.lightActivePill)
        assertEquals(Color(0xFFAFA3AC), BottomNavTokens.darkInactive)
        assertEquals(Color(0xFFC978A8), BottomNavTokens.darkActive)
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

        for (status in SheVaultSafetyStatus.values()) {
            val lightDesc = lightTokens.descriptorFor(status)
            val darkDesc = darkTokens.descriptorFor(status)

            assertNotNull("Light descriptor for $status must exist", lightDesc)
            assertNotNull("Dark descriptor for $status must exist", darkDesc)
            assertTrue(lightDesc.title.isNotBlank())
            assertTrue(lightDesc.description.isNotBlank())
            assertNotNull(lightDesc.icon)
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
