package com.liuyi.trainer.model

import androidx.compose.runtime.Immutable

@Immutable
data class ExerciseStandardSection(
    val title: String,
    val body: String,
)

@Immutable
data class ExerciseStandardIllustration(
    val assetPath: String,
    val caption: String,
)

@Immutable
data class ExerciseStandardArticle(
    val familyId: String,
    val stepLevel: Int,
    val sourceLabel: String,
    val sections: List<ExerciseStandardSection>,
    val illustrations: List<ExerciseStandardIllustration>,
)

object ExerciseStandardsCatalog {
    private val entriesByKey: Map<String, ExerciseStandardArticle> =
        GeneratedExerciseStandards.entries.associateBy(::buildKey)

    fun find(
        familyId: String,
        stepLevel: Int,
    ): ExerciseStandardArticle? = entriesByKey["$familyId:$stepLevel"]

    private fun buildKey(article: ExerciseStandardArticle): String = "${article.familyId}:${article.stepLevel}"
}
