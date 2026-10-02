package com.shevault.feature.incident

import com.shevault.feature.incident.gesture.GesturePipelineStage
import com.shevault.feature.incident.gesture.PanicGestureDetector
import com.shevault.feature.incident.gesture.PanicIntentCandidate
import com.shevault.feature.incident.model.ActivationMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PanicGestureDetectorTest {

    private lateinit var detector: PanicGestureDetector
    private var activatedCandidate: PanicIntentCandidate? = null

    @Before
    fun setup() {
        activatedCandidate = null
        detector = PanicGestureDetector(
            requiredTaps = 4,
            timeWindowMs = 2000L,
            minDebounceIntervalMs = 80L,
            maxTapIntervalMs = 750L,
            confidenceThreshold = 0.65f,
            graceDurationSeconds = 5,
            onActivationConfirmed = { activatedCandidate = it }
        )
    }

    @Test
    fun testExactFourTapsWithinWindowTriggersFullPipeline() {
        // Quad-tap with regular 250ms cadence: 0ms, 250ms, 500ms, 750ms
        val stage1 = detector.recordTap(1000L)
        assertTrue(stage1 is GesturePipelineStage.Idle)

        val stage2 = detector.recordTap(1250L)
        assertTrue(stage2 is GesturePipelineStage.Idle)

        val stage3 = detector.recordTap(1500L)
        assertTrue(stage3 is GesturePipelineStage.Idle)

        val stage4 = detector.recordTap(1750L)
        // 4th tap must complete pipeline all the way to GraceWindow
        assertTrue("4th tap within window must enter GraceWindow stage", stage4 is GesturePipelineStage.GraceWindow)
        val grace = stage4 as GesturePipelineStage.GraceWindow
        assertEquals(5, grace.graceDurationSeconds)
        assertEquals(5, grace.secondsRemaining)

        // Verify intent candidate properties
        val candidate = grace.candidate
        assertEquals(4, candidate.tapCount)
        assertEquals(750L, candidate.durationMs)
        assertTrue("Panic candidate must be confirmed", candidate.isIntentConfirmed)
        assertTrue("Confidence score must exceed threshold", candidate.confidenceScore >= 0.65f)
        assertNotNull("Activation callback must be invoked", activatedCandidate)
        assertEquals(candidate.candidateId, activatedCandidate?.candidateId)
    }

    @Test
    fun testLessThanFourTapsDoesNotActivate() {
        detector.recordTap(1000L)
        detector.recordTap(1300L)
        val stage3 = detector.recordTap(1600L)

        // 3 taps is not enough for panic gesture
        assertTrue(stage3 is GesturePipelineStage.Idle)
        assertEquals(null, activatedCandidate)
    }

    @Test
    fun testTapsSpacedBeyondWindowArePruned() {
        // Taps at 1000, 1300, 1600 (3 taps)
        detector.recordTap(1000L)
        detector.recordTap(1300L)
        detector.recordTap(1600L)

        // Long pause: next tap at 4000L (delta is 2400ms > 2000ms window)
        val stage4 = detector.recordTap(4000L)

        // Taps from 1000, 1300, 1600 must be pruned
        assertTrue("Expired taps must be pruned and not trigger activation", stage4 is GesturePipelineStage.Idle)
        assertEquals(null, activatedCandidate)
    }

    @Test
    fun testHardwareBounceJitterRejectedByDebounce() {
        // Tap 1
        detector.recordTap(1000L)

        // Jitter glitch 30ms later (< 80ms min debounce)
        detector.recordTap(1030L)

        // Tap 2
        detector.recordTap(1300L)

        // Tap 3
        detector.recordTap(1600L)

        // Since the 1030ms bounce was discarded, we only have 3 taps total
        val stage = detector.recordTap(1600L)
        // Same timestamp or too close is rejected
        assertEquals(null, activatedCandidate)
    }

    @Test
    fun testIntervalExceedingMaxBoundFailsIntent() {
        // Tap 1 at 1000, Tap 2 at 1200, Tap 3 at 1400, Tap 4 at 2300 (gap = 900ms > 750ms max bound)
        detector.recordTap(1000L)
        detector.recordTap(1200L)
        detector.recordTap(1400L)
        val stage4 = detector.recordTap(2300L)

        // Candidate evaluated but rejected due to excessive gap between taps
        assertTrue("Candidate with interval > 750ms must not activate", stage4 is GesturePipelineStage.IntentCandidate)
        val candidate = (stage4 as GesturePipelineStage.IntentCandidate).candidate
        assertFalse(candidate.isIntentConfirmed)
        assertEquals(null, activatedCandidate)
    }

    @Test
    fun testResetClearsBuffer() {
        detector.recordTap(1000L)
        detector.recordTap(1250L)
        detector.reset()

        detector.recordTap(1500L)
        detector.recordTap(1750L)
        // Should only be 2 taps after reset
        assertEquals(null, activatedCandidate)
    }
}
