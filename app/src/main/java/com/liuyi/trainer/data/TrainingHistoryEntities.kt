package com.liuyi.trainer.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "training_sessions",
)
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0,
    val familyId: String,
    val stepLevel: Int,
    val restPresetSeconds: Int,
    val sessionStartedAtUtcEpochMs: Long,
    val sessionEndedAtUtcEpochMs: Long,
    val totalSets: Int,
    val totalReps: Int,
)

@Entity(
    tableName = "training_sets",
    foreignKeys = [
        ForeignKey(
            entity = TrainingSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionOwnerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["sessionOwnerId"]),
    ],
)
data class TrainingSetEntity(
    @PrimaryKey(autoGenerate = true)
    val setId: Long = 0,
    val sessionOwnerId: Long,
    val setIndex: Int,
    val familyId: String,
    val stepLevel: Int,
    val cadenceProfileId: String,
    val startedAtUtcEpochMs: Long,
    val endedAtUtcEpochMs: Long,
    val elapsedMs: Long,
    val completedRepCount: Int,
    val lastCompletedPhase: String,
)

data class TrainingSessionWithSets(
    @Embedded
    val session: TrainingSessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionOwnerId",
    )
    val sets: List<TrainingSetEntity>,
)

