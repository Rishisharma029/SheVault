package com.shevault.feature.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSafetyStateTest {

    @Test
    fun testAllRequiredHomeStatesExist() {
        val requiredStates = listOf(
            "HOME_PROTECTED",
            "HOME_SETUP_REQUIRED",
            "HOME_PARTIAL_PROTECTION",
            "HOME_OFFLINE",
            "HOME_PERMISSION_WARNING"
        )

        for (stateName in requiredStates) {
            val state = HomeSafetyState.valueOf(stateName)
            assertNotNull("State $stateName must exist in HomeSafetyState enum", state)
        }
        assertEquals("HomeSafetyState must contain exactly 5 states", 5, HomeSafetyState.values().size)
    }

    @Test
    fun testSetupRequiredWhenSetupIncompleteOrNoContacts() {
        // If setup is not complete
        val stateIncomplete = HomeSafetyStateResolver.resolve(
            isSetupComplete = false,
            hasTrustedContacts = true,
            hasLocationPermission = true,
            hasNotificationPermission = true,
            isOnline = true
        )
        assertEquals(HomeSafetyState.HOME_SETUP_REQUIRED, stateIncomplete)

        // If trusted circle has no contacts
        val stateNoContacts = HomeSafetyStateResolver.resolve(
            isSetupComplete = true,
            hasTrustedContacts = false,
            hasLocationPermission = true,
            hasNotificationPermission = true,
            isOnline = true
        )
        assertEquals(HomeSafetyState.HOME_SETUP_REQUIRED, stateNoContacts)

        // If both setup incomplete and no contacts
        val stateBothIncomplete = HomeSafetyStateResolver.resolve(
            isSetupComplete = false,
            hasTrustedContacts = false,
            hasLocationPermission = true,
            hasNotificationPermission = true,
            isOnline = true
        )
        assertEquals(HomeSafetyState.HOME_SETUP_REQUIRED, stateBothIncomplete)
    }

    @Test
    fun testNeverFalselyStateProtectedWhenLocationRevoked() {
        val resolvedState = HomeSafetyStateResolver.resolve(
            isSetupComplete = true,
            hasTrustedContacts = true,
            hasLocationPermission = false,
            hasNotificationPermission = true,
            isOnline = true
        )

        assertFalse("Must never state HOME_PROTECTED when location is revoked", resolvedState == HomeSafetyState.HOME_PROTECTED)
        assertEquals("Must report HOME_PARTIAL_PROTECTION when location is revoked", HomeSafetyState.HOME_PARTIAL_PROTECTION, resolvedState)
    }

    @Test
    fun testCriticalPermissionWarningResolution() {
        val resolvedState = HomeSafetyStateResolver.resolve(
            isSetupComplete = true,
            hasTrustedContacts = true,
            hasLocationPermission = false,
            hasNotificationPermission = false,
            isOnline = true
        )

        assertEquals("Must report HOME_PERMISSION_WARNING when critical permissions missing", HomeSafetyState.HOME_PERMISSION_WARNING, resolvedState)
    }

    @Test
    fun testOfflineStateResolution() {
        val resolvedState = HomeSafetyStateResolver.resolve(
            isSetupComplete = true,
            hasTrustedContacts = true,
            hasLocationPermission = true,
            hasNotificationPermission = true,
            isOnline = false
        )

        assertEquals(HomeSafetyState.HOME_OFFLINE, resolvedState)
    }

    @Test
    fun testProtectedStateOnlyWhenAllRequirementsMet() {
        val resolvedState = HomeSafetyStateResolver.resolve(
            isSetupComplete = true,
            hasTrustedContacts = true,
            hasLocationPermission = true,
            hasNotificationPermission = true,
            isOnline = true
        )

        assertEquals(HomeSafetyState.HOME_PROTECTED, resolvedState)
    }
}
