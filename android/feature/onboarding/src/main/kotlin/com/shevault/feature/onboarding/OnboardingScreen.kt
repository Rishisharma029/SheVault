package com.shevault.feature.onboarding

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.design.components.PINIndicator
import com.shevault.core.design.components.PINPad
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.components.SheVaultSecondaryButton
import com.shevault.core.design.theme.SheVaultTheme

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    // User profile state
    var userName by remember { mutableStateOf("Rishi Sharma") }
    var userPhone by remember { mutableStateOf("+91 98765 43210") }

    // Trusted contacts state
    val contactsList = remember {
        mutableStateListOf(
            "Mom (+91 98765 00001) - Mother",
            "Dad (+91 98765 00002) - Father"
        )
    }

    // Permission states
    var locationConfigured by remember { mutableStateOf(true) }
    var notificationsConfigured by remember { mutableStateOf(true) }
    var micConfigured by remember { mutableStateOf(false) }

    // Security PIN setup state
    var safePin by remember { mutableStateOf("1234") }
    var duressPin by remember { mutableStateOf("9999") }
    var pinSetupStep by remember { mutableIntStateOf(1) } // 1: Safe PIN, 2: Duress PIN
    var currentPinInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheVaultTheme.colors.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SheVault Setup",
                    style = SheVaultTheme.typography.labelLarge,
                    color = SheVaultTheme.colors.onSurfaceVariant
                )
                Text(
                    text = "Step $step of 7",
                    style = SheVaultTheme.typography.labelSmall,
                    color = SheVaultTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Screen content switcher
            AnimatedContent(targetState = step, label = "OnboardingStep") { targetStep ->
                when (targetStep) {
                    1 -> WelcomeStepView()
                    2 -> HowItWorksStepView()
                    3 -> ProfileStepView(
                        name = userName,
                        onNameChange = { userName = it },
                        phone = userPhone,
                        onPhoneChange = { userPhone = it }
                    )
                    4 -> TrustedCircleStepView(
                        contacts = contactsList,
                        onAddMockContact = { contactsList.add("Sister (+91 98765 00003) - Sibling") }
                    )
                    5 -> PermissionsStepView(
                        locationOk = locationConfigured,
                        onToggleLocation = { locationConfigured = !locationConfigured },
                        notifOk = notificationsConfigured,
                        onToggleNotif = { notificationsConfigured = !notificationsConfigured },
                        micOk = micConfigured,
                        onToggleMic = { micConfigured = !micConfigured }
                    )
                    6 -> SecurityPinStepView(
                        pinStep = pinSetupStep,
                        enteredPin = currentPinInput,
                        onDigitClick = { if (currentPinInput.length < 4) currentPinInput += it },
                        onBackspace = { if (currentPinInput.isNotEmpty()) currentPinInput = currentPinInput.dropLast(1) },
                        onPinConfirmed = {
                            if (pinSetupStep == 1) {
                                safePin = currentPinInput
                                currentPinInput = ""
                                pinSetupStep = 2
                            } else {
                                duressPin = currentPinInput
                                step = 7
                            }
                        }
                    )
                    7 -> ReadyStepView()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom action buttons
            if (step != 6) {
                SheVaultPrimaryButton(
                    text = if (step == 1) "Get Started" else if (step == 7) "Enter SheVault" else "Continue",
                    onClick = {
                        if (step < 7) {
                            step++
                        } else {
                            onFinishOnboarding()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WelcomeStepView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(SheVaultTheme.colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SheVaultTheme.colors.primary,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "SheVault",
            style = SheVaultTheme.typography.headlineLarge,
            color = SheVaultTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your safety network,\nalways within reach.",
            style = SheVaultTheme.typography.titleMedium,
            color = SheVaultTheme.colors.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun HowItWorksStepView() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "How SheVault works",
            style = SheVaultTheme.typography.headlineSmall,
            color = SheVaultTheme.colors.onSurface
        )

        StepCard(number = "01", title = "Activate", desc = "Hold SOS when you need immediate help.")
        StepCard(number = "02", title = "Stay Connected", desc = "Your trusted circle can receive live safety updates.")
        StepCard(number = "03", title = "Stay Discreet", desc = "Use discreet mode when safety visibility matters.")
    }
}

@Composable
private fun StepCard(number: String, title: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SheVaultTheme.colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, SheVaultTheme.colors.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = number,
                style = SheVaultTheme.typography.titleLarge,
                color = SheVaultTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = SheVaultTheme.typography.titleMedium,
                    color = SheVaultTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    style = SheVaultTheme.typography.bodySmall,
                    color = SheVaultTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileStepView(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Your profile",
            style = SheVaultTheme.typography.headlineSmall,
            color = SheVaultTheme.colors.onSurface
        )
        Text(
            text = "Used to identify you to your guardians in an emergency.",
            style = SheVaultTheme.typography.bodyMedium,
            color = SheVaultTheme.colors.onSurfaceVariant
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TrustedCircleStepView(
    contacts: List<String>,
    onAddMockContact: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Add people you trust",
            style = SheVaultTheme.typography.headlineSmall,
            color = SheVaultTheme.colors.onSurface
        )

        SheVaultSecondaryButton(
            text = "+ Add Contact",
            onClick = onAddMockContact,
            modifier = Modifier.fillMaxWidth()
        )

        if (contacts.isEmpty()) {
            Text(
                text = "No contacts yet",
                style = SheVaultTheme.typography.bodyMedium,
                color = SheVaultTheme.colors.onSurfaceVariant
            )
        } else {
            contacts.forEach { c ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SheVaultTheme.colors.surface, RoundedCornerShape(8.dp))
                        .border(1.dp, SheVaultTheme.colors.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(text = c, style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.onSurface)
                }
            }
        }
    }
}

@Composable
private fun PermissionsStepView(
    locationOk: Boolean,
    onToggleLocation: () -> Unit,
    notifOk: Boolean,
    onToggleNotif: () -> Unit,
    micOk: Boolean,
    onToggleMic: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Permissions",
            style = SheVaultTheme.typography.headlineSmall,
            color = SheVaultTheme.colors.onSurface
        )

        PermissionRow("Location", "Used for active safety sessions.", locationOk, onToggleLocation)
        PermissionRow("Notifications", "Used for safety updates.", notifOk, onToggleNotif)
        PermissionRow("Microphone", "Only used if you enable audio evidence.", micOk, onToggleMic)
    }
}

@Composable
private fun PermissionRow(
    title: String,
    desc: String,
    configured: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SheVaultTheme.colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, SheVaultTheme.colors.outlineVariant, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = SheVaultTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = desc, style = SheVaultTheme.typography.bodySmall, color = SheVaultTheme.colors.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            SheVaultSecondaryButton(
                text = if (configured) "Configured ✓" else "Set up",
                onClick = onToggle
            )
        }
    }
}

