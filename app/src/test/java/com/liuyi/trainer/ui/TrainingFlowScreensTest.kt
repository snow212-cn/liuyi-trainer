package com.liuyi.trainer.ui

import com.liuyi.trainer.data.TrainingSessionEntity
import com.liuyi.trainer.data.TrainingSessionWithSets
import com.liuyi.trainer.data.TrainingSetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingFlowScreensTest {
    @Test
    fun `buildHistoryGroups keeps month and day order`() {
        val rows = listOf(
            historyRow(sessionId = 11, monthLabel = "2026年4月", dayLabel = "4月30日 周四", timeLabel = "20:10"),
            historyRow(sessionId = 10, monthLabel = "2026年4月", dayLabel = "4月30日 周四", timeLabel = "18:00"),
            historyRow(sessionId = 9, monthLabel = "2026年4月", dayLabel = "4月29日 周三", timeLabel = "21:30"),
            historyRow(sessionId = 8, monthLabel = "2026年3月", dayLabel = "3月28日 周六", timeLabel = "09:15"),
        )

        val groups = buildHistoryGroups(rows)

        assertEquals(listOf("2026年4月", "2026年3月"), groups.map { it.monthLabel })
        assertEquals(listOf("4月30日 周四", "4月29日 周三"), groups.first().dayGroups.map { it.dayLabel })
        assertEquals(listOf(11L, 10L), groups.first().dayGroups.first().rows.map { it.sessionId })
        assertEquals(listOf(8L), groups.last().dayGroups.first().rows.map { it.sessionId })
    }

    @Test
    fun `buildHistoryGroups respects visible row limit`() {
        val rows = listOf(
            historyRow(sessionId = 4, monthLabel = "2026年4月", dayLabel = "4月30日 周四", timeLabel = "20:10"),
            historyRow(sessionId = 3, monthLabel = "2026年4月", dayLabel = "4月30日 周四", timeLabel = "18:00"),
            historyRow(sessionId = 2, monthLabel = "2026年4月", dayLabel = "4月29日 周三", timeLabel = "21:30"),
            historyRow(sessionId = 1, monthLabel = "2026年3月", dayLabel = "3月28日 周六", timeLabel = "09:15"),
        )

        val groups = buildHistoryGroups(rows, visibleRowLimit = 2)

        assertEquals(1, groups.size)
        assertEquals(listOf("4月30日 周四"), groups.first().dayGroups.map { it.dayLabel })
        assertEquals(listOf(4L, 3L), groups.first().dayGroups.first().rows.map { it.sessionId })
    }

    @Test
    fun `buildHistoryPreview keeps set breakdown and derives time labels`() {
        val preview = buildHistoryPreview(
            sessions = listOf(
                trainingSessionWithSets(
                    sessionId = 7,
                    familyId = "pushup",
                    stepLevel = 5,
                    endedAtMs = 1_714_505_200_000,
                    setReps = listOf(20, 18, 15),
                ),
            ),
        )

        val row = preview.rows.single()
        assertEquals("20 + 18 + 15", row.setPreview)
        assertEquals("3 组", row.setCountLabel)
        assertTrue(row.monthLabel.isNotBlank())
        assertTrue(row.dayLabel.isNotBlank())
        assertTrue(row.timeLabel.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(row.dateLabel.contains(" "))
    }

    private fun historyRow(
        sessionId: Long,
        monthLabel: String,
        dayLabel: String,
        timeLabel: String,
    ): HistoryRowPreview = HistoryRowPreview(
        sessionId = sessionId,
        familyLabel = "俯卧撑",
        stepLabel = "标准俯卧撑",
        title = "俯卧撑·标准俯卧撑",
        totalRepsLabel = "53",
        setCountLabel = "3 组",
        setPreview = "20 + 18 + 15",
        monthLabel = monthLabel,
        dayLabel = dayLabel,
        timeLabel = timeLabel,
        dateLabel = "$dayLabel $timeLabel",
    )

    private fun trainingSessionWithSets(
        sessionId: Long,
        familyId: String,
        stepLevel: Int,
        endedAtMs: Long,
        setReps: List<Int>,
    ): TrainingSessionWithSets {
        val startedAtMs = endedAtMs - 10 * 60 * 1000
        val session = TrainingSessionEntity(
            sessionId = sessionId,
            familyId = familyId,
            stepLevel = stepLevel,
            restPresetSeconds = 60,
            sessionStartedAtUtcEpochMs = startedAtMs,
            sessionEndedAtUtcEpochMs = endedAtMs,
            totalSets = setReps.size,
            totalReps = setReps.sum(),
        )
        val sets = setReps.mapIndexed { index, reps ->
            TrainingSetEntity(
                setId = index.toLong() + 1,
                sessionOwnerId = sessionId,
                setIndex = index + 1,
                familyId = familyId,
                stepLevel = stepLevel,
                cadenceProfileId = "default",
                startedAtUtcEpochMs = startedAtMs + index * 60_000L,
                endedAtUtcEpochMs = startedAtMs + index * 60_000L + 30_000L,
                elapsedMs = 30_000L,
                completedRepCount = reps,
                lastCompletedPhase = "TOP",
            )
        }
        return TrainingSessionWithSets(
            session = session,
            sets = sets,
        )
    }
}
