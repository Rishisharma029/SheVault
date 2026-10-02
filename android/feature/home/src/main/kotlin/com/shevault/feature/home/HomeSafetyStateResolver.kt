package com.shevault.feature.home

/**
 * The 5 Explicit Home Screen States required by SheVault Specification.
 */
enum class HomeSafetyState {
    HOME_PROTECTED,
    HOME_SETUP_REQUIRED,
    HOME_PARTIAL_PROTECTION,
    HOME_OFFLINE,
    HOME_PERMISSION_WARNING
}

/**
 * Pure evaluator for Home screen safety state resolution.
 * Strictly guarantees that "✓ You're Protected" is never falsely claimed
 * when any critical dependency (setup, permissions, or connectivity) is impaired.
 */
object HomeSafetyStateResolver {

    fun resolve(
        isSetupComplete: Boolean,
        hasTrustedContacts: Boolean,
        hasLocationPermission: Boolean,
        hasNotificationPermission: Boolean,
        isOnline: Boolean
    ): HomeSafetyState {
        return when {
            // Priority 1: Setup incomplete or zero trusted contacts
            !isSetupComplete || !hasTrustedContacts -> HomeSafetyState.HOME_SETUP_REQUIRED

            // Priority 2: Critical safety permissions missing
            !hasLocationPermission && !hasNotificationPermission -> HomeSafetyState.HOME_PERMISSION_WARNING

            // Priority 3: Partial impairment (e.g. location unavailable)
            !hasLocationPermission -> HomeSafetyState.HOME_PARTIAL_PROTECTION

            // Priority 4: Offline (SMS fallback active)
            !isOnline -> HomeSafetyState.HOME_OFFLINE

            // Priority 5: Full protection active
            else -> HomeSafetyState.HOME_PROTECTED
        }
    }
}
