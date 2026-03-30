package com.liuyi.trainer.data

import org.json.JSONArray
import org.json.JSONObject

data class TrainingHistoryBackup(
    val schemaVersion: Int,
    val exportedAtUtcEpochMs: Long,
    val sessions: List<TrainingSessionBackupEntry>,
)

data class TrainingSessionBackupEntry(
    val familyId: String,
    val stepLevel: Int,
    val restPresetSeconds: Int,
    val sessionStartedAtUtcEpochMs: Long,
    val sessionEndedAtUtcEpochMs: Long,
    val sets: List<TrainingSetBackupEntry>,
)

data class TrainingSetBackupEntry(
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

object TrainingHistoryBackupCodec {
    private const val CurrentSchemaVersion = 1

    fun encode(
        sessions: List<TrainingSessionWithSets>,
        exportedAtUtcEpochMs: Long = System.currentTimeMillis(),
    ): String {
        val payload = TrainingHistoryBackup(
            schemaVersion = CurrentSchemaVersion,
            exportedAtUtcEpochMs = exportedAtUtcEpochMs,
            sessions = sessions.map { sessionWithSets ->
                TrainingSessionBackupEntry(
                    familyId = sessionWithSets.session.familyId,
                    stepLevel = sessionWithSets.session.stepLevel,
                    restPresetSeconds = sessionWithSets.session.restPresetSeconds,
                    sessionStartedAtUtcEpochMs = sessionWithSets.session.sessionStartedAtUtcEpochMs,
                    sessionEndedAtUtcEpochMs = sessionWithSets.session.sessionEndedAtUtcEpochMs,
                    sets = sessionWithSets.sets
                        .sortedBy { it.setIndex }
                        .map { set ->
                            TrainingSetBackupEntry(
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
            },
        )

        return payload.toJson().toString(2)
    }

    fun decode(rawJson: String): TrainingHistoryBackup {
        val root = JSONObject(rawJson)
        val schemaVersion = root.optInt("schemaVersion", 0)
        require(schemaVersion in 1..CurrentSchemaVersion) {
            "备份版本不受支持"
        }

        val sessions = root.optJSONArray("sessions")
            ?.let(::readSessions)
            ?: emptyList()

        return TrainingHistoryBackup(
            schemaVersion = schemaVersion,
            exportedAtUtcEpochMs = root.optLong(
                "exportedAtUtcEpochMs",
                System.currentTimeMillis(),
            ),
            sessions = sessions,
        )
    }

    private fun TrainingHistoryBackup.toJson(): JSONObject = JSONObject().apply {
        put("schemaVersion", schemaVersion)
        put("exportedAtUtcEpochMs", exportedAtUtcEpochMs)
        put(
            "sessions",
            JSONArray().apply {
                sessions.forEach { session ->
                    put(session.toJson())
                }
            },
        )
    }

    private fun TrainingSessionBackupEntry.toJson(): JSONObject = JSONObject().apply {
        put("familyId", familyId)
        put("stepLevel", stepLevel)
        put("restPresetSeconds", restPresetSeconds)
        put("sessionStartedAtUtcEpochMs", sessionStartedAtUtcEpochMs)
        put("sessionEndedAtUtcEpochMs", sessionEndedAtUtcEpochMs)
        put(
            "sets",
            JSONArray().apply {
                sets.forEach { set ->
                    put(set.toJson())
                }
            },
        )
    }

    private fun TrainingSetBackupEntry.toJson(): JSONObject = JSONObject().apply {
        put("setIndex", setIndex)
        put("familyId", familyId)
        put("stepLevel", stepLevel)
        put("cadenceProfileId", cadenceProfileId)
        put("startedAtUtcEpochMs", startedAtUtcEpochMs)
        put("endedAtUtcEpochMs", endedAtUtcEpochMs)
        put("elapsedMs", elapsedMs)
        put("completedRepCount", completedRepCount)
        put("lastCompletedPhase", lastCompletedPhase)
    }

    private fun readSessions(array: JSONArray): List<TrainingSessionBackupEntry> =
        List(array.length()) { index ->
            array.getJSONObject(index).toSessionEntry()
        }

    private fun JSONObject.toSessionEntry(): TrainingSessionBackupEntry {
        val startedAt = optLong("sessionStartedAtUtcEpochMs", 0L).coerceAtLeast(0L)
        val endedAt = optLong("sessionEndedAtUtcEpochMs", startedAt).coerceAtLeast(startedAt)
        val sets = optJSONArray("sets")
            ?.let(::readSets)
            ?.sortedBy { it.setIndex }
            ?: emptyList()

        return TrainingSessionBackupEntry(
            familyId = requireString("familyId"),
            stepLevel = optInt("stepLevel", 1).coerceAtLeast(1),
            restPresetSeconds = optInt("restPresetSeconds", 0).coerceAtLeast(0),
            sessionStartedAtUtcEpochMs = startedAt,
            sessionEndedAtUtcEpochMs = endedAt,
            sets = sets,
        )
    }

    private fun readSets(array: JSONArray): List<TrainingSetBackupEntry> =
        List(array.length()) { index ->
            array.getJSONObject(index).toSetEntry(fallbackIndex = index + 1)
        }

    private fun JSONObject.toSetEntry(fallbackIndex: Int): TrainingSetBackupEntry {
        val startedAt = optLong("startedAtUtcEpochMs", 0L).coerceAtLeast(0L)
        val endedAt = optLong("endedAtUtcEpochMs", startedAt).coerceAtLeast(startedAt)

        return TrainingSetBackupEntry(
            setIndex = optInt("setIndex", fallbackIndex).coerceAtLeast(1),
            familyId = requireString("familyId"),
            stepLevel = optInt("stepLevel", 1).coerceAtLeast(1),
            cadenceProfileId = requireString("cadenceProfileId"),
            startedAtUtcEpochMs = startedAt,
            endedAtUtcEpochMs = endedAt,
            elapsedMs = optLong("elapsedMs", endedAt - startedAt).coerceAtLeast(0L),
            completedRepCount = optInt("completedRepCount", 0).coerceAtLeast(0),
            lastCompletedPhase = requireString("lastCompletedPhase"),
        )
    }

    private fun JSONObject.requireString(key: String): String =
        optString(key, "").trim().ifBlank {
            throw IllegalArgumentException("备份缺少字段：$key")
        }
}

data class TrainingHistoryImportResult(
    val sessionCount: Int,
    val setCount: Int,
)
