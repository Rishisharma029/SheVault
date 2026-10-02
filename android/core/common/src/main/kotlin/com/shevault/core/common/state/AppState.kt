package com.shevault.core.common.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ProtectionState {
    PROTECTED,
    LIMITED,
    UNAVAILABLE
}

enum class GlobalIncidentState {
    IDLE,
    STARTING,
    ACTIVE_GRACE,
    ESCALATING,
    ESCALATED,
    CANCELLED,
    DURESS,
    UNKNOWN,
    ENDED
}

enum class ConnectivityState {
    ONLINE,
    OFFLINE
}

enum class MovementState {
    STATIONARY,
    WALKING,
    RUNNING,
    VEHICLE,
    UNKNOWN
}

data class AppState(
    val protectionState: ProtectionState = ProtectionState.PROTECTED,
    val incidentState: GlobalIncidentState = GlobalIncidentState.IDLE,
    val connectivityState: ConnectivityState = ConnectivityState.ONLINE,
    val batteryPercent: Int = 88,
    val isBatteryCharging: Boolean = false,
    val movementState: MovementState = MovementState.STATIONARY,
    val isDiscreetMode: Boolean = false,
    val activeIncidentId: String? = null,
    val activeCheckInDestination: String? = null,
    val isCheckInActive: Boolean = false
)

object GlobalStateManager {
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    fun updateState(transform: (AppState) -> AppState) {
        _appState.update(transform)
    }

    fun triggerSos() {
        _appState.update {
            it.copy(
                incidentState = GlobalIncidentState.ACTIVE_GRACE,
                activeIncidentId = "INC-${System.currentTimeMillis() % 10000}"
            )
        }
    }

    fun safeCancel() {
        _appState.update {
            it.copy(
                incidentState = GlobalIncidentState.CANCELLED,
                activeIncidentId = null
            )
        }
    }

    fun duressCancel() {
        _appState.update {
            it.copy(
                incidentState = GlobalIncidentState.DURESS,
                activeIncidentId = null
            )
        }
    }

    fun resetToIdle() {
        _appState.update {
            it.copy(
                incidentState = GlobalIncidentState.IDLE,
                activeIncidentId = null
            )
        }
    }
}
