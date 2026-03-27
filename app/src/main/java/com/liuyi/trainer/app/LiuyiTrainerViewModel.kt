package com.liuyi.trainer.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyi.trainer.data.TrainingSessionWithSets
import com.liuyi.trainer.model.ExerciseCatalog
import com.liuyi.trainer.model.TrainingSessionState
import com.liuyi.trainer.model.completeTrainingSession
import com.liuyi.trainer.model.finishCurrentSet
import com.liuyi.trainer.model.startNextSet
import com.liuyi.trainer.model.startTrainingSession
import com.liuyi.trainer.model.updateRestState
import com.liuyi.trainer.ui.ExerciseContext
import com.liuyi.trainer.ui.defaultExerciseContext
import com.liuyi.trainer.ui.defaultRestPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant

class LiuyiTrainerViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val defaultContext = defaultExerciseContext()
    private var tickerJob: Job? = null
    private val trainingHistoryRepository =
        (application as LiuyiTrainerApplication).trainingHistoryRepository

    var selectedFamilyId by mutableStateOf(defaultContext.family.id)
        private set

    var selectedStepLevel by mutableIntStateOf(defaultContext.step.level)
        private set

    var restPresetSeconds by mutableIntStateOf(defaultRestPreset().defaultRestSeconds)
        private set

    var sessionState by mutableStateOf<TrainingSessionState>(TrainingSessionState.Idle)
        private set

    var nowUtc by mutableStateOf(Instant.now())
        private set

    var recentSessions by mutableStateOf<List<TrainingSessionWithSets>>(emptyList())
        private set

    val restPresetOptions: List<Int> = defaultRestPreset().presetOptionsSeconds

    init {
        viewModelScope.launch {
            trainingHistoryRepository.observeRecentSessions().collect { sessions ->
                recentSessions = sessions
            }
        }
    }

    val selectedContext: ExerciseContext
        get() = resolveExerciseContext(
            familyId = selectedFamilyId,
            stepLevel = selectedStepLevel,
            fallback = defaultContext,
        )

    val activeContext: ExerciseContext
        get() = resolveContextForSession(
            state = sessionState,
            fallback = selectedContext,
        )

    fun selectFamily(familyId: String) {
        selectedFamilyId = familyId
        selectedStepLevel = 1
    }

    fun selectStep(stepLevel: Int) {
        selectedStepLevel = stepLevel
    }

    fun selectRestPreset(seconds: Int) {
        restPresetSeconds = seconds
    }

    fun beginTraining() {
        val startedAt = Instant.now()
        nowUtc = startedAt
        sessionState = startTrainingSession(
            familyId = selectedContext.family.id,
            stepLevel = selectedContext.step.level,
            cadenceProfile = selectedContext.family.previewCadence,
            restPreset = defaultRestPreset().copy(defaultRestSeconds = restPresetSeconds),
            startedAtUtc = startedAt,
        )
        ensureTicker()
    }

    fun finishSet() {
        val currentState = sessionState
        if (currentState !is TrainingSessionState.SetRunning) {
            return
        }

        val endedAt = Instant.now()
        nowUtc = endedAt
        sessionState = finishCurrentSet(
            state = currentState,
            endedAtUtc = endedAt,
        )
        ensureTicker()
    }

    fun beginNextSet() {
        val currentState = sessionState
        if (currentState !is TrainingSessionState.RestRunning &&
            currentState !is TrainingSessionState.RestOvertime
        ) {
            return
        }

        val startedAt = Instant.now()
        nowUtc = startedAt
        sessionState = startNextSet(
            state = currentState,
            startedAtUtc = startedAt,
        )
        ensureTicker()
    }

    fun completeTraining() {
        val currentState = sessionState
        if (currentState == TrainingSessionState.Idle ||
            currentState is TrainingSessionState.Completed
        ) {
            return
        }

        val endedAt = Instant.now()
        nowUtc = endedAt
        val completedState = completeTrainingSession(
            state = currentState,
            endedAtUtc = endedAt,
        )
        sessionState = completedState
        viewModelScope.launch {
            trainingHistoryRepository.saveCompletedSession(completedState)
        }
        ensureTicker()
    }

    private fun ensureTicker() {
        tickerJob?.cancel()
        tickerJob = when (sessionState) {
            is TrainingSessionState.SetRunning,
            is TrainingSessionState.RestRunning,
            is TrainingSessionState.RestOvertime
            -> viewModelScope.launch {
                while (isActive) {
                    val currentState = sessionState
                    val now = Instant.now()
                    nowUtc = now

                    if (currentState is TrainingSessionState.RestRunning) {
                        sessionState = updateRestState(
                            state = currentState,
                            nowUtc = now,
                        )
                    }

                    delay(100)
                }
            }

            else -> null
        }
    }
}

private fun resolveExerciseContext(
    familyId: String,
    stepLevel: Int,
    fallback: ExerciseContext,
): ExerciseContext {
    val family = ExerciseCatalog.families.firstOrNull { it.id == familyId }
        ?: fallback.family
    val step = family.steps.firstOrNull { it.level == stepLevel }
        ?: family.steps.first()
    return ExerciseContext(
        family = family,
        step = step,
    )
}

private fun resolveContextForSession(
    state: TrainingSessionState,
    fallback: ExerciseContext,
): ExerciseContext = when (state) {
    is TrainingSessionState.SetRunning -> resolveExerciseContext(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        fallback = fallback,
    )

    is TrainingSessionState.RestRunning -> resolveExerciseContext(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        fallback = fallback,
    )

    is TrainingSessionState.RestOvertime -> resolveExerciseContext(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        fallback = fallback,
    )

    is TrainingSessionState.Completed -> resolveExerciseContext(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        fallback = fallback,
    )

    TrainingSessionState.Idle -> fallback
}
