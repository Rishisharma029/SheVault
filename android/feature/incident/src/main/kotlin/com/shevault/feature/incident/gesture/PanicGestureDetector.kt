package com.shevault.feature.incident.gesture

import com.shevault.feature.incident.model.ActivationMethod
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID
import kotlin.math.sqrt

/**
 * Representation of an evaluated intent candidate.
 */
data class PanicIntentCandidate(
    val candidateId: String = UUID.randomUUID().toString(),
    val detectedAt: Long,
    val tapCount: Int,
    val durationMs: Long,
    val intervals: List<Long>,
    val cadenceVariance: Double,
    val confidenceScore: Float,
    val isIntentConfirmed: Boolean,
    val evaluationReason: String
)

/**
 * The 4 stages of the panic gesture activation pipeline.
 */
sealed class GesturePipelineStage {
    data object Idle : GesturePipelineStage()

    /** Stage 1: Raw gesture signature detected (e.g. 4 taps in time window) */
    data class GestureDetected(
        val tapCount: Int,
        val durationMs: Long,
        val intervals: List<Long>
    ) : GesturePipelineStage()

    /** Stage 2: Intent candidate evaluated against rhythm and jitter filters */
    data class IntentCandidate(
        val candidate: PanicIntentCandidate
    ) : GesturePipelineStage()

    /** Stage 3: Confirmed intent triggered safety activation */
    data class SafetyActivation(
        val candidate: PanicIntentCandidate,
        val activationMethod: ActivationMethod = ActivationMethod.PANIC_GESTURE,
        val activatedAt: Long = System.currentTimeMillis()
    ) : GesturePipelineStage()

    /** Stage 4: Safety session enters grace window before alert escalation */
    data class GraceWindow(
        val candidate: PanicIntentCandidate,
        val graceDurationSeconds: Int = 5,
        val secondsRemaining: Int = 5
    ) : GesturePipelineStage()
}

/**
 * PanicGestureDetector: Secondary emergency activation mechanism.
 *
 * Implements strict multi-stage validation:
 * 1. Gesture detected (4 taps within defined time window)
 * 2. Intent candidate (cadence/regularity scoring, bounce rejection)
 * 3. Safety activation (incident session initialization)
 * 4. Grace window (countdown buffer allowing authenticated abort)
 */
