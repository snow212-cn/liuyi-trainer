package com.liuyi.trainer.data

import com.liuyi.trainer.model.TrainingSessionState
import kotlinx.coroutines.flow.Flow

class TrainingHistoryRepository(
    private val trainingHistoryDao: TrainingHistoryDao,
) {
    fun observeRecentSessions(): Flow<List<TrainingSessionWithSets>> =
        trainingHistoryDao.observeRecentSessions()

    suspend fun deleteSession(sessionId: Long) {
        trainingHistoryDao.deleteSessionById(sessionId)
    }

    suspend fun saveCompletedSession(
        state: TrainingSessionState.Completed,
    ) {
        val session = TrainingSessionEntity(
            familyId = state.familyId,
            stepLevel = state.stepLevel,
            restPresetSeconds = state.restPreset.defaultRestSeconds,
            sessionStartedAtUtcEpochMs = state.sessionStartedAtUtc.toEpochMilli(),
            sessionEndedAtUtcEpochMs = state.sessionEndedAtUtc.toEpochMilli(),
            totalSets = state.completedSets.size,
            totalReps = state.completedSets.sumOf { it.completedRepCount },
        )
        val setEntities = state.completedSets.mapIndexed { index, set ->
            TrainingSetEntity(
                sessionOwnerId = 0,
                setIndex = index + 1,
                familyId = set.familyId,
                stepLevel = set.stepLevel,
                cadenceProfileId = set.cadenceProfileId,
                startedAtUtcEpochMs = set.startedAtUtc.toEpochMilli(),
                endedAtUtcEpochMs = set.endedAtUtc.toEpochMilli(),
                elapsedMs = set.elapsedMs,
                completedRepCount = set.completedRepCount,
                lastCompletedPhase = set.lastCompletedPhase.name,
            )
        }
        trainingHistoryDao.insertCompletedSession(
            session = session,
            sets = setEntities,
        )
    }
}

