package com.liuyi.trainer.model

import androidx.compose.runtime.Immutable

@Immutable
data class CadenceProfile(
    val id: String,
    val label: String,
    val eccentricSeconds: Int,
    val bottomPauseSeconds: Int,
    val concentricSeconds: Int,
    val topPauseSeconds: Int,
    val source: String,
) {
    val cycleDurationMs: Long
        get() = (
            eccentricSeconds +
                bottomPauseSeconds +
                concentricSeconds +
                topPauseSeconds
            ) * 1000L
}

enum class ContentStatus {
    Placeholder,
    Draft,
    Ready,
}

@Immutable
data class MovementStep(
    val level: Int,
    val label: String,
    val contentStatus: ContentStatus = ContentStatus.Placeholder,
)

@Immutable
data class MovementFamily(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val summary: String,
    val steps: List<MovementStep>,
    val previewCadence: CadenceProfile,
)

object ExerciseCatalog {
    val previewCadence = CadenceProfile(
        id = "preview_default",
        label = "2-1-2 原型节奏",
        eccentricSeconds = 2,
        bottomPauseSeconds = 1,
        concentricSeconds = 2,
        topPauseSeconds = 0,
        source = "当前按用户确认的 2-1-2 节奏原型演示",
    )

    private fun tenSteps(): List<MovementStep> = (1..10).map { level ->
        MovementStep(
            level = level,
            label = "第${level}式",
        )
    }

    val families: List<MovementFamily> = listOf(
        MovementFamily(
            id = "pushup",
            titleZh = "俯卧撑",
            titleEn = "Pushup",
            summary = "上肢推力链训练，覆盖胸肩肱三头与核心协同。",
            steps = tenSteps(),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "squat",
            titleZh = "深蹲",
            titleEn = "Squat",
            summary = "下肢屈伸链训练，覆盖股四头、臀腿与稳定控制。",
            steps = tenSteps(),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "pullup",
            titleZh = "引体",
            titleEn = "Pullup",
            summary = "上肢拉力链训练，强调背阔、前臂和肩胛控制。",
            steps = tenSteps(),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "leg_raise",
            titleZh = "举腿",
            titleEn = "Leg Raise",
            summary = "躯干前链训练，覆盖腹部、髋屈肌与骨盆控制。",
            steps = tenSteps(),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "bridge",
            titleZh = "桥",
            titleEn = "Bridge",
            summary = "躯干后链训练，强调脊柱伸展、臀部与肩部开合。",
            steps = tenSteps(),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "handstand_pushup",
            titleZh = "倒立撑",
            titleEn = "Handstand Pushup",
            summary = "垂直推力链训练，强调肩部力量、平衡与全身张力。",
            steps = tenSteps(),
            previewCadence = previewCadence,
        ),
    )
}