class PanicGestureDetector(
    val requiredTaps: Int = 4,
    val timeWindowMs: Long = 2000L,
    val minDebounceIntervalMs: Long = 80L,
    val maxTapIntervalMs: Long = 750L,
    val confidenceThreshold: Float = 0.65f,
    val graceDurationSeconds: Int = 5,
    private val onActivationConfirmed: ((PanicIntentCandidate) -> Unit)? = null
) {

    private val tapTimestamps = mutableListOf<Long>()
    private val _stageFlow = MutableSharedFlow<GesturePipelineStage>(extraBufferCapacity = 16)
    val stageFlow: SharedFlow<GesturePipelineStage> = _stageFlow.asSharedFlow()

    private var currentStage: GesturePipelineStage = GesturePipelineStage.Idle

    /**
     * Records an incoming tap event. Returns the resulting pipeline stage.
     */
    @Synchronized
    fun recordTap(timestampMs: Long = System.currentTimeMillis()): GesturePipelineStage {
        // 1. Debounce check: Reject rapid hardware bounce / touch jitter
        if (tapTimestamps.isNotEmpty()) {
            val lastTap = tapTimestamps.last()
            val delta = timestampMs - lastTap
            if (delta < minDebounceIntervalMs) {
                // Rejected as hardware contact bounce
                return currentStage
            }
        }

        // 2. Prune expired taps outside the rolling time window
        tapTimestamps.removeAll { timestampMs - it > timeWindowMs }

        // 3. Append current valid tap
        tapTimestamps.add(timestampMs)

        // 4. Check if required taps count reached
        if (tapTimestamps.size >= requiredTaps) {
            val windowTaps = tapTimestamps.takeLast(requiredTaps)
            val duration = windowTaps.last() - windowTaps.first()

            // Calculate consecutive tap intervals
            val intervals = mutableListOf<Long>()
            for (i in 1 until windowTaps.size) {
                intervals.add(windowTaps[i] - windowTaps[i - 1])
            }

            // STAGE 1: Gesture detected
            val detectedStage = GesturePipelineStage.GestureDetected(
                tapCount = requiredTaps,
                durationMs = duration,
                intervals = intervals
            )
            currentStage = detectedStage
            _stageFlow.tryEmit(detectedStage)

            // STAGE 2: Intent candidate evaluation
            val candidate = evaluateIntentCandidate(windowTaps, intervals, duration)
            val candidateStage = GesturePipelineStage.IntentCandidate(candidate)
            currentStage = candidateStage
            _stageFlow.tryEmit(candidateStage)

            // Reset tap buffer to avoid duplicate triggers
            tapTimestamps.clear()

            if (candidate.isIntentConfirmed) {
                // STAGE 3: Safety activation
                val activationStage = GesturePipelineStage.SafetyActivation(
                    candidate = candidate,
                    activationMethod = ActivationMethod.PANIC_GESTURE,
                    activatedAt = timestampMs
                )
                currentStage = activationStage
                _stageFlow.tryEmit(activationStage)
                onActivationConfirmed?.invoke(candidate)

                // STAGE 4: Grace window
                val graceStage = GesturePipelineStage.GraceWindow(
                    candidate = candidate,
                    graceDurationSeconds = graceDurationSeconds,
                    secondsRemaining = graceDurationSeconds
                )
                currentStage = graceStage
                _stageFlow.tryEmit(graceStage)
                return graceStage
            } else {
                return candidateStage
            }
        }

        return currentStage
    }

    /**
     * Evaluates rhythm regularity and bounds to separate deliberate panic tapping from
     * nervous fidgeting, accidental drops, or pocket friction.
     */
    private fun evaluateIntentCandidate(
        taps: List<Long>,
        intervals: List<Long>,
        durationMs: Long
    ): PanicIntentCandidate {
        // Interval sanity check: Any interval exceeding max allowed gap fails candidate
        val hasExcessiveInterval = intervals.any { it > maxTapIntervalMs }
        if (hasExcessiveInterval) {
            return PanicIntentCandidate(
                detectedAt = taps.last(),
                tapCount = taps.size,
                durationMs = durationMs,
                intervals = intervals,
                cadenceVariance = 1.0,
                confidenceScore = 0.3f,
                isIntentConfirmed = false,
                evaluationReason = "Tap interval exceeded maximum bound (${maxTapIntervalMs}ms)"
            )
        }

        // Calculate cadence consistency (Coefficient of Variation)
        val meanInterval = intervals.average()
        val variance = intervals.map { (it - meanInterval) * (it - meanInterval) }.average()
        val standardDeviation = sqrt(variance)
        val coefficientOfVariation = if (meanInterval > 0) standardDeviation / meanInterval else 1.0

        // Score formulation:
        // - Base score: 0.50 for achieving 4 taps within window
        // - Rhythm regularity bonus: up to 0.35 if intervals are evenly paced (CV <= 0.45)
        // - Pace bonus: up to 0.15 if tapping tempo is in optimal panic range (150ms - 450ms)
        var score = 0.50f

        val regularityBonus = when {
            coefficientOfVariation <= 0.30 -> 0.35f
            coefficientOfVariation <= 0.50 -> 0.25f
            coefficientOfVariation <= 0.70 -> 0.15f
            else -> 0.05f
        }
        score += regularityBonus

        val tempoBonus = if (meanInterval in 120.0..500.0) 0.15f else 0.05f
        score += tempoBonus

        val isConfirmed = score >= confidenceThreshold

        return PanicIntentCandidate(
            detectedAt = taps.last(),
            tapCount = taps.size,
            durationMs = durationMs,
            intervals = intervals,
            cadenceVariance = coefficientOfVariation,
            confidenceScore = score.coerceIn(0f, 1f),
            isIntentConfirmed = isConfirmed,
            evaluationReason = if (isConfirmed) {
                "Panic intent verified (Confidence: ${"%.2f".format(score)}, CV: ${"%.2f".format(coefficientOfVariation)})"
            } else {
                "Candidate rejected: Inconsistent cadence rhythm (Confidence: ${"%.2f".format(score)})"
            }
        )
    }

    @Synchronized
    fun reset() {
        tapTimestamps.clear()
        currentStage = GesturePipelineStage.Idle
    }
}