@Composable
private fun SecurityPinStepView(
    pinStep: Int,
    enteredPin: String,
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onPinConfirmed: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (pinStep == 1) "Create Safe PIN" else "Create Duress PIN",
            style = SheVaultTheme.typography.headlineSmall,
            color = SheVaultTheme.colors.onSurface
        )
        Text(
            text = if (pinStep == 1) {
                "Used to disarm and terminate safety sessions normally."
            } else {
                "Your Duress PIN appears to cancel a safety session while quietly signaling distress."
            },
            style = SheVaultTheme.typography.bodySmall,
            color = SheVaultTheme.colors.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        PINIndicator(length = 4, enteredCount = enteredPin.length)

        PINPad(
            onDigitClick = onDigitClick,
            onBackspaceClick = onBackspace
        )

        if (enteredPin.length == 4) {
            SheVaultPrimaryButton(
                text = "Confirm PIN",
                onClick = onPinConfirmed,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReadyStepView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SheVaultTheme.colors.safeContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SheVaultTheme.colors.safe,
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "You're ready.",
            style = SheVaultTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SheVaultTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✓ Trusted Circle configured", style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.safe)
            Text("✓ Safety PIN configured", style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.safe)
            Text("✓ Protection settings reviewed", style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.safe)
        }
    }
}
