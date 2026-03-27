package com.liuyi.trainer.model

import java.time.Duration
import java.time.Instant

data class RestPreset(
    val defaultRestSeconds: Int,
    val presetOptionsSeconds: List<Int> = listOf(30, 60, 90, 120, 180),
)

sealed interface TrainingSessionState {
    data object Idle : TrainingSessionState

    data class SetRunning(
        val familyId: String,
        val stepLevel: Int,
        val cadenceProfile: CadenceProfile,
        val restPreset: RestPreset,
        val sessionStartedAtUtc: Instant,
        val setStartedAtUtc: Instant,
        val completedSets: List<WorkoutSetResult>,
    ) : TrainingSessionState

    data class RestRunning(
        val familyId: String,
        val stepLevel: Int,
        val cadenceProfile: CadenceProfile,
        val restPreset: RestPreset,
        val sessionStartedAtUtc: Instant,
        val restStartedAtUtc: Instant,
        val completedSets: List<WorkoutSetResult>,
    ) : TrainingSessionState

    data class RestOvertime(
        val familyId: String,
        val stepLevel: Int,
        val cadenceProfile: CadenceProfile,
        val restPreset: RestPreset,
        val sessionStartedAtUtc: Instant,
        val restStartedAtUtc: Instant,
        val overtimeElapsedMs: Long,
        val completedSets: List<WorkoutSetResult>,
    ) : TrainingSessionState

    data class Completed(
        val familyId: String,
        val stepLevel: Int,
        val restPreset: RestPreset,
        val sessionStartedAtUtc: Instant,
        val sessionEndedAtUtc: Instant,
        val completedSets: List<WorkoutSetResult>,
    ) : TrainingSessionState
}

data class RestSnapshot(
    val remainingMs: Long,
    val overtimeMs: Long,
    val isOvertime: Boolean,
)

fun startTrainingSession(
    familyId: String,
    stepLevel: Int,
    cadenceProfile: CadenceProfile,
    restPreset: RestPreset,
    startedAtUtc: Instant,
): TrainingSessionState.SetRunning = TrainingSessionState.SetRunning(
    familyId = familyId,
    stepLevel = stepLevel,
    cadenceProfile = cadenceProfile,
    restPreset = restPreset,
    sessionStartedAtUtc = startedAtUtc,
    setStartedAtUtc = startedAtUtc,
    completedSets = emptyList(),
)

fun finishCurrentSet(
    state: TrainingSessionState.SetRunning,
    endedAtUtc: Instant,
): TrainingSessionState.RestRunning {
    val result = finishWorkoutSet(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        cadenceProfile = state.cadenceProfile,
        startedAtUtc = state.setStartedAtUtc,
        endedAtUtc = endedAtUtc,
    )

    return TrainingSessionState.RestRunning(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        cadenceProfile = state.cadenceProfile,
        restPreset = state.restPreset,
        sessionStartedAtUtc = state.sessionStartedAtUtc,
        restStartedAtUtc = endedAtUtc,
        completedSets = state.completedSets + result,
    )
}

fun snapshotRestState(
    restStartedAtUtc: Instant,
    restPreset: RestPreset,
    nowUtc: Instant,
): RestSnapshot {
    val elapsedMs = Duration.between(restStartedAtUtc, nowUtc).toMillis().coerceAtLeast(0L)
    val targetMs = restPreset.defaultRestSeconds * 1000L

    return RestSnapshot(
        remainingMs = (targetMs - elapsedMs).coerceAtLeast(0L),
        overtimeMs = (elapsedMs - targetMs).coerceAtLeast(0L),
        isOvertime = elapsedMs > targetMs,
    )
}

fun updateRestState(
    state: TrainingSessionState.RestRunning,
    nowUtc: Instant,
): TrainingSessionState {
    val snapshot = snapshotRestState(
        restStartedAtUtc = state.restStartedAtUtc,
        restPreset = state.restPreset,
        nowUtc = nowUtc,
    )

    return if (snapshot.isOvertime) {
        TrainingSessionState.RestOvertime(
            familyId = state.familyId,
            stepLevel = state.stepLevel,
            cadenceProfile = state.cadenceProfile,
            restPreset = state.restPreset,
            sessionStartedAtUtc = state.sessionStartedAtUtc,
            restStartedAtUtc = state.restStartedAtUtc,
            overtimeElapsedMs = snapshot.overtimeMs,
            completedSets = state.completedSets,
        )
    } else {
        state
    }
}

fun startNextSet(
    state: TrainingSessionState,
    startedAtUtc: Instant,
): TrainingSessionState.SetRunning = when (state) {
    is TrainingSessionState.RestRunning -> TrainingSessionState.SetRunning(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        cadenceProfile = state.cadenceProfile,
        restPreset = state.restPreset,
        sessionStartedAtUtc = state.sessionStartedAtUtc,
        setStartedAtUtc = startedAtUtc,
        completedSets = state.completedSets,
    )

    is TrainingSessionState.RestOvertime -> TrainingSessionState.SetRunning(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        cadenceProfile = state.cadenceProfile,
        restPreset = state.restPreset,
        sessionStartedAtUtc = state.sessionStartedAtUtc,
        setStartedAtUtc = startedAtUtc,
        completedSets = state.completedSets,
    )

    else -> error("Only rest states can transition to the next set.")
}

fun completeTrainingSession(
    state: TrainingSessionState,
    endedAtUtc: Instant,
): TrainingSessionState.Completed = when (state) {
    is TrainingSessionState.SetRunning -> {
        val restState = finishCurrentSet(
            state = state,
            endedAtUtc = endedAtUtc,
        )
        TrainingSessionState.Completed(
            familyId = state.familyId,
            stepLevel = state.stepLevel,
            restPreset = state.restPreset,
            sessionStartedAtUtc = state.sessionStartedAtUtc,
            sessionEndedAtUtc = endedAtUtc,
            completedSets = restState.completedSets,
        )
    }

    is TrainingSessionState.RestRunning -> TrainingSessionState.Completed(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        restPreset = state.restPreset,
        sessionStartedAtUtc = state.sessionStartedAtUtc,
        sessionEndedAtUtc = endedAtUtc,
        completedSets = state.completedSets,
    )

    is TrainingSessionState.RestOvertime -> TrainingSessionState.Completed(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        restPreset = state.restPreset,
        sessionStartedAtUtc = state.sessionStartedAtUtc,
        sessionEndedAtUtc = endedAtUtc,
        completedSets = state.completedSets,
    )

    else -> error("Session must be active before completion.")
}
