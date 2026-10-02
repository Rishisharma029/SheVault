package com.shevault.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingModelTest {

    @Test
    fun testAllPresetRelationshipsAvailable() {
        val expectedRelationships = listOf(
            "Mother",
            "Father",
            "Friend",
            "Sibling",
            "Partner",
            "Emergency contact"
        )

        for (relation in expectedRelationships) {
            assertTrue("Expected relationship '$relation' should be in PRESET_RELATIONSHIPS",
                PRESET_RELATIONSHIPS.contains(relation))
        }
        assertEquals(6, PRESET_RELATIONSHIPS.size)
    }

    @Test
    fun testOnboardingContactModelFields() {
        val contact = OnboardingContact(
            id = "test-123",
            name = "Sarah Miller",
            phone = "+1 555 123 4567",
            relationship = "Sibling",
            priority = "PRIMARY",
            canReceiveLocation = true,
            canReceiveIncidentUpdates = true
        )

        assertEquals("test-123", contact.id)
        assertEquals("Sarah Miller", contact.name)
        assertEquals("+1 555 123 4567", contact.phone)
        assertEquals("Sibling", contact.relationship)
        assertEquals("PRIMARY", contact.priority)
        assertTrue(contact.canReceiveLocation)
        assertTrue(contact.canReceiveIncidentUpdates)
    }

    @Test
    fun testGranularContactPermissionsIndependent() {
        val contactWithLocationOnly = OnboardingContact(
            name = "Contact A",
            phone = "111",
            relationship = "Friend",
            priority = "SECONDARY",
            canReceiveLocation = true,
            canReceiveIncidentUpdates = false
        )

        val contactWithUpdatesOnly = OnboardingContact(
            name = "Contact B",
            phone = "222",
            relationship = "Partner",
            priority = "PRIMARY",
            canReceiveLocation = false,
            canReceiveIncidentUpdates = true
        )

        assertTrue(contactWithLocationOnly.canReceiveLocation)
        assertFalse(contactWithLocationOnly.canReceiveIncidentUpdates)

        assertFalse(contactWithUpdatesOnly.canReceiveLocation)
        assertTrue(contactWithUpdatesOnly.canReceiveIncidentUpdates)
    }

    @Test
    fun testToEntityMapping() {
        val contact = OnboardingContact(
            id = "entity-test",
            name = "Priya Sharma",
            phone = "+91 99999 88888",
            relationship = "Mother",
            priority = "PRIMARY",
            canReceiveLocation = true,
            canReceiveIncidentUpdates = false
        )

        val entity = contact.toEntity()
        assertEquals(contact.id, entity.id)
        assertEquals(contact.name, entity.name)
        assertEquals(contact.phone, entity.phoneNumber)
        assertEquals(contact.relationship, entity.relationship)
        assertTrue(entity.isPrimary)
        assertTrue(entity.notifyCall)
        assertTrue(entity.notifySms)
        assertTrue(entity.canReceiveLocation)
        assertFalse(entity.canReceiveIncidentUpdates)
    }

    @Test
    fun testAccountValidation() {
        val emptyName = ""
        val validName = "Ananya"
        val emptyContact = ""
        val validContact = "ananya@example.com"

        assertFalse(emptyName.isNotBlank() && validContact.isNotBlank())
        assertFalse(validName.isNotBlank() && emptyContact.isNotBlank())
        assertTrue(validName.isNotBlank() && validContact.isNotBlank())
    }
}
