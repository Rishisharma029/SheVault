package com.shevault.core.common.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MockIncident(
    val id: String,
    val title: String,
    val date: String,
    val status: String,
    val method: String,
    val lat: Double = 28.6139,
    val lng: Double = 77.2090,
    val accuracyMeters: Float = 6f
)

data class MockContact(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String,
    val priority: Int,
    val alertEnabled: Boolean = true,
    val locationEnabled: Boolean = true,
    val batteryEnabled: Boolean = true,
    val evidenceEnabled: Boolean = false
)

object MockRepositories {
    val incidents = MutableStateFlow(
        listOf(
            MockIncident("SV-9F82", "SOS Session", "Oct 2, 2026", "Cancelled by user", "SOS Hold (1.4s)"),
            MockIncident("SV-8841", "Safety Check-In", "Sep 26, 2026", "Completed", "Arrival Verified"),
            MockIncident("SV-7612", "Route Monitoring", "Sep 21, 2026", "Completed", "Safe Arrival")
        )
    )

    val contacts = MutableStateFlow(
        listOf(
            MockContact("c1", "Mom", "Mother", "+91 98765 00001", 1),
            MockContact("c2", "Dad", "Father", "+91 98765 00002", 2),
            MockContact("c3", "Priya", "Friend", "+91 98765 00003", 3)
        )
    )

    fun addContact(contact: MockContact) {
        contacts.value = contacts.value + contact
    }
}
