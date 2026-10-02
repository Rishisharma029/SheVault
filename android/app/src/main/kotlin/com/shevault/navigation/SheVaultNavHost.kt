package com.shevault.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shevault.core.design.components.SheVaultBottomNav
import com.shevault.feature.disretmode.DiscreteModeScreen
import com.shevault.feature.history.HistoryScreen
import com.shevault.feature.history.IncidentDetailScreen
import com.shevault.feature.home.CheckInScreen
import com.shevault.feature.home.HomeScreen
import com.shevault.feature.incident.IncidentScreen
import com.shevault.feature.incident.ui.CancellationFlowScreen
import com.shevault.feature.onboarding.OnboardingScreen
import com.shevault.feature.saferoute.SafeRouteScreen
import com.shevault.feature.settings.AboutScreen
import com.shevault.feature.settings.AccountSettingsScreen
import com.shevault.feature.settings.DeveloperSimulatorScreen
import com.shevault.feature.settings.NotificationSettingsScreen
import com.shevault.feature.settings.PermissionsSettingsScreen
import com.shevault.feature.settings.PrivacySettingsScreen
import com.shevault.feature.settings.SecuritySettingsScreen
import com.shevault.feature.settings.SettingsScreen
import com.shevault.feature.sos.SosScreen
import com.shevault.feature.trustedcircle.AddContactScreen
import com.shevault.feature.trustedcircle.TrustedCircleScreen

/**
 * 20 Distinct App Routes as required by section 2 of the specification
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Sos : Screen("sos")
    data object Incident : Screen("incident")
    data object Cancel : Screen("cancel")
    data object Duress : Screen("duress")
    data object TrustedCircle : Screen("trusted-circle")
    data object AddContact : Screen("add-contact")
    data object SafeRoute : Screen("safe-route")
    data object CheckIn : Screen("check-in")
    data object History : Screen("history")
    data object HistoryDetail : Screen("history/{id}") {
        fun createRoute(id: String) = "history/$id"
    }
    data object Discreet : Screen("discreet")
    data object Settings : Screen("settings")
    data object SettingsAccount : Screen("settings/account")
    data object SettingsPrivacy : Screen("settings/privacy")
    data object SettingsPermissions : Screen("settings/permissions")
    data object SettingsSecurity : Screen("settings/security")
    data object SettingsNotifications : Screen("settings/notifications")
    data object About : Screen("about")

    // Developer tool route
    data object DevSimulator : Screen("dev-simulator")
}

@Composable
fun SheVaultNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Onboarding.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Section 3: Active incident mode and discreet mode should NOT show normal bottom navigation
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.TrustedCircle.route,
        "trustedcircle",
        Screen.History.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val selectedTab = when (currentRoute) {
                    Screen.TrustedCircle.route, "trustedcircle" -> "trustedcircle"
                    Screen.History.route -> "history"
                    Screen.Settings.route -> "settings"
                    else -> "home"
                }
                SheVaultBottomNav(
                    currentRoute = selectedTab,
                    onNavigate = { route ->
                        when (route) {
                            "home" -> navController.navigate(Screen.Home.route)
                            "trustedcircle" -> navController.navigate(Screen.TrustedCircle.route)
                            "history" -> navController.navigate(Screen.History.route)
                            "settings" -> navController.navigate(Screen.Settings.route)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSos = { navController.navigate(Screen.Sos.route) },
                    onNavigateToSafeRoute = { navController.navigate(Screen.SafeRoute.route) },
                    onNavigateToTrustedCircle = { navController.navigate(Screen.TrustedCircle.route) },
                    onNavigateToDiscreet = { navController.navigate(Screen.Discreet.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToProfile = { navController.navigate(Screen.SettingsAccount.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToCheckIn = { navController.navigate(Screen.CheckIn.route) }
                )
            }

            composable(Screen.Sos.route) {
                SosScreen(
                    onCancelSos = { navController.navigate(Screen.Cancel.route) },
                    onSosConfirmed = { navController.navigate(Screen.Incident.route) }
                )
            }

            composable(Screen.Incident.route) {
                IncidentScreen(
                    onNavigateToCancel = { navController.navigate(Screen.Cancel.route) },
                    onContinueProtection = { navController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.Cancel.route) {
                CancellationFlowScreen(
                    onDismissToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onContinueProtection = { navController.popBackStack() }
                )
            }

            composable(Screen.Duress.route) {
                CancellationFlowScreen(
                    onDismissToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onContinueProtection = { navController.popBackStack() }
                )
            }

            composable(Screen.TrustedCircle.route) {
                TrustedCircleScreen(
                    onNavigateToAddContact = { navController.navigate(Screen.AddContact.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddContact.route) {
                AddContactScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SafeRoute.route) {
                SafeRouteScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CheckIn.route) {
                CheckInScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.HistoryDetail.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.HistoryDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: "SV-9F82"
                IncidentDetailScreen(
                    incidentId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Discreet.route) {
                DiscreteModeScreen(
                    onExitDiscreetMode = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToAccount = { navController.navigate(Screen.SettingsAccount.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.SettingsPrivacy.route) },
                    onNavigateToPermissions = { navController.navigate(Screen.SettingsPermissions.route) },
                    onNavigateToSecurity = { navController.navigate(Screen.SettingsSecurity.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.SettingsNotifications.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToDevSimulator = { navController.navigate(Screen.DevSimulator.route) }
                )
            }

            composable(Screen.SettingsAccount.route) {
                AccountSettingsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.SettingsPrivacy.route) {
                PrivacySettingsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.SettingsPermissions.route) {
                PermissionsSettingsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.SettingsSecurity.route) {
                SecuritySettingsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.SettingsNotifications.route) {
                NotificationSettingsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.About.route) {
                AboutScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.DevSimulator.route) {
                DeveloperSimulatorScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
