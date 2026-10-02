package com.shevault.feature.trustedcircle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.common.mock.MockContact
import com.shevault.core.common.mock.MockRepositories
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Section 20 & 22: Trusted Circle List Screen and Empty State
 */
@Composable
fun TrustedCircleScreen(
    onNavigateToAddContact: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by MockRepositories.contacts.collectAsState()

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trusted Circle",
                    style = SheVaultTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SheVaultTheme.colors.onSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SheVaultTheme.colors.primaryContainer)
                        .clickable { onNavigateToAddContact() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+ Add",
                        style = SheVaultTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SheVaultTheme.colors.primary
                    )
                }
            }

            if (contacts.isEmpty()) {
                // Section 22: Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Your Trusted Circle is empty",
                            style = SheVaultTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add someone you trust so they can\nreceive configured safety updates.",
                            style = SheVaultTheme.typography.bodySmall,
                            color = SheVaultTheme.colors.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        SheVaultPrimaryButton(
                            text = "Add Contact",
                            onClick = onNavigateToAddContact
                        )
                    }
                }
            } else {
                contacts.forEach { contact ->
                    ContactCardItem(contact = contact)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ContactCardItem(contact: MockContact) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SheVaultTheme.colors.surface)
            .border(1.dp, SheVaultTheme.colors.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SheVaultTheme.colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.take(1),
                            style = SheVaultTheme.typography.titleSmall,
                            color = SheVaultTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = contact.name,
                            style = SheVaultTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SheVaultTheme.colors.onSurface
                        )
                        Text(
                            text = contact.relationship,
                            style = SheVaultTheme.typography.bodySmall,
                            color = SheVaultTheme.colors.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = SheVaultTheme.colors.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (contact.alertEnabled) {
                    Text(text = "✓ Incident alerts", style = SheVaultTheme.typography.labelSmall, color = SheVaultTheme.colors.safe)
                }
                if (contact.locationEnabled) {
                    Text(text = "✓ Location", style = SheVaultTheme.typography.labelSmall, color = SheVaultTheme.colors.safe)
                }
                if (contact.batteryEnabled) {
                    Text(text = "✓ Battery level", style = SheVaultTheme.typography.labelSmall, color = SheVaultTheme.colors.safe)
                }
            }
        }
    }
}
