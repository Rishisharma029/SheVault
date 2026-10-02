package com.shevault.feature.trustedcircle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.common.mock.MockContact
import com.shevault.core.common.mock.MockRepositories
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.theme.SheVaultTheme
import java.util.UUID

/**
 * Section 21: Add Contact Screen with granular permissions
 */
@Composable
fun AddContactScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var alertEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }
    var batteryEnabled by remember { mutableStateOf(true) }
    var evidenceEnabled by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Trusted Contact",
                style = SheVaultTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = relationship,
                onValueChange = { relationship = it },
                label = { Text("Relationship (e.g. Sister, Friend, Partner)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Permissions for this contact",
                style = SheVaultTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SheVaultTheme.colors.onSurface
            )

            PermissionToggleRow("Incident alerts", alertEnabled) { alertEnabled = it }
            PermissionToggleRow("Location sharing", locationEnabled) { locationEnabled = it }
            PermissionToggleRow("Battery level status", batteryEnabled) { batteryEnabled = it }
            PermissionToggleRow("Audio evidence access", evidenceEnabled) { evidenceEnabled = it }

            Spacer(modifier = Modifier.height(16.dp))

            SheVaultPrimaryButton(
                text = "Save Contact",
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        MockRepositories.addContact(
                            MockContact(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                relationship = relationship.ifBlank { "Guardian" },
                                phone = phone,
                                priority = MockRepositories.contacts.value.size + 1,
                                alertEnabled = alertEnabled,
                                locationEnabled = locationEnabled,
                                batteryEnabled = batteryEnabled,
                                evidenceEnabled = evidenceEnabled
                            )
                        )
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SheVaultTheme.colors.primary,
                checkedTrackColor = SheVaultTheme.colors.primaryContainer
            )
        )
    }
}
