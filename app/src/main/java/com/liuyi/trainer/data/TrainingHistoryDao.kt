package com.liuyi.trainer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<TrainingSetEntity>)

    @Transaction
    suspend fun insertCompletedSession(
        session: TrainingSessionEntity,
        sets: List<TrainingSetEntity>,
    ) {
        val sessionId = insertSession(session)
        if (sets.isNotEmpty()) {
            insertSets(
                sets = sets.map { set ->
                    set.copy(sessionOwnerId = sessionId)
                },
            )
        }
    }

    @Transaction
    suspend fun replaceAllSessions(
        entries: List<TrainingSessionBackupEntry>,
    ) {
        deleteAllSessions()
        entries.forEach { entry ->
            insertCompletedSession(
                session = TrainingSessionEntity(
                    familyId = entry.familyId,
                    stepLevel = entry.stepLevel,
                    restPresetSeconds = entry.restPresetSeconds,
                    sessionStartedAtUtcEpochMs = entry.sessionStartedAtUtcEpochMs,
                    sessionEndedAtUtcEpochMs = entry.sessionEndedAtUtcEpochMs,
                    totalSets = entry.sets.size,
                    totalReps = entry.sets.sumOf { it.completedRepCount },
                ),
                sets = entry.sets
                    .sortedBy { it.setIndex }
                    .map { set ->
                        TrainingSetEntity(
                            sessionOwnerId = 0,
                            setIndex = set.setIndex,
                            familyId = set.familyId,
                            stepLevel = set.stepLevel,
                            cadenceProfileId = set.cadenceProfileId,
                            startedAtUtcEpochMs = set.startedAtUtcEpochMs,
                            endedAtUtcEpochMs = set.endedAtUtcEpochMs,
                            elapsedMs = set.elapsedMs,
                            completedRepCount = set.completedRepCount,
                            lastCompletedPhase = set.lastCompletedPhase,
                        )
                    },
            )
        }
    }

    @Transaction
    @Query("SELECT * FROM training_sessions ORDER BY sessionEndedAtUtcEpochMs DESC")
    fun observeRecentSessions(): Flow<List<TrainingSessionWithSets>>

    @Transaction
    @Query("SELECT * FROM training_sessions ORDER BY sessionEndedAtUtcEpochMs DESC")
    suspend fun getAllSessionsSnapshot(): List<TrainingSessionWithSets>

    @Query(
        """
        UPDATE training_sessions
        SET totalSets = :totalSets,
            totalReps = :totalReps
        WHERE sessionId = :sessionId
        """
    )
    suspend fun updateSessionTotals(
        sessionId: Long,
        totalSets: Int,
        totalReps: Int,
    )

    @Query(
        """
        UPDATE training_sets
        SET completedRepCount = :completedRepCount
        WHERE setId = :setId
        """
    )
    suspend fun updateSetRepCount(
        setId: Long,
        completedRepCount: Int,
    )

    @Query("DELETE FROM training_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM training_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)
}
