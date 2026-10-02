package com.shevault.core.design.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shevault.core.design.theme.SheVaultTheme
import com.shevault.core.design.tokens.SheVaultColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * High-reliability SOS Emergency Lifecycle States.
 */
enum class SosState {
    IDLE,
    TOUCH_DOWN,
    HOLDING,
    HOLD_COMPLETE,
    PANIC_GESTURE,
    ACTIVATING,
    ACTIVE,
    CANCEL_PENDING,
    CANCELLED,
    ESCALATED,
    FAILED
}

/**
 * Dedicated SheVault SOS Component.
 *
 * Implements:
 * - 1.2–1.5s press-and-hold trigger (default 1350ms)
 * - ~300ms recontact tolerance (timer survives accidental brief touch breaks)
 * - Multi-tap panic gesture detection
 * - Multi-modal indicators: Red + SOS typography + Warning Icon + Progress Ring + Haptic feedback
 * - Comprehensive state visualization (IDLE, HOLDING, ACTIVE, CANCEL_PENDING, etc.)
 */
@Composable
fun SheVaultSosComponent(
    modifier: Modifier = Modifier,
    currentState: SosState = SosState.IDLE,
    holdDurationMs: Long = 1350L,
    recontactToleranceMs: Long = 300L,
    size: Dp = 170.dp,
    onStateChanged: (SosState) -> Unit = {},
    onSosTriggered: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var internalState by remember { mutableStateOf(currentState) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var holdElapsedMs by remember { mutableLongStateOf(0L) }
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTimestamp by remember { mutableLongStateOf(0L) }

    var isTouchActive by remember { mutableStateOf(false) }

    // Sync if caller sets external state (e.g. ACTIVE, CANCEL_PENDING, etc.)
    LaunchedEffect(currentState) {
        if (currentState != internalState) {
            internalState = currentState
        }
    }

    val currentOnSosTriggered by rememberUpdatedState(onSosTriggered)
    val currentOnStateChanged by rememberUpdatedState(onStateChanged)

    // Pulsing animation for active or holding states
    val infiniteTransition = rememberInfiniteTransition(label = "sos_halo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (internalState == SosState.ACTIVE || internalState == SosState.HOLDING || internalState == SosState.ACTIVATING) 1.18f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (internalState == SosState.ACTIVE) 650 else 1000,
                easing = SheVaultTheme.motion.standardEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale_anim"
    )

    var holdTimerJob by remember { mutableStateOf<Job?>(null) }
    var recontactGraceJob by remember { mutableStateOf<Job?>(null) }

    fun updateState(newState: SosState) {
        internalState = newState
        currentOnStateChanged(newState)
    }

    fun completeHoldAndTrigger() {
        holdTimerJob?.cancel()
        holdTimerJob = null
        recontactGraceJob?.cancel()
        recontactGraceJob = null
        updateState(SosState.HOLD_COMPLETE)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        coroutineScope.launch {
            delay(80L)
            updateState(SosState.ACTIVATING)
            delay(120L)
            updateState(SosState.ACTIVE)
            currentOnSosTriggered()
        }
    }

    fun onTouchDown() {
        isTouchActive = true

        // Cancel pending grace release if user re-touched within tolerance
        recontactGraceJob?.cancel()
        recontactGraceJob = null

        val now = System.currentTimeMillis()
        if (now - lastTapTimestamp < 500L) {
            tapCount += 1
            if (tapCount >= 3) {
                // Panic gesture: 3 rapid consecutive touches
                tapCount = 0
                holdTimerJob?.cancel()
                holdTimerJob = null
                updateState(SosState.PANIC_GESTURE)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                completeHoldAndTrigger()
                return
            }
        } else {
            tapCount = 1
        }
        lastTapTimestamp = now

        // If hold duration was already met while finger was in grace window and user re-contacted
        if (holdElapsedMs >= holdDurationMs) {
            holdProgress = 1f
            completeHoldAndTrigger()
            return
        }

        if (internalState == SosState.IDLE || internalState == SosState.CANCELLED || internalState == SosState.FAILED) {
            updateState(SosState.TOUCH_DOWN)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        if (holdTimerJob == null || !holdTimerJob!!.isActive) {
            holdTimerJob = coroutineScope.launch {
                updateState(SosState.HOLDING)
                val tickMs = 16L
                while (holdElapsedMs < holdDurationMs) {
                    delay(tickMs)
                    holdElapsedMs += tickMs
                    holdProgress = (holdElapsedMs.toFloat() / holdDurationMs.toFloat()).coerceIn(0f, 1f)

                    // Provide subtle tactile tick at 50% progress
                    if (holdElapsedMs in (holdDurationMs / 2)..(holdDurationMs / 2 + tickMs)) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                holdProgress = 1f
                // Only trigger immediately if touch contact is currently active.
                // If user released touch within the recontact grace window, wait for recontact before triggering!
                if (isTouchActive) {
                    completeHoldAndTrigger()
                }
            }
        }
    }

    fun onTouchUp() {
        isTouchActive = false

        if (internalState == SosState.ACTIVE || internalState == SosState.ACTIVATING || internalState == SosState.HOLD_COMPLETE) {
            // Already triggered, do nothing on release
            return
        }

        // Recontact tolerance: do not instantly kill timer. Wait recontactToleranceMs
        recontactGraceJob?.cancel()
        recontactGraceJob = coroutineScope.launch {
            delay(recontactToleranceMs)
            // Recontact tolerance expired without touch return
            if (!isTouchActive) {
                holdTimerJob?.cancel()
                holdTimerJob = null
                holdElapsedMs = 0L
                holdProgress = 0f
                if (internalState != SosState.ACTIVE && internalState != SosState.CANCEL_PENDING) {
                    updateState(SosState.IDLE)
                }
            }
        }
    }

    val primarySosColor = SheVaultColors.emergency

    val (cardColor, haloColor, iconVector, statusText, subText) = when (internalState) {
        SosState.IDLE -> StateDisplayInfo(
            color = primarySosColor,
            halo = primarySosColor.copy(alpha = 0.15f),
            icon = Icons.Filled.Warning,
            mainText = "SOS",
            subText = "HOLD 1.4s"
        )
        SosState.TOUCH_DOWN, SosState.HOLDING -> StateDisplayInfo(
            color = primarySosColor,
            halo = primarySosColor.copy(alpha = 0.35f),
            icon = Icons.Filled.WarningAmber,
            mainText = "SOS",
            subText = "${(holdProgress * 100).toInt()}%"
        )
        SosState.HOLD_COMPLETE, SosState.ACTIVATING -> StateDisplayInfo(
            color = primarySosColor,
            halo = primarySosColor.copy(alpha = 0.6f),
            icon = Icons.Filled.Warning,
            mainText = "SOS",
            subText = "DISPATCHING"
        )
        SosState.PANIC_GESTURE -> StateDisplayInfo(
            color = primarySosColor,
            halo = primarySosColor.copy(alpha = 0.7f),
            icon = Icons.Filled.Warning,
            mainText = "SOS",
            subText = "PANIC 3x"
        )
        SosState.ACTIVE -> StateDisplayInfo(
            color = primarySosColor,
            halo = primarySosColor.copy(alpha = 0.45f),
            icon = Icons.Filled.Warning,
            mainText = "SOS",
            subText = "LIVE ALERT"
        )
        SosState.CANCEL_PENDING -> StateDisplayInfo(
            color = Color(0xFFD97706), // Warning Amber
            halo = Color(0xFFD97706).copy(alpha = 0.35f),
            icon = Icons.Filled.Warning,
            mainText = "CANCEL",
            subText = "PENDING PIN"
        )
        SosState.CANCELLED -> StateDisplayInfo(
            color = Color(0xFF475569), // Muted Slate
            halo = Color.Transparent,
            icon = Icons.Filled.CheckCircle,
            mainText = "STANDBY",
            subText = "CANCELLED"
        )
        SosState.ESCALATED -> StateDisplayInfo(
            color = Color(0xFFB91C1C), // Deep crimson
            halo = primarySosColor.copy(alpha = 0.75f),
            icon = Icons.Filled.Warning,
            mainText = "112",
            subText = "ESCALATED"
        )
        SosState.FAILED -> StateDisplayInfo(
            color = Color(0xFF991B1B),
            halo = Color(0xFF991B1B).copy(alpha = 0.25f),
            icon = Icons.Filled.Error,
            mainText = "RETRY",
            subText = "RECONNECTING"
        )
    }

    Box(
        modifier = modifier
            .size(size + 50.dp)
            .semantics {
                contentDescription = "Emergency SOS button. $subText. Hold for 1.4 seconds with 300ms touch-slip tolerance."
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer animated halo
        Box(
            modifier = Modifier
                .size(size * pulseScale)
                .clip(CircleShape)
                .background(haloColor)
        )

        // Progress ring canvas (surrounds main button)
        Canvas(
            modifier = Modifier.size(size + 24.dp)
        ) {
            // Track ring background
            drawCircle(
                color = primarySosColor.copy(alpha = 0.22f),
                style = Stroke(width = 6.dp.toPx())
            )
            // Active progress arc (fills from 0 to 360 degrees)
            if (holdProgress > 0f) {
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = holdProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Glowing perimeter border
        Box(
            modifier = Modifier
                .size(size + 8.dp)
                .clip(CircleShape)
                .border(
                    width = SheVaultTheme.iconRules.regularStroke,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )

        // Main tactile interactive core
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(cardColor)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
                        down.consume()
                        onTouchDown()

                        // Track pointer events until release or cancel
                        var pointerUp = false
                        while (!pointerUp) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Main)
                            if (event.changes.all { !it.pressed }) {
                                pointerUp = true
                                onTouchUp()
                            } else {
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Multi-modal requirement 1: Warning icon
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Multi-modal requirement 2: High-contrast SOS text
                Text(
                    text = statusText,
                    style = SheVaultTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 38.sp,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtext / status cue
                Text(
                    text = subText,
                    style = SheVaultTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }
    }
}

private data class StateDisplayInfo(
    val color: Color,
    val halo: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val mainText: String,
    val subText: String
)
