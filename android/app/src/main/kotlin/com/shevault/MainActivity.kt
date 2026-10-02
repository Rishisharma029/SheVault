package com.shevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shevault.core.design.theme.SheVaultTheme
import com.shevault.navigation.SheVaultNavHost

/**
 * MainActivity: Thin entrypoint into the modular Jetpack Compose application.
 * All feature logic and navigation live in dedicated modular components.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SheVaultTheme {
                SheVaultNavHost()
            }
        }
    }
}
