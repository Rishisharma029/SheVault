package com.shevault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Trusted emergency contact entity.
 *
 * Implements Task 12 requirement:
 * TrustedContact: id, name, phoneNumber, relationship, isPrimary, notifySms, notifyCall, etc.
 */
@Entity(tableName = "trusted_contacts")
data class TrustedContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean,
    val notifySms: Boolean,
    val notifyCall: Boolean,
    val priority: String = "PRIMARY", // PRIMARY, SECONDARY, TERTIARY
    val canReceiveLocation: Boolean = true,
    val canReceiveIncidentUpdates: Boolean = true
)
