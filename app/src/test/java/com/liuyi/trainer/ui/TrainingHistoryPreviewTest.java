package com.liuyi.trainer.ui;

import static org.junit.Assert.assertEquals;

import com.liuyi.trainer.data.TrainingSessionEntity;
import com.liuyi.trainer.data.TrainingSessionWithSets;
import com.liuyi.trainer.data.TrainingSetEntity;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TrainingHistoryPreviewTest {
    @Test
    public void buildHistoryPreviewSortsNewestFirstAndBuildsDayFields() {
        TrainingSessionWithSets older = sessionWithSets(
                1L,
                "pushup",
                4,
                epochAt(2025, 4, 24, 19, 30),
                epochAt(2025, 4, 24, 19, 42),
                Arrays.asList(8, 7, 8, 8)
        );
        TrainingSessionWithSets newerSameDay = sessionWithSets(
                2L,
                "squat",
                3,
                epochAt(2025, 4, 24, 20, 10),
                epochAt(2025, 4, 24, 20, 16),
                Arrays.asList(12, 11, 10)
        );
        TrainingSessionWithSets newestDifferentDay = sessionWithSets(
                3L,
                "bridge",
                2,
                epochAt(2025, 4, 25, 8, 10),
                epochAt(2025, 4, 25, 8, 20),
                Arrays.asList(5, 5, 5)
        );

        HistoryPreview preview = TrainingFlowScreensKt.buildHistoryPreview(
                Arrays.asList(older, newestDifferentDay, newerSameDay),
                null,
                null,
                null
        );

        List<HistoryRowPreview> rows = preview.getRows();
        assertEquals(Arrays.asList(3L, 2L, 1L), Arrays.asList(
                rows.get(0).getSessionId(),
                rows.get(1).getSessionId(),
                rows.get(2).getSessionId()
        ));

        HistoryRowPreview first = rows.get(0);
        assertEquals("桥", first.getFamilyLabel());
        assertEquals("直桥", first.getStepLabel());
        assertEquals("桥·直桥", first.getTitle());
        assertEquals("15", first.getTotalRepsLabel());
        assertEquals("3 组", first.getSetCountLabel());
        assertEquals("5 + 5 + 5", first.getSetPreview());
        assertEquals("2025年4月", first.getMonthLabel());
        assertEquals("2025-04-25", first.getDayKey());
        assertEquals("4月25日", first.getDayLabel());
        assertEquals("4月25日 08:20", first.getDateLabel());
        assertEquals("08:20", first.getTimeLabel());
    }

    @Test
    public void buildHistoryPreviewPreservesTransferAndExportMetadata() {
        HistoryPreview preview = TrainingFlowScreensKt.buildHistoryPreview(
                java.util.Collections.emptyList(),
                "导入完成",
                "最近导出：2025-04-25",
                "content://history/latest.json"
        );

        assertEquals("导入完成", preview.getTransferStatus());
        assertEquals("最近导出：2025-04-25", preview.getLatestExportLabel());
        assertEquals("content://history/latest.json", preview.getLatestExportUri());
    }

    private static TrainingSessionWithSets sessionWithSets(
            long sessionId,
            String familyId,
            int stepLevel,
            long startedAt,
            long endedAt,
            List<Integer> reps
    ) {
        java.util.ArrayList<TrainingSetEntity> sets = new java.util.ArrayList<>();
        for (int index = 0; index < reps.size(); index++) {
            sets.add(new TrainingSetEntity(
                    sessionId * 10 + index,
                    sessionId,
                    index + 1,
                    familyId,
                    stepLevel,
                    "custom",
                    startedAt + index * 60_000L,
                    startedAt + index * 60_000L + 45_000L,
                    45_000L,
                    reps.get(index),
                    "Rising"
            ));
        }

        TrainingSessionEntity session = new TrainingSessionEntity(
                sessionId,
                familyId,
                stepLevel,
                90,
                startedAt,
                endedAt,
                reps.size(),
                reps.stream().mapToInt(Integer::intValue).sum()
        );
        return new TrainingSessionWithSets(session, sets);
    }

    private static long epochAt(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
