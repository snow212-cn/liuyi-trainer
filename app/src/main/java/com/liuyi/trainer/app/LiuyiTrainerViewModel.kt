package com.liuyi.trainer.app

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyi.trainer.data.TrainingSessionWithSets
import com.liuyi.trainer.model.DeviceVoiceOption
import com.liuyi.trainer.model.ExerciseCatalog
import com.liuyi.trainer.model.TrainingSessionState
import com.liuyi.trainer.model.VoiceGuideMode
import com.liuyi.trainer.model.completeTrainingSession
import com.liuyi.trainer.model.finishCurrentSet
import com.liuyi.trainer.model.prepareNextSet
import com.liuyi.trainer.model.prepareTrainingSession
import com.liuyi.trainer.model.updatePreparingState
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
import java.util.Locale

class LiuyiTrainerViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val defaultContext = defaultExerciseContext()
    private var tickerJob: Job? = null
    private var voiceProbe: TextToSpeech? = null
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

    var speechEnabled by mutableStateOf(true)
        private set

    var voiceGuideMode by mutableStateOf(VoiceGuideMode.Command)
        private set

    var availableVoices by mutableStateOf<List<DeviceVoiceOption>>(listOf(defaultVoiceOption()))
        private set

    var selectedVoiceId by mutableStateOf(defaultVoiceOption().id)
        private set

    var preparationSeconds by mutableIntStateOf(3)
        private set

    var restCountdownVoiceEnabled by mutableStateOf(true)
        private set

    var summaryRepDrafts by mutableStateOf<List<String>>(emptyList())
        private set

    var summarySaved by mutableStateOf(false)
        private set

    var selectedHistorySessionId by mutableLongStateOf(-1L)
        private set

    var historyRepDrafts by mutableStateOf<List<String>>(emptyList())
        private set

    var historyEditsDirty by mutableStateOf(false)
        private set

    val restPresetOptions: List<Int> = defaultRestPreset().presetOptionsSeconds
    val preparationOptions: List<Int> = listOf(3, 5, 8)

    init {
        loadAvailableVoices()
        viewModelScope.launch {
            trainingHistoryRepository.observeRecentSessions().collect { sessions ->
                recentSessions = sessions
                if (selectedHistorySessionId == -1L && sessions.isNotEmpty()) {
                    selectedHistorySessionId = sessions.first().session.sessionId
                } else if (
                    selectedHistorySessionId != -1L &&
                    sessions.none { it.session.sessionId == selectedHistorySessionId }
                ) {
                    selectedHistorySessionId = sessions.firstOrNull()?.session?.sessionId ?: -1L
                }
                syncHistoryDrafts()
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

    val selectedHistorySession: TrainingSessionWithSets?
        get() = recentSessions.firstOrNull { it.session.sessionId == selectedHistorySessionId }

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

    fun updateSpeechEnabled(enabled: Boolean) {
        speechEnabled = enabled
    }

    fun updateVoiceGuideMode(mode: VoiceGuideMode) {
        voiceGuideMode = mode
    }

    fun updateSelectedVoice(voiceId: String) {
        selectedVoiceId = voiceId
    }

    fun updatePreparationSeconds(seconds: Int) {
        preparationSeconds = seconds
    }

    fun updateRestCountdownVoiceEnabled(enabled: Boolean) {
        restCountdownVoiceEnabled = enabled
    }

    fun selectHistorySession(sessionId: Long) {
        selectedHistorySessionId = sessionId
        historyEditsDirty = false
        syncHistoryDrafts()
    }

    fun loadSelectedHistoryAsCurrent() {
        val session = selectedHistorySession ?: return
        selectedFamilyId = session.session.familyId
        selectedStepLevel = session.session.stepLevel
        restPresetSeconds = session.session.restPresetSeconds
        sessionState = TrainingSessionState.Idle
        summaryRepDrafts = emptyList()
        summarySaved = false
        tickerJob?.cancel()
        tickerJob = null
        nowUtc = Instant.now()
    }

    fun deleteSelectedHistorySession() {
        val sessionId = selectedHistorySessionId
        if (sessionId == -1L) {
            return
        }
        viewModelScope.launch {
            trainingHistoryRepository.deleteSession(sessionId)
        }
    }

    fun beginTraining() {
        val preparedAt = Instant.now()
        nowUtc = preparedAt
        summaryRepDrafts = emptyList()
        summarySaved = false
        sessionState = prepareTrainingSession(
            familyId = selectedContext.family.id,
            stepLevel = selectedContext.step.level,
            cadenceProfile = selectedContext.family.previewCadence,
            restPreset = defaultRestPreset().copy(defaultRestSeconds = restPresetSeconds),
            prepareStartedAtUtc = preparedAt,
            preparationSeconds = preparationSeconds,
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

        val preparedAt = Instant.now()
        nowUtc = preparedAt
        sessionState = prepareNextSet(
            state = currentState,
            prepareStartedAtUtc = preparedAt,
            preparationSeconds = preparationSeconds,
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
        summaryRepDrafts = completedState.completedSets.map { it.completedRepCount.toString() }
        summarySaved = false
        ensureTicker()
    }

    fun updateSummaryRep(
        index: Int,
        value: String,
    ) {
        if (summarySaved) {
            return
        }
        if (index !in summaryRepDrafts.indices) {
            return
        }

        val sanitized = value.filter(Char::isDigit).take(4)
        summaryRepDrafts = summaryRepDrafts.mapIndexed { currentIndex, currentValue ->
            if (currentIndex == index) {
                sanitized
            } else {
                currentValue
            }
        }
    }

    fun saveCompletedTraining() {
        val currentState = sessionState
        if (currentState !is TrainingSessionState.Completed || summarySaved) {
            return
        }

        val adjustedSets = currentState.completedSets.mapIndexed { index, set ->
            set.copy(
                completedRepCount = summaryRepDrafts.getOrNull(index)?.toIntOrNull()
                    ?: set.completedRepCount,
            )
        }
        val adjustedState = currentState.copy(completedSets = adjustedSets)
        sessionState = adjustedState
        summarySaved = true
        viewModelScope.launch {
            trainingHistoryRepository.saveCompletedSession(adjustedState)
        }
    }

    fun updateHistoryRep(
        index: Int,
        value: String,
    ) {
        if (index !in historyRepDrafts.indices) {
            return
        }

        val sanitized = value.filter(Char::isDigit).take(4)
        historyRepDrafts = historyRepDrafts.mapIndexed { currentIndex, currentValue ->
            if (currentIndex == index) {
                sanitized
            } else {
                currentValue
            }
        }
        historyEditsDirty = true
    }

    fun saveSelectedHistoryEdits() {
        val session = selectedHistorySession ?: return
        val sortedSets = session.sets.sortedBy { it.setIndex }
        if (sortedSets.isEmpty()) {
            return
        }

        val repUpdates = sortedSets.mapIndexed { index, set ->
            set.setId to (historyRepDrafts.getOrNull(index)?.toIntOrNull() ?: set.completedRepCount)
        }

        viewModelScope.launch {
            trainingHistoryRepository.updateSessionRepCounts(
                sessionId = session.session.sessionId,
                setRepUpdates = repUpdates,
            )
            historyEditsDirty = false
        }
    }

    fun finishActiveTrainingFromAnywhere(): Boolean {
        return when (sessionState) {
            TrainingSessionState.Idle -> false
            is TrainingSessionState.Completed -> true
            is TrainingSessionState.PreparingSet -> {
                sessionState = TrainingSessionState.Idle
                tickerJob?.cancel()
                tickerJob = null
                nowUtc = Instant.now()
                false
            }

            else -> {
                completeTraining()
                true
            }
        }
    }

    private fun ensureTicker() {
        tickerJob?.cancel()
        tickerJob = when (sessionState) {
            is TrainingSessionState.PreparingSet,
            is TrainingSessionState.SetRunning,
            is TrainingSessionState.RestRunning,
            is TrainingSessionState.RestOvertime
            -> viewModelScope.launch {
                while (isActive) {
                    val currentState = sessionState
                    val now = Instant.now()
                    nowUtc = now

                    sessionState = when (currentState) {
                        is TrainingSessionState.PreparingSet -> updatePreparingState(
                            state = currentState,
                            nowUtc = now,
                        )

                        is TrainingSessionState.RestRunning -> updateRestState(
                            state = currentState,
                            nowUtc = now,
                        )

                        else -> currentState
                    }

                    delay(100)
                }
            }

            else -> null
        }
    }

    private fun syncHistoryDrafts() {
        if (historyEditsDirty) {
            return
        }

        historyRepDrafts = selectedHistorySession
            ?.sets
            ?.sortedBy { it.setIndex }
            ?.map { it.completedRepCount.toString() }
            ?: emptyList()
    }

    private fun loadAvailableVoices() {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val discoveredVoices = buildVoiceOptions(engine?.voices.orEmpty())

                availableVoices = listOf(defaultVoiceOption()) + discoveredVoices
                if (availableVoices.none { it.id == selectedVoiceId }) {
                    selectedVoiceId = defaultVoiceOption().id
                }
            } else {
                availableVoices = listOf(defaultVoiceOption())
                selectedVoiceId = defaultVoiceOption().id
            }

            engine?.shutdown()
            if (voiceProbe === engine) {
                voiceProbe = null
            }
        }
        voiceProbe = engine
    }

    override fun onCleared() {
        voiceProbe?.shutdown()
        voiceProbe = null
        super.onCleared()
    }
}

private fun defaultVoiceOption(): DeviceVoiceOption = DeviceVoiceOption(
    id = "",
    label = "系统默认",
)

private fun buildVoiceOptions(voices: Collection<Voice>): List<DeviceVoiceOption> {
    val filteredVoices = voices.filter { voice ->
        !voice.isNetworkConnectionRequired &&
            !voice.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
    }
    if (filteredVoices.isEmpty()) {
        return emptyList()
    }

    val preferredLocale = Locale.getDefault()

    return filteredVoices
        .groupBy { voice -> voice.locale.toLanguageTag() }
        .values
        .mapNotNull { sameLocaleVoices ->
            sameLocaleVoices
                .maxWithOrNull(
                    compareBy<Voice> { voice ->
                        voiceSelectionScore(
                            voice = voice,
                            preferredLocale = preferredLocale,
                        )
                    }.thenByDescending { it.quality }
                        .thenBy { -it.latency },
                )
                ?.toDeviceVoiceOptionForLocale()
        }
        .sortedWith(
            compareByDescending<DeviceVoiceOption> { option ->
                option.languageTag == preferredLocale.toLanguageTag()
            }.thenByDescending { option ->
                option.languageTag.substringBefore('-') == preferredLocale.language
            }.thenByDescending { option ->
                option.languageTag.substringBefore('-') == Locale.SIMPLIFIED_CHINESE.language
            }.thenBy { option ->
                option.label
            },
        )
}

private fun voiceSelectionScore(
    voice: Voice,
    preferredLocale: Locale,
): Int {
    val tag = voice.locale.toLanguageTag()
    val language = tag.substringBefore('-')
    var score = 0

    if (tag == preferredLocale.toLanguageTag()) {
        score += 400
    }
    if (language == preferredLocale.language) {
        score += 200
    }
    if (language == Locale.SIMPLIFIED_CHINESE.language) {
        score += 100
    }
    if (language == Locale.ENGLISH.language) {
        score += 20
    }

    score += voice.quality
    score -= voice.latency
    return score
}

private fun Voice.toDeviceVoiceOptionForLocale(): DeviceVoiceOption {
    val localeTag = locale.toLanguageTag()
    val localeLabel = locale.getDisplayName(Locale.SIMPLIFIED_CHINESE)
    val shortName = name
        .substringAfterLast('/')
        .substringAfterLast('#')
        .substringAfterLast('.')
        .ifBlank { name }
    val readableName = shortName.takeIf(::looksLikeHumanReadableVoiceName)
    val label = readableName?.let { "$it · $localeLabel" } ?: localeLabel

    return DeviceVoiceOption(
        id = name,
        label = label,
        languageTag = localeTag,
    )
}

private fun looksLikeHumanReadableVoiceName(name: String): Boolean {
    if (name.length <= 1) {
        return false
    }
    if (name.equals("default", ignoreCase = true)) {
        return false
    }
    if (Regex("^[a-z]{2,3}([-_][A-Za-z0-9]{2,})+$").matches(name)) {
        return false
    }

    val stripped = name.replace(Regex("[_\\-]"), "")
    val hasLetters = stripped.any { it.isLetter() }
    val hasOnlyLettersAndDigits = stripped.all { it.isLetterOrDigit() }
    return hasLetters && !hasOnlyLettersAndDigits
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
    is TrainingSessionState.PreparingSet -> resolveExerciseContext(
        familyId = state.familyId,
        stepLevel = state.stepLevel,
        fallback = fallback,
    )

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
