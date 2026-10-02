package com.shevault.core.common.movement

/**
 * High-level user mobility classifications used to drive adaptive telemetry.
 *
 * Implements Task 15 requirement:
 * Stationary, Walking, Running, Vehicle, Unknown.
 */
enum class MovementState {
    STATIONARY,
    WALKING,
    RUNNING,
    VEHICLE,
    UNKNOWN
}
