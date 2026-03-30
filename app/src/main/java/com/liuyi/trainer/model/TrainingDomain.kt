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
        label = "2-1-2 原书节奏",
        eccentricSeconds = 2,
        bottomPauseSeconds = 1,
        concentricSeconds = 2,
        topPauseSeconds = 0,
        source = "按原书确认的 2-1-2 节奏演示",
    )

    private fun steps(vararg labels: String): List<MovementStep> = labels.mapIndexed { index, label ->
        MovementStep(
            level = index + 1,
            label = label,
        )
    }

    val families: List<MovementFamily> = listOf(
        MovementFamily(
            id = "pushup",
            titleZh = "俯卧撑",
            titleEn = "Pushup",
            summary = "上肢推力链训练，覆盖胸肩肱三头与核心协同。",
            steps = steps(
                "墙壁俯卧撑",
                "上斜俯卧撑",
                "膝盖俯卧撑",
                "半俯卧撑",
                "标准俯卧撑",
                "窄距俯卧撑",
                "偏重俯卧撑",
                "单臂半俯卧撑",
                "杠杆俯卧撑",
                "单臂俯卧撑",
            ),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "squat",
            titleZh = "深蹲",
            titleEn = "Squat",
            summary = "下肢屈伸链训练，覆盖股四头、臀腿与稳定控制。",
            steps = steps(
                "肩倒立深蹲",
                "折刀深蹲",
                "支撑深蹲",
                "半深蹲",
                "标准深蹲",
                "窄距深蹲",
                "偏重深蹲",
                "单腿半深蹲",
                "单腿辅助深蹲",
                "单腿深蹲",
            ),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "pullup",
            titleZh = "引体",
            titleEn = "Pullup",
            summary = "上肢拉力链训练，强调背阔、前臂和肩胛控制。",
            steps = steps(
                "垂直引体",
                "水平引体向上",
                "折刀引体向上",
                "半引体向上",
                "标准引体向上",
                "窄距引体向上",
                "偏重引体向上",
                "单臂半引体向上",
                "单臂辅助引体向上",
                "单臂引体向上",
            ),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "leg_raise",
            titleZh = "举腿",
            titleEn = "Leg Raise",
            summary = "躯干前链训练，覆盖腹部、髋屈肌与骨盆控制。",
            steps = steps(
                "坐姿屈膝",
                "平卧抬膝",
                "平卧屈举腿",
                "平卧蛙举腿",
                "平卧直举腿",
                "悬垂屈膝",
                "悬垂屈举腿",
                "悬垂蛙举腿",
                "悬垂半举腿",
                "悬垂直举腿",
            ),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "bridge",
            titleZh = "桥",
            titleEn = "Bridge",
            summary = "躯干后链训练，强调脊柱伸展、臀部与肩部开合。",
            steps = steps(
                "短桥",
                "直桥",
                "高低桥",
                "顶桥",
                "半桥",
                "标准桥",
                "下行桥",
                "上行桥",
                "合桥",
                "铁板桥",
            ),
            previewCadence = previewCadence,
        ),
        MovementFamily(
            id = "handstand_pushup",
            titleZh = "倒立撑",
            titleEn = "Handstand Pushup",
            summary = "垂直推力链训练，强调肩部力量、平衡与全身张力。",
            steps = steps(
                "靠墙顶立",
                "乌鸦式",
                "靠墙倒立",
                "半倒立撑",
                "标准倒立撑",
                "窄距倒立撑",
                "偏重倒立撑",
                "单臂半倒立撑",
                "杠杆倒立撑",
                "单臂倒立撑",
            ),
            previewCadence = previewCadence,
        ),
    )
}
