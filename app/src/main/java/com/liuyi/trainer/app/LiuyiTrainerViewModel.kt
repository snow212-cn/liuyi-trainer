package com.liuyi.trainer.app

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyi.trainer.data.TrainingSessionWithSets
import com.liuyi.trainer.data.TrainingHistoryImportMode
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
import com.liuyi.trainer.ui.TrainingBackgroundMusicOption
import com.liuyi.trainer.ui.defaultTrainingBackgroundMusicOption
import com.liuyi.trainer.ui.defaultExerciseContext
import com.liuyi.trainer.ui.defaultRestPreset
import com.liuyi.trainer.ui.findTrainingBackgroundMusicOption
import com.liuyi.trainer.ui.trainingBackgroundMusicOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Instant
import java.util.Locale

class LiuyiTrainerViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val selectionPreferences =
        application.getSharedPreferences(SELECTION_PREFS_NAME, Context.MODE_PRIVATE)
    private val defaultContext = defaultExerciseContext()
    private val restoredSelectionContext = resolveExerciseContext(
        familyId = selectionPreferences.getString(
            KEY_SELECTED_FAMILY_ID,
            defaultContext.family.id,
        ) ?: defaultContext.family.id,
        stepLevel = selectionPreferences.getInt(KEY_SELECTED_STEP_LEVEL, 1),
        fallback = defaultContext,
    )
    private var tickerJob: Job? = null
    private var voiceProbe: TextToSpeech? = null
    private val trainingHistoryRepository =
        (application as LiuyiTrainerApplication).trainingHistoryRepository

    var selectedFamilyId by mutableStateOf(restoredSelectionContext.family.id)
        private set

    var selectedStepLevel by mutableIntStateOf(restoredSelectionContext.step.level)
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

    var backgroundMusicEnabled by mutableStateOf(true)
        private set

    var selectedBackgroundMusicId by mutableStateOf(defaultTrainingBackgroundMusicOption().id)
        private set

    var lastSpokenCueToken by mutableStateOf<String?>(null)
        private set

    var lastPlayedWhistleCueToken by mutableStateOf<String?>(null)
        private set

    var lastPlayedWhistleCompletedAtMs by mutableLongStateOf(0L)
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

    var historyTransferStatus by mutableStateOf<String?>(null)
        private set

    var latestHistoryExportUri by mutableStateOf<String?>(null)
        private set

    var latestHistoryExportLabel by mutableStateOf<String?>(null)
        private set

    var customEccentricSeconds by mutableIntStateOf(2)
        private set

    var customBottomPauseSeconds by mutableIntStateOf(1)
        private set

    var customConcentricSeconds by mutableIntStateOf(2)
        private set

    val restPresetOptions: List<Int> = defaultRestPreset().presetOptionsSeconds
    val preparationOptions: List<Int> = listOf(3, 5, 8)
    val backgroundMusicOptions: List<TrainingBackgroundMusicOption> = trainingBackgroundMusicOptions

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
        persistSelection()
    }

    fun selectStep(stepLevel: Int) {
        selectedStepLevel = stepLevel
        persistSelection()
    }

    fun selectRestPreset(seconds: Int) {
        restPresetSeconds = seconds
    }

    fun updateSpeechEnabled(enabled: Boolean) {
        speechEnabled = enabled
        if (!enabled) {
            clearSpeechCueTracking()
        }
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

    fun updateBackgroundMusicEnabled(enabled: Boolean) {
        backgroundMusicEnabled = enabled
    }

    fun updateSelectedBackgroundMusic(trackId: String) {
        if (findTrainingBackgroundMusicOption(trackId) != null) {
            selectedBackgroundMusicId = trackId
        }
    }

    fun updateCustomEccentricSeconds(seconds: Int) {
        customEccentricSeconds = seconds.coerceIn(0, 10)
    }

    fun updateCustomBottomPauseSeconds(seconds: Int) {
        customBottomPauseSeconds = seconds.coerceIn(0, 10)
    }

    fun updateCustomConcentricSeconds(seconds: Int) {
        customConcentricSeconds = seconds.coerceIn(0, 10)
    }

    fun markSpeechCueSpoken(cueToken: String) {
        lastSpokenCueToken = cueToken
    }

    fun markWhistleCuePlayed(cueToken: String) {
        lastPlayedWhistleCueToken = cueToken
        lastPlayedWhistleCompletedAtMs = SystemClock.elapsedRealtime()
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
        persistSelection()
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

    private fun persistSelection() {
        selectionPreferences.edit()
            .putString(KEY_SELECTED_FAMILY_ID, selectedFamilyId)
            .putInt(KEY_SELECTED_STEP_LEVEL, selectedStepLevel)
            .apply()
    }

    fun beginTraining() {
        val preparedAt = Instant.now()
        nowUtc = preparedAt
        summaryRepDrafts = emptyList()
        summarySaved = false
        val customCadence = com.liuyi.trainer.model.CadenceProfile(
            id = "custom",
            label = "${customEccentricSeconds}-${customBottomPauseSeconds}-${customConcentricSeconds} 自定义节奏",
            eccentricSeconds = customEccentricSeconds,
            bottomPauseSeconds = customBottomPauseSeconds,
            concentricSeconds = customConcentricSeconds,
            topPauseSeconds = 0,
            source = "用户自定义节奏"
        )
        sessionState = prepareTrainingSession(
            familyId = selectedContext.family.id,
            stepLevel = selectedContext.step.level,
            cadenceProfile = customCadence,
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

    fun exportHistoryBackup(uri: Uri) {
        historyTransferStatus = "正在导出训练历史…"
        viewModelScope.launch {
            runCatching {
                val backupJson = trainingHistoryRepository.exportBackupJson()
                writeTextToUri(uri, backupJson)
                latestHistoryExportUri = uri.toString()
                latestHistoryExportLabel = queryDisplayName(uri) ?: "liuyi-history-backup.json"
                historyTransferStatus = "已导出 ${recentSessions.size} 条历史记录"
            }.onFailure { error ->
                historyTransferStatus = "导出失败：${error.toReadableBackupMessage()}"
            }
        }
    }

    fun importHistoryBackup(
        uri: Uri,
        mode: TrainingHistoryImportMode,
    ) {
        historyTransferStatus = "正在导入训练历史…"
        historyEditsDirty = false
        viewModelScope.launch {
            runCatching {
                val rawJson = readTextFromUri(uri)
                val result = trainingHistoryRepository.importBackupJson(
                    rawJson = rawJson,
                    mode = mode,
                )
                selectedHistorySessionId = -1L
                historyTransferStatus = when (mode) {
                    TrainingHistoryImportMode.Replace ->
                        "已替换导入 ${result.sessionCount} 条记录，共 ${result.setCount} 组"

                    TrainingHistoryImportMode.Merge ->
                        "已合并导入 ${result.sessionCount} 条记录，共 ${result.setCount} 组"
                }
            }.onFailure { error ->
                historyTransferStatus = "导入失败：${error.toReadableBackupMessage()}"
            }
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

    private fun clearSpeechCueTracking() {
        lastSpokenCueToken = null
        lastPlayedWhistleCueToken = null
        lastPlayedWhistleCompletedAtMs = 0L
    }

    private fun writeTextToUri(
        uri: Uri,
        text: String,
    ) {
        val outputStream = getApplication<Application>().contentResolver.openOutputStream(uri)
            ?: throw IOException("系统没有返回可写入的备份文件")
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(text)
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            ?: throw IOException("无法读取所选备份文件")
        return inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        val resolver = getApplication<Application>().contentResolver
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { currentCursor ->
            if (currentCursor.moveToFirst()) {
                return currentCursor.getString(0)?.takeIf(String::isNotBlank)
            }
        }
        return null
    }
}

private const val SELECTION_PREFS_NAME = "liuyi_trainer_selection"
private const val KEY_SELECTED_FAMILY_ID = "selected_family_id"
private const val KEY_SELECTED_STEP_LEVEL = "selected_step_level"

private fun defaultVoiceOption(): DeviceVoiceOption = DeviceVoiceOption(
    id = "",
    label = "系统默认",
)

private fun buildVoiceOptions(voices: Collection<Voice>): List<DeviceVoiceOption> {
    val usableVoices = voices.filter(::isUsableVoice)
    if (usableVoices.isEmpty()) {
        return emptyList()
    }

    val preferredLocale = Locale.getDefault()
    val localVoices = usableVoices
        .filter(::supportsEmbeddedSynthesis)
        .ifEmpty { usableVoices }
    val prioritizedVoices = localVoices
        .filter { voice -> isRelevantTrainingVoice(voice.locale, preferredLocale) }
        .ifEmpty { localVoices }

    return prioritizedVoices
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
                ?.toDeviceVoiceOptionForLocale(preferredLocale)
        }
        .distinctBy { option -> option.label }
        .sortedWith(
            compareByDescending<DeviceVoiceOption> { option ->
                option.isRecommended
            }.thenByDescending { option ->
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

private fun isUsableVoice(voice: Voice): Boolean {
    val features = voice.features.orEmpty()
    return !voice.isNetworkConnectionRequired &&
        !features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
}

private fun supportsEmbeddedSynthesis(voice: Voice): Boolean {
    val features = voice.features.orEmpty()
    return features.isEmpty() || features.contains(TextToSpeech.Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS)
}

private fun isRelevantTrainingVoice(
    locale: Locale,
    preferredLocale: Locale,
): Boolean {
    val language = locale.language
    return locale.toLanguageTag() == preferredLocale.toLanguageTag() ||
        language == preferredLocale.language ||
        language == Locale.SIMPLIFIED_CHINESE.language
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

private fun Voice.toDeviceVoiceOptionForLocale(preferredLocale: Locale): DeviceVoiceOption {
    val localeTag = locale.toLanguageTag()
    val localeLabel = locale.toReadableVoiceLocaleLabel()
    val shortName = name
        .substringAfterLast('/')
        .substringAfterLast('#')
        .substringAfterLast('.')
        .ifBlank { name }
    val readableName = shortName.takeIf(::looksLikeHumanReadableVoiceName)
    val label = readableName?.let { "$localeLabel · $it" } ?: localeLabel

    return DeviceVoiceOption(
        id = name,
        label = label,
        languageTag = localeTag,
        isRecommended = isRelevantTrainingVoice(locale, preferredLocale),
    )
}

private fun Locale.toReadableVoiceLocaleLabel(): String {
    if (language == Locale.SIMPLIFIED_CHINESE.language) {
        val scriptLabel = when (script) {
            "Hans" -> "简体"
            "Hant" -> "繁体"
            else -> null
        }
        val regionLabel = when (country.uppercase(Locale.ROOT)) {
            "CN" -> "大陆"
            "SG" -> "新加坡"
            "TW" -> "台湾"
            "HK" -> "香港"
            "MO" -> "澳门"
            else -> country.takeIf { it.isNotBlank() }?.let { getDisplayCountry(Locale.SIMPLIFIED_CHINESE) }
        }
        return listOfNotNull("中文", scriptLabel, regionLabel)
            .distinct()
            .joinToString("·")
            .ifBlank { "中文" }
    }

    val languageLabel = getDisplayLanguage(Locale.SIMPLIFIED_CHINESE).ifBlank { getDisplayLanguage(this) }
    val regionLabel = getDisplayCountry(Locale.SIMPLIFIED_CHINESE).takeIf { it.isNotBlank() }
    return listOfNotNull(languageLabel, regionLabel).joinToString("·")
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

private fun Throwable.toReadableBackupMessage(): String = when (this) {
    is IllegalArgumentException -> message ?: "备份文件内容不完整"
    is IOException -> message ?: "文件读写失败"
    else -> "请确认文件格式正确，且系统文件管理器允许当前读写操作"
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
