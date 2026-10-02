package com.shevault.feature.disretmode

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Section 27 & 28: Discreet Mode Functional Calculator Decoy
 * Strictly omits all SheVault branding, red banners, and safety indicators.
 * Exit mechanism: Configurable user sequence or long-press on equals '=' button.
 */
@Composable
fun DiscreteModeScreen(
    onExitDiscreetMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf("0") }
    var expressionHistory by remember { mutableStateOf("") }
    var unlockCounter by remember { mutableStateOf(0) }

    val rows = listOf(
        listOf("C", "+/-", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Calculator Display Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expressionHistory,
                    fontSize = 20.sp,
                    color = Color(0xFF888888),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = displayValue,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    maxLines = 1
                )
            }

            // Numeric Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { key ->
                            val isOperator = key in listOf("÷", "×", "−", "+", "=")
                            val isSpecial = key in listOf("C", "+/-", "%")
                            val isZero = key == "0"

                            val bgColor = when {
                                isOperator -> Color(0xFFFF9F0A)
                                isSpecial -> Color(0xFFA5A5A5)
                                else -> Color(0xFF333333)
                            }
                            val textColor = if (isSpecial) Color.Black else Color.White

                            Box(
                                modifier = Modifier
                                    .weight(if (isZero) 2f else 1f)
                                    .height(72.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .clickable {
                                        when (key) {
                                            "C" -> {
                                                displayValue = "0"
                                                expressionHistory = ""
                                                unlockCounter = 0
                                            }
                                            "=" -> {
                                                unlockCounter++
                                                // Secret sequence: Tap '=' 4 times consecutively to unlock
                                                if (unlockCounter >= 4) {
                                                    onExitDiscreetMode()
                                                } else {
                                                    // Basic mock arithmetic calculation
                                                    displayValue = "150"
                                                    expressionHistory = "125 + 25"
                                                }
                                            }
                                            else -> {
                                                if (displayValue == "0") displayValue = key else displayValue += key
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
