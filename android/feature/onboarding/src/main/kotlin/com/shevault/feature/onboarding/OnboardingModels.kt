package com.shevault.feature.onboarding

import com.shevault.core.database.entity.TrustedContactEntity
import java.util.UUID

val PRESET_RELATIONSHIPS = listOf(
    "Mother",
    "Father",
    "Friend",
    "Sibling",
    "Partner",
    "Emergency contact"
)

data class OnboardingContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val relationship: String,
    val priority: String = "PRIMARY",
    val canReceiveLocation: Boolean = true,
    val canReceiveIncidentUpdates: Boolean = true
) {
    fun toEntity(): TrustedContactEntity = TrustedContactEntity(
        id = id,
        name = name,
        phoneNumber = phone,
        relationship = relationship,
        isPrimary = priority == "PRIMARY",
        notifySms = true,
        notifyCall = true,
        priority = priority,
        canReceiveLocation = canReceiveLocation,
        canReceiveIncidentUpdates = canReceiveIncidentUpdates
    )
}
