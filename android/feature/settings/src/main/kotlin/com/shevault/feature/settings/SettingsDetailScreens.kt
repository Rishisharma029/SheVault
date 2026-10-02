package com.shevault.feature.settings

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
import com.shevault.core.design.components.SheVaultPrimaryButton
import com.shevault.core.design.components.SheVaultSecondaryButton
import com.shevault.core.design.theme.SheVaultTheme

@Composable
fun AccountSettingsScreen(onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("Rishi Sharma") }
    var phone by remember { mutableStateOf("+91 98765 43210") }
    var email by remember { mutableStateOf("rishi.sharma@example.com") }

    SettingsScaffold(title = "Account Settings", onNavigateBack = onNavigateBack) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        SheVaultPrimaryButton(text = "Save Profile", onClick = onNavigateBack, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun PrivacySettingsScreen(onNavigateBack: () -> Unit) {
    SettingsScaffold(title = "Privacy", onNavigateBack = onNavigateBack) {
        Text("Your Privacy", style = SheVaultTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("• Location: Used exclusively during active emergency sessions and journey monitoring.", style = SheVaultTheme.typography.bodySmall)
        Text("• Incident Data: Stored locally in encrypted Room database with optional offline-first buffering.", style = SheVaultTheme.typography.bodySmall)
        Text("• Trusted Contacts: Only nominated contacts receive SMS coordinates and incident updates.", style = SheVaultTheme.typography.bodySmall)
        Text("• Retention: Local forensic records are retained for 90 days then pruned automatically.", style = SheVaultTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(24.dp))
        SheVaultSecondaryButton(text = "Purge Local Incident Logs", onClick = { /* Clear DB */ }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun PermissionsSettingsScreen(onNavigateBack: () -> Unit) {
    SettingsScaffold(title = "Permissions", onNavigateBack = onNavigateBack) {
        PermissionItem("Location Access", "✓ Allowed (Fine GPS & Fused Provider)", true)
        PermissionItem("Push Notifications", "✓ Allowed (Critical Alert Channel)", true)
        PermissionItem("Microphone (Audio Evidence)", "Disabled (User opt-in only)", false)
        PermissionItem("Battery Optimization Exemption", "Needs Review", false)
        Spacer(modifier = Modifier.height(16.dp))
        SheVaultSecondaryButton(text = "Open System App Settings", onClick = { /* Intent */ }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun SecuritySettingsScreen(onNavigateBack: () -> Unit) {
    var biometricEnabled by remember { mutableStateOf(true) }

    SettingsScaffold(title = "Security", onNavigateBack = onNavigateBack) {
        Text("Authentication Credentials", style = SheVaultTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        SheVaultSecondaryButton(text = "Change Safe PIN (Current: ● ● ● ●)", onClick = {}, modifier = Modifier.fillMaxWidth())
        SheVaultSecondaryButton(text = "Change Duress PIN (Current: ● ● ● ●)", onClick = {}, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Biometric Authentication", style = SheVaultTheme.typography.bodyMedium)
            Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
        }
    }
}

@Composable
fun NotificationSettingsScreen(onNavigateBack: () -> Unit) {
    var alerts by remember { mutableStateOf(true) }
    var reminders by remember { mutableStateOf(true) }
    var circleUpdates by remember { mutableStateOf(true) }

    SettingsScaffold(title = "Notifications", onNavigateBack = onNavigateBack) {
        NotificationToggle("Safety Alerts", alerts) { alerts = it }
        NotificationToggle("Check-In Reminders", reminders) { reminders = it }
        NotificationToggle("Trusted Circle Updates", circleUpdates) { circleUpdates = it }
    }
}

@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    SettingsScaffold(title = "About SheVault", onNavigateBack = onNavigateBack) {
        Text("SheVault v1.0.0", style = SheVaultTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Native Android-First Safety Network & Guardian Command Hub", style = SheVaultTheme.typography.bodyMedium, color = SheVaultTheme.colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Built strictly complying with Android 14 Foreground Service requirements (Location, Microphone, DataSync) and Google Material 3 tokens.", style = SheVaultTheme.typography.bodySmall)
    }
}

@Composable
private fun SettingsScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(SheVaultTheme.colors.background).padding(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title, style = SheVaultTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            content()
            Spacer(modifier = Modifier.height(16.dp))
            SheVaultSecondaryButton(text = "Back", onClick = onNavigateBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PermissionItem(title: String, status: String, granted: Boolean) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(title, style = SheVaultTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(status, style = SheVaultTheme.typography.bodySmall, color = if (granted) SheVaultTheme.colors.safe else SheVaultTheme.colors.warning)
    }
}

@Composable
private fun NotificationToggle(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = SheVaultTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
