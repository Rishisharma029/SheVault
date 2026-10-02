package com.shevault.core.common.incident

/**
 * Standard timeline event types for auditable local incident journals.
 *
 * Implements Task 12 requirement:
 * Provides an auditable incident timeline capturing critical system lifecycle events.
 */
object IncidentTimelineEventType {
    const val INCIDENT_CREATED = "INCIDENT_CREATED"
    const val LOCATION_CAPTURED = "LOCATION_CAPTURED"
    const val NETWORK_LOST = "NETWORK_LOST"
    const val NETWORK_RESTORED = "NETWORK_RESTORED"
    const val ESCALATION_ATTEMPT = "ESCALATION_ATTEMPT"
    const val ACK_RECEIVED = "ACK_RECEIVED"
    const val SAFE_CANCEL = "SAFE_CANCEL"
    const val DURESS_CANCEL = "DURESS_CANCEL"
    const val INCIDENT_ENDED = "INCIDENT_ENDED"
}
