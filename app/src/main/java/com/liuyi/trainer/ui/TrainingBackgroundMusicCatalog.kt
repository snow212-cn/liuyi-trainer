package com.liuyi.trainer.ui

import androidx.annotation.RawRes
import com.liuyi.trainer.R

data class TrainingBackgroundMusicOption(
    val id: String,
    val label: String,
    val summary: String,
    @param:RawRes val rawResId: Int,
)

val trainingBackgroundMusicOptions: List<TrainingBackgroundMusicOption> = listOf(
    TrainingBackgroundMusicOption(
        id = "tense_future_loop",
        label = "重压未来",
        summary = "低频压迫、回声旋律、轻打击",
        rawResId = R.raw.bgm_tense_future_loop,
    ),
    TrainingBackgroundMusicOption(
        id = "project_utopia",
        label = "静压氛围",
        summary = "慢速垫底，更适合稳定节奏训练",
        rawResId = R.raw.bgm_project_utopia,
    ),
    TrainingBackgroundMusicOption(
        id = "spacy_loop",
        label = "冷感电子",
        summary = "更清晰的电子循环，适合专注推进",
        rawResId = R.raw.bgm_spacy_loop,
    ),
)

fun defaultTrainingBackgroundMusicOption(): TrainingBackgroundMusicOption =
    trainingBackgroundMusicOptions.first()

fun findTrainingBackgroundMusicOption(id: String): TrainingBackgroundMusicOption? =
    trainingBackgroundMusicOptions.firstOrNull { it.id == id }
