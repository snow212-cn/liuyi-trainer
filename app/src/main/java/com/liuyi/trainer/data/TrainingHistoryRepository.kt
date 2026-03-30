package com.liuyi.trainer.data

import com.liuyi.trainer.model.TrainingSessionState
import kotlinx.coroutines.flow.Flow

class TrainingHistoryRepository(
    private val trainingHistoryDao: TrainingHistoryDao,
) {
    fun observeRecentSessions(): Flow<List<TrainingSessionWithSets>> =
        trainingHistoryDao.observeRecentSessions()

    suspend fun exportBackupJson(): String =
        TrainingHistoryBackupCodec.encode(trainingHistoryDao.getAllSessionsSnapshot())

    suspend fun deleteSession(sessionId: Long) {
        trainingHistoryDao.deleteSessionById(sessionId)
    }

    suspend fun updateSessionRepCounts(
        sessionId: Long,
        setRepUpdates: List<Pair<Long, Int>>,
    ) {
        setRepUpdates.forEach { (setId, completedRepCount) ->
            trainingHistoryDao.updateSetRepCount(
                setId = setId,
                completedRepCount = completedRepCount,
            )
        }
        trainingHistoryDao.updateSessionTotals(
            sessionId = sessionId,
            totalSets = setRepUpdates.size,
            totalReps = setRepUpdates.sumOf { it.second },
        )
    }

    suspend fun importBackupJson(rawJson: String): TrainingHistoryImportResult {
        return importBackupJson(
            rawJson = rawJson,
            mode = TrainingHistoryImportMode.Replace,
        )
    }

    suspend fun importBackupJson(
        rawJson: String,
        mode: TrainingHistoryImportMode,
    ): TrainingHistoryImportResult {
        val backup = TrainingHistoryBackupCodec.decode(rawJson)
        val sortedEntries = backup.sessions.sortedBy { it.sessionStartedAtUtcEpochMs }
        when (mode) {
            TrainingHistoryImportMode.Replace -> {
                trainingHistoryDao.replaceAllSessions(entries = sortedEntries)
            }

            TrainingHistoryImportMode.Merge -> {
                trainingHistoryDao.appendSessions(entries = sortedEntries)
            }
        }

        return TrainingHistoryImportResult(
            sessionCount = backup.sessions.size,
            setCount = backup.sessions.sumOf { it.sets.size },
        )
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

