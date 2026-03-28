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
    @Query("SELECT * FROM training_sessions ORDER BY sessionEndedAtUtcEpochMs DESC")
    fun observeRecentSessions(): Flow<List<TrainingSessionWithSets>>

    @Query("DELETE FROM training_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)
}
