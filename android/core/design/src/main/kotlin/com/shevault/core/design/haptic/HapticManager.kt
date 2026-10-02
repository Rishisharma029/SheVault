package com.shevault.core.design.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * HapticFeedbackTypes required by section 43 of SheVault specification.
 */
enum class HapticFeedbackType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    WARNING,
    ERROR
}

/**
 * HapticManager: Encapsulates platform vibration calls so UI components
 * never call low-level vibrator APIs directly.
 */
class HapticManager(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun performHaptic(type: HapticFeedbackType) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticFeedbackType.LIGHT -> VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticFeedbackType.MEDIUM -> VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticFeedbackType.HEAVY -> VibrationEffect.createOneShot(100L, 255)
                HapticFeedbackType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 40), -1)
                HapticFeedbackType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 60), -1)
                HapticFeedbackType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 70, 100, 70, 100), -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticFeedbackType.LIGHT -> vibrator.vibrate(20L)
                HapticFeedbackType.MEDIUM -> vibrator.vibrate(45L)
                HapticFeedbackType.HEAVY -> vibrator.vibrate(100L)
                HapticFeedbackType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 30, 60, 40), -1)
                HapticFeedbackType.WARNING -> vibrator.vibrate(longArrayOf(0, 60, 80, 60), -1)
                HapticFeedbackType.ERROR -> vibrator.vibrate(longArrayOf(0, 100, 70, 100), -1)
            }
        }
    }
}

@Composable
fun rememberHapticManager(): HapticManager {
    val context = LocalContext.current
    return remember(context) { HapticManager(context) }
}
