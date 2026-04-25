package com.liuyi.trainer.model

import java.time.Duration
import java.time.Instant

enum class CadencePhase {
    Lowering,
    BottomHold,
    Rising,
}

data class LiveCadenceProgress(
    val phase: CadencePhase,
    val phaseElapsedMs: Long,
    val phaseTotalMs: Long,
    val completedRepCount: Int,
)

data class WorkoutSetResult(
    val familyId: String,
    val stepLevel: Int,
    val startedAtUtc: Instant,
    val endedAtUtc: Instant,
    val elapsedMs: Long,
    val cadenceProfileId: String,
    val completedRepCount: Int,
    val lastCompletedPhase: CadencePhase,
)

fun trackLiveCadence(
    cadenceProfile: CadenceProfile,
    elapsedMs: Long,
): LiveCadenceProgress {
    val safeElapsedMs = elapsedMs.coerceAtLeast(0L)
    val cycleMs = cadenceProfile.cycleDurationMs.coerceAtLeast(1L)
    val completedRepCount = (safeElapsedMs / cycleMs).toInt()
    val cyclePositionMs = safeElapsedMs % cycleMs

    val loweringMs = cadenceProfile.eccentricSeconds * 1000L
    val bottomHoldMs = cadenceProfile.bottomPauseSeconds * 1000L
    val risingMs = cadenceProfile.concentricSeconds * 1000L

    return when {
        cyclePositionMs < loweringMs -> LiveCadenceProgress(
            phase = CadencePhase.Lowering,
            phaseElapsedMs = cyclePositionMs,
            phaseTotalMs = loweringMs,
            completedRepCount = completedRepCount,
        )

        cyclePositionMs < loweringMs + bottomHoldMs -> LiveCadenceProgress(
            phase = CadencePhase.BottomHold,
            phaseElapsedMs = cyclePositionMs - loweringMs,
            phaseTotalMs = bottomHoldMs,
            completedRepCount = completedRepCount,
        )

        else -> LiveCadenceProgress(
            phase = CadencePhase.Rising,
            phaseElapsedMs = cyclePositionMs - loweringMs - bottomHoldMs,
            phaseTotalMs = risingMs,
            completedRepCount = completedRepCount,
        )
    }
}

fun finishWorkoutSet(
    familyId: String,
    stepLevel: Int,
    cadenceProfile: CadenceProfile,
    startedAtUtc: Instant,
    endedAtUtc: Instant,
): WorkoutSetResult {
    val elapsedMs = Duration.between(startedAtUtc, endedAtUtc).toMillis().coerceAtLeast(0L)
    val liveProgress = trackLiveCadence(
        cadenceProfile = cadenceProfile,
        elapsedMs = elapsedMs,
    )

    return WorkoutSetResult(
        familyId = familyId,
        stepLevel = stepLevel,
        startedAtUtc = startedAtUtc,
        endedAtUtc = endedAtUtc,
        elapsedMs = elapsedMs,
        cadenceProfileId = cadenceProfile.id,
        completedRepCount = liveProgress.completedRepCount,
        lastCompletedPhase = liveProgress.phase,
    )
}

