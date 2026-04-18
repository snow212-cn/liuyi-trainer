package com.liuyi.trainer.ui

import android.content.ClipData
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.liuyi.trainer.data.TrainingSessionWithSets
import com.liuyi.trainer.data.TrainingHistoryImportMode
import com.liuyi.trainer.model.CadencePhase
import com.liuyi.trainer.model.DeviceVoiceOption
import com.liuyi.trainer.model.ExerciseCatalog
import com.liuyi.trainer.model.ExerciseStandardsCatalog
import com.liuyi.trainer.model.MovementFamily
import com.liuyi.trainer.model.MovementStep
import com.liuyi.trainer.model.RestPreset
import com.liuyi.trainer.model.TrainingSessionState
import com.liuyi.trainer.model.VoiceGuideMode
import com.liuyi.trainer.model.WorkoutSetResult
import com.liuyi.trainer.model.snapshotRestState
import com.liuyi.trainer.model.trackLiveCadence
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val UiTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("M月d日 HH:mm").withZone(ZoneId.systemDefault())

private val UiClockFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

private val UiMonthFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy年M月").withZone(ZoneId.systemDefault())

data class ExerciseContext(
    val family: MovementFamily,
    val step: MovementStep,
)

data class TrainingEntryPreview(
    val context: ExerciseContext,
    val cadenceLabel: String,
    val cadenceSeconds: Long,
    val restPresetLabel: String,
)

data class PreparingPreview(
    val context: ExerciseContext,
    val currentSetIndex: Int,
    val completedSetCount: Int,
    val totalRepCount: Int,
    val countdownLabel: String,
    val hintLabel: String,
    val speechCueKey: String?,
    val speechCueText: String?,
)

data class RunningPreview(
    val context: ExerciseContext,
    val currentPhaseLabel: String,
    val phaseSecondLabel: String,
    val phaseCueText: String,
    val activeBeatIndex: Int,
    val speechCueKey: String?,
    val speechCueText: String?,
    val currentRepCount: Int,
    val currentSetIndex: Int,
    val completedSetCount: Int,
    val totalRepCount: Int,
    val sessionElapsedLabel: String,
)

data class RestPreview(
    val context: ExerciseContext,
    val completedSetCount: Int,
    val totalRepCount: Int,
    val restHeadline: String,
    val restTimeLabel: String,
    val restHint: String,
    val presetLabel: String,
    val speechCueKey: String?,
    val speechCueText: String?,
)

data class SummarySetRowPreview(
    val setIndex: Int,
    val repValue: String,
    val durationLabel: String,
    val endedAtLabel: String,
)

data class SummaryPreview(
    val context: ExerciseContext,
    val sessionStartLabel: String,
    val sessionEndLabel: String,
    val totalSets: Int,
    val totalReps: Int,
    val setRows: List<SummarySetRowPreview>,
    val isEditable: Boolean,
    val isSaved: Boolean,
)

data class StandardsPreview(
    val context: ExerciseContext,
    val sourceLabel: String,
    val sections: List<Pair<String, String>>,
    val illustrations: List<StandardsIllustrationPreview>,
)

data class StandardsIllustrationPreview(
    val assetPath: String,
    val caption: String,
)

data class TrainingSettingsPreview(
    val speechEnabled: Boolean,
    val voiceGuideMode: VoiceGuideMode,
    val voiceModeLabel: String,
    val availableVoices: List<DeviceVoiceOption>,
    val selectedVoiceId: String,
    val selectedVoiceLabel: String,
    val restPresetSeconds: Int,
    val restPresetOptions: List<Int>,
    val preparationSeconds: Int,
    val preparationOptions: List<Int>,
    val restCountdownVoiceEnabled: Boolean,
)

data class HistoryRowPreview(
    val sessionId: Long,
    val familyLabel: String,
    val stepLabel: String,
    val title: String,
    val totalRepsLabel: String,
    val setCountLabel: String,
    val setPreview: String,
    val monthLabel: String,
    val dateLabel: String,
)

data class HistoryPreview(
    val rows: List<HistoryRowPreview>,
    val transferStatus: String?,
    val latestExportLabel: String?,
    val latestExportUri: String?,
)

data class HistorySetDetailPreview(
    val setId: Long,
    val setIndex: Int,
    val repValue: String,
    val durationLabel: String,
    val restAfterLabel: String,
    val endedAtLabel: String,
)

data class HistoryDetailPreview(
    val sessionId: Long,
    val title: String,
    val totalRepsLabel: String,
    val totalSetsLabel: String,
    val startedAtLabel: String,
    val endedAtLabel: String,
    val durationLabel: String,
    val restPresetLabel: String,
    val setDetails: List<HistorySetDetailPreview>,
    val isSaveEnabled: Boolean,
)

@Composable
fun TrainingReadyScreen(
    preview: TrainingEntryPreview,
    onBack: () -> Unit,
    onStartSet: () -> Unit,
    onOpenStandards: () -> Unit,
) {
    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "训练准备",
                actionLabel = "返回首页",
                onAction = onBack,
            )

            SteelPanel {
                SectionKicker(text = "准备")
                Text(
                    text = "${preview.context.family.titleZh}·${preview.context.step.label}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MutedBody(text = "原书节奏 2-1-2")
                    MutedBody(text = preview.restPresetLabel)
                }
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "本次配置",
                    subtitle = "手动开始",
                )
                ReadySettingRow(label = "完整循环", value = "${preview.cadenceSeconds} 秒")
                ReadySettingRow(label = "训练设置", value = preview.cadenceLabel)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SteelPrimaryButton(
                    text = "开始本组",
                    onClick = onStartSet,
                    modifier = Modifier.weight(1f),
                )
                SteelSecondaryButton(
                    text = "动作标准",
                    onClick = onOpenStandards,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun TrainingPreparationScreen(
    preview: PreparingPreview,
    speechEnabled: Boolean,
    selectedVoiceId: String,
    onBack: () -> Unit,
) {
    SpeechCueEffect(
        enabled = speechEnabled,
        cueKey = preview.speechCueKey,
        cueText = preview.speechCueText,
        selectedVoiceId = selectedVoiceId,
    )

    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "准备开始",
                actionLabel = "返回首页",
                onAction = onBack,
            )

            StatusStrip(
                lines = listOf(
                    "${preview.context.family.titleZh}·${preview.context.step.label}",
                    "第${preview.currentSetIndex}组 · 已完成${preview.completedSetCount}组",
                ),
            )

            SteelPanel {
                SectionKicker(text = "就位")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(228.dp)
                            .clip(CircleShape)
                            .border(
                                width = 8.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                    ),
                                ),
                                shape = CircleShape,
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.background,
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = preview.countdownLabel,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            MutedBody(text = preview.hintLabel)
                        }
                    }
                }
                MetricPlate(
                    label = "累计次数",
                    value = preview.totalRepCount.toString(),
                )
            }
        }
    }
}

@Composable
fun TrainingRunningScreen(
    preview: RunningPreview,
    selectedVoiceId: String,
    onBack: () -> Unit,
    onFinishSet: () -> Unit,
    onCompleteTraining: () -> Unit,
) {
    SpeechCueEffect(
        enabled = preview.speechCueText != null,
        cueKey = preview.speechCueKey,
        cueText = preview.speechCueText,
        selectedVoiceId = selectedVoiceId,
    )

    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "训练执行",
                actionLabel = "返回首页",
                onAction = onBack,
            )

            StatusStrip(
                lines = listOf(
                    "${preview.context.family.titleZh}·${preview.context.step.label}",
                    "第${preview.currentSetIndex}组 · 已完成${preview.completedSetCount}组",
                ),
            )

            CadenceCorePanel(
                phaseLabel = preview.currentPhaseLabel,
                phaseSecondLabel = preview.phaseSecondLabel,
                cueText = preview.phaseCueText,
                activeBeatIndex = preview.activeBeatIndex,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricPlate(
                    modifier = Modifier.weight(1f),
                    label = "本组次数",
                    value = preview.currentRepCount.toString(),
                )
                MetricPlate(
                    modifier = Modifier.weight(1f),
                    label = "累计次数",
                    value = preview.totalRepCount.toString(),
                )
                MetricPlate(
                    modifier = Modifier.weight(1f),
                    label = "已训练",
                    value = preview.sessionElapsedLabel,
                    compact = true,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SteelPrimaryButton(
                    text = "结束本组",
                    onClick = onFinishSet,
                    modifier = Modifier.weight(1f),
                )
                SteelSecondaryButton(
                    text = "结束训练",
                    onClick = onCompleteTraining,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun TrainingRestScreen(
    preview: RestPreview,
    speechEnabled: Boolean,
    selectedVoiceId: String,
    onBack: () -> Unit,
    onStartNextSet: () -> Unit,
    onCompleteTraining: () -> Unit,
) {
    SpeechCueEffect(
        enabled = speechEnabled,
        cueKey = preview.speechCueKey,
        cueText = preview.speechCueText,
        selectedVoiceId = selectedVoiceId,
    )

    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "组间休息",
                actionLabel = "返回首页",
                onAction = onBack,
            )

            StatusStrip(
                lines = listOf(
                    "${preview.context.family.titleZh}·${preview.context.step.label}",
                    "已完成${preview.completedSetCount}组 · 累计${preview.totalRepCount}次",
                    "休息预设 ${preview.presetLabel}",
                ),
            )

            SteelPanel {
                SectionKicker(text = "休息")
                Text(
                    text = preview.restHeadline,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = preview.restTimeLabel,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                )
                MutedBody(text = preview.restHint)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricPlate(modifier = Modifier.weight(1f), label = "已完成组数", value = preview.completedSetCount.toString())
                MetricPlate(modifier = Modifier.weight(1f), label = "累计次数", value = preview.totalRepCount.toString())
                MetricPlate(modifier = Modifier.weight(1f), label = "休息预设", value = preview.presetLabel, compact = true)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SteelPrimaryButton(
                    text = "开始下一组",
                    onClick = onStartNextSet,
                    modifier = Modifier.weight(1f),
                )
                SteelSecondaryButton(
                    text = "结束本次训练",
                    onClick = onCompleteTraining,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun TrainingSummaryScreen(
    preview: SummaryPreview,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onOpenHistory: () -> Unit,
    onUpdateRep: (Int, String) -> Unit,
    onSave: () -> Unit,
) {
    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "训练总结",
                actionLabel = "返回上一页",
                onAction = onBack,
            )

            SteelPanel {
                SectionKicker(text = "${preview.context.family.titleZh}·${preview.context.step.label}")
                Text(
                    text = "共${preview.totalSets}组 · ${preview.totalReps}次",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
                MutedBody(text = "${preview.sessionStartLabel} - ${preview.sessionEndLabel}")
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "保存前校对",
                    subtitle = if (preview.isSaved) "已保存" else "可修正",
                )
                if (preview.setRows.isEmpty()) {
                    MutedBody(text = "当前还没有完成的组记录。")
                } else {
                    preview.setRows.forEachIndexed { index, row ->
                        SummarySetEditor(
                            row = row,
                            editable = preview.isEditable && !preview.isSaved,
                            onValueChange = { onUpdateRep(index, it) },
                        )
                        if (index != preview.setRows.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SteelSecondaryButton(
                    text = "回到首页",
                    onClick = onBackHome,
                    modifier = Modifier.weight(1f),
                )
                SteelSecondaryButton(
                    text = if (preview.isSaved) "查看历史" else "历史列表",
                    onClick = onOpenHistory,
                    modifier = Modifier.weight(1f),
                )
            }

            if (preview.isEditable && !preview.isSaved) {
                SteelPrimaryButton(
                    text = "保存到历史",
                    onClick = onSave,
                )
            }
        }
    }
}

@Composable
fun TrainingSettingsScreen(
    preview: TrainingSettingsPreview,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onUpdateSpeechEnabled: (Boolean) -> Unit,
    onUpdateVoiceGuideMode: (VoiceGuideMode) -> Unit,
    onUpdateSelectedVoice: (String) -> Unit,
    onUpdateRestPreset: (Int) -> Unit,
    onUpdatePreparationSeconds: (Int) -> Unit,
    onUpdateRestCountdownVoiceEnabled: (Boolean) -> Unit,
) {
    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "训练设置",
                actionLabel = "返回上一页",
                onAction = onBack,
            )

            SteelPanel {
                SectionKicker(text = "语音与节奏")
                ReadySettingRow(label = "原书节奏", value = "2-1-2")
                ReadySettingRow(label = "当前语音", value = preview.voiceModeLabel)
                ReadySettingRow(label = "组间休息", value = "${preview.restPresetSeconds} 秒")
                ReadySettingRow(label = "起组准备", value = "${preview.preparationSeconds} 秒")
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "语音总开关",
                    subtitle = if (preview.speechEnabled) "已开启" else "已关闭",
                )
                BooleanOptionRow(
                    enabled = preview.speechEnabled,
                    enabledText = "语音开启",
                    disabledText = "语音关闭",
                    onToggle = onUpdateSpeechEnabled,
                )
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "训练语音模式",
                    subtitle = preview.voiceModeLabel,
                )
                VoiceModeSelector(
                    selectedMode = preview.voiceGuideMode,
                    onSelect = onUpdateVoiceGuideMode,
                )
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "语音人物",
                    subtitle = preview.selectedVoiceLabel,
                )
                VoicePersonSelector(
                    voices = preview.availableVoices,
                    selectedVoiceId = preview.selectedVoiceId,
                    onSelect = onUpdateSelectedVoice,
                )
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "起组准备",
                    subtitle = "${preview.preparationSeconds} 秒",
                )
                SecondsSelector(
                    options = preview.preparationOptions,
                    selectedSeconds = preview.preparationSeconds,
                    onSelect = onUpdatePreparationSeconds,
                )
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "组间休息",
                    subtitle = "${preview.restPresetSeconds} 秒",
                )
                SecondsSelector(
                    options = preview.restPresetOptions,
                    selectedSeconds = preview.restPresetSeconds,
                    onSelect = onUpdateRestPreset,
                )
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "休息倒数播报",
                    subtitle = if (preview.restCountdownVoiceEnabled) "最后 3 秒播报" else "关闭",
                )
                BooleanOptionRow(
                    enabled = preview.restCountdownVoiceEnabled,
                    enabledText = "最后 3 秒播报",
                    disabledText = "不播报",
                    onToggle = onUpdateRestCountdownVoiceEnabled,
                )
            }

            SteelSecondaryButton(
                text = "回到首页",
                onClick = onBackHome,
            )
        }
    }
}

@Composable
fun StandardsScreen(
    preview: StandardsPreview,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onOpenTraining: () -> Unit,
) {
    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "动作标准",
                actionLabel = "返回上一页",
                onAction = onBack,
            )

            SteelPanel {
                SectionKicker(text = "动作标准")
                Text(
                    text = "${preview.context.family.titleZh}·${preview.context.step.label}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
                MutedBody(text = preview.sourceLabel)
            }

            SteelPanel(soft = true) {
                Text(
                    text = "动作示意",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (preview.illustrations.isEmpty()) {
                    StandardsIllustrationFrame()
                } else {
                    StandardsIllustrationGallery(illustrations = preview.illustrations)
                }
            }

            preview.sections.forEach { (title, body) ->
                SteelPanel(soft = true) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SteelPrimaryButton(
                    text = "进入训练",
                    onClick = onOpenTraining,
                    modifier = Modifier.weight(1f),
                )
                SteelSecondaryButton(
                    text = "回到首页",
                    onClick = onBackHome,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun TrainingHistoryScreen(
    preview: HistoryPreview,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onExportToUri: (Uri) -> Unit,
    onImportFromUri: (Uri, TrainingHistoryImportMode) -> Unit,
    onOpenDetail: (Long) -> Unit,
) {
    val context = LocalContext.current
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    val familyOptions = remember(preview.rows) {
        listOf("全部六艺") + preview.rows.map { it.familyLabel }.distinct()
    }
    var selectedFamily by remember(preview.rows) { mutableStateOf(familyOptions.first()) }
    val stepOptions = remember(preview.rows, selectedFamily) {
        listOf("全部动作") + preview.rows
            .filter { selectedFamily == "全部六艺" || it.familyLabel == selectedFamily }
            .map { it.stepLabel }
            .distinct()
    }
    val monthOptions = remember(preview.rows) {
        listOf("全部月份") + preview.rows.map { it.monthLabel }.distinct()
    }
    var selectedStep by remember(preview.rows, selectedFamily) { mutableStateOf(stepOptions.first()) }
    var selectedMonth by remember(preview.rows) { mutableStateOf(monthOptions.first()) }
    val filteredRows = remember(preview.rows, selectedFamily, selectedStep, selectedMonth) {
        preview.rows.filter { row ->
            (selectedFamily == "全部六艺" || row.familyLabel == selectedFamily) &&
                (selectedStep == "全部动作" || row.stepLabel == selectedStep) &&
                (selectedMonth == "全部月份" || row.monthLabel == selectedMonth)
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let(onExportToUri)
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        pendingImportUri = uri
    }

    pendingImportUri?.let { importUri ->
        ImportHistoryConfirmDialog(
            onDismiss = { pendingImportUri = null },
            onReplace = {
                pendingImportUri = null
                onImportFromUri(importUri, TrainingHistoryImportMode.Replace)
            },
            onMerge = {
                pendingImportUri = null
                onImportFromUri(importUri, TrainingHistoryImportMode.Merge)
            },
        )
    }

    if (showBackupDialog) {
        BackupActionDialog(
            latestExportLabel = preview.latestExportLabel,
            canShare = preview.latestExportUri != null,
            onDismiss = { showBackupDialog = false },
            onExport = {
                showBackupDialog = false
                exportLauncher.launch(defaultHistoryBackupFileName())
            },
            onImport = {
                showBackupDialog = false
                importLauncher.launch(arrayOf("application/json", "text/*"))
            },
            onShare = {
                preview.latestExportUri?.let { uriString ->
                    showBackupDialog = false
                    shareHistoryBackup(
                        context = context,
                        uri = Uri.parse(uriString),
                    )
                }
            },
        )
    }

    PrisonSurface {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                HistoryTopBar(
                    onBack = onBack,
                    onOpenBackup = { showBackupDialog = true },
                )
            }

            if (preview.rows.isNotEmpty()) {
                item {
                    HistoryFilterBar(
                        familyOptions = familyOptions,
                        selectedFamily = selectedFamily,
                        onSelectFamily = {
                            selectedFamily = it
                            selectedStep = "全部动作"
                        },
                        stepOptions = stepOptions,
                        selectedStep = selectedStep,
                        onSelectStep = { selectedStep = it },
                        monthOptions = monthOptions,
                        selectedMonth = selectedMonth,
                        onSelectMonth = { selectedMonth = it },
                    )
                }
            }

            preview.transferStatus?.let { status ->
                item {
                    MutedBody(text = status)
                }
            }

            if (preview.rows.isEmpty()) {
                item {
                    SteelPanel {
                        SectionKicker(text = "暂无数据")
                        Text(
                            text = "还没有历史记录",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        MutedBody(text = "完成一次训练并保存后，就会在这里看到可点击记录。")
                    }
                }
            } else if (filteredRows.isEmpty()) {
                item {
                    SteelPanel {
                        SectionKicker(text = "没有匹配结果")
                        Text(
                            text = "当前筛选下没有记录",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        MutedBody(text = "换一个六艺或月份试试。")
                    }
                }
            } else {
                filteredRows
                    .groupBy { it.monthLabel }
                    .forEach { (monthLabel, rows) ->
                        item(key = "month-$monthLabel") {
                            HistoryMonthDivider(label = monthLabel)
                        }
                        items(rows, key = { it.sessionId }) { row ->
                            HistoryListCard(
                                preview = row,
                                onClick = { onOpenDetail(row.sessionId) },
                            )
                        }
                    }
            }

            item {
                SteelSecondaryButton(
                    text = "回到首页",
                    onClick = onBackHome,
                )
            }
        }
    }
}

@Composable
fun TrainingHistoryDetailScreen(
    preview: HistoryDetailPreview?,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onReuse: () -> Unit,
    onDelete: () -> Unit,
    onUpdateRep: (Int, String) -> Unit,
    onSave: () -> Unit,
) {
    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "历史详情",
                actionLabel = "返回列表",
                onAction = onBack,
            )

            if (preview == null) {
                SteelPanel {
                    SectionKicker(text = "无可显示内容")
                    Text(
                        text = "没有选中记录",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    MutedBody(text = "返回历史列表后重新选择一条记录。")
                }
            } else {
                SteelPanel {
                    SectionKicker(text = preview.title)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MetricPlate(
                            label = "总次数",
                            value = preview.totalRepsLabel,
                            modifier = Modifier.weight(1f),
                            compact = true,
                        )
                        MetricPlate(
                            label = "组数",
                            value = preview.totalSetsLabel,
                            modifier = Modifier.weight(1f),
                            compact = true,
                        )
                    }
                    HistoryDetailMetaBand(
                        startedAtLabel = preview.startedAtLabel,
                        endedAtLabel = preview.endedAtLabel,
                        durationLabel = preview.durationLabel,
                        restPresetLabel = preview.restPresetLabel,
                    )
                }

                DetailActionRow(
                    canSave = preview.isSaveEnabled,
                    onSave = onSave,
                    onReuse = onReuse,
                    onDelete = onDelete,
                )

                SteelPanel(soft = true) {
                    SteelSectionHeader(
                        title = "分组详情",
                        subtitle = "${preview.setDetails.size} 组",
                    )
                    preview.setDetails.forEachIndexed { index, detail ->
                        DetailSetBlock(
                            detail = detail,
                            onValueChange = { onUpdateRep(index, it) },
                        )
                        if (index != preview.setDetails.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                        }
                    }
                }
            }

            SteelSecondaryButton(
                text = "回到首页",
                onClick = onBackHome,
            )
        }
    }
}

@Composable
private fun HistoryTopBar(
    onBack: () -> Unit,
    onOpenBackup: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "训练历史",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SteelCompactButton(
                text = "备份",
                onClick = onOpenBackup,
            )
            SteelCompactButton(
                text = "返回",
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun BackupActionDialog(
    latestExportLabel: String?,
    canShare: Boolean,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onShare: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "历史备份",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                latestExportLabel?.let { label ->
                    MutedBody(text = label)
                }
                SteelPrimaryButton(
                    text = "导出备份",
                    onClick = onExport,
                )
                SteelSecondaryButton(
                    text = "导入备份",
                    onClick = onImport,
                )
                if (canShare) {
                    SteelSecondaryButton(
                        text = "分享备份",
                        onClick = onShare,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        dismissButton = {},
    )
}

@Composable
private fun HistoryDetailMetaBand(
    startedAtLabel: String,
    endedAtLabel: String,
    durationLabel: String,
    restPresetLabel: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DetailMetaChip(
                label = "开始",
                value = startedAtLabel,
                modifier = Modifier.weight(1f),
            )
            DetailMetaChip(
                label = "结束",
                value = endedAtLabel,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DetailMetaChip(
                label = "用时",
                value = durationLabel,
                modifier = Modifier.weight(1f),
            )
            DetailMetaChip(
                label = "休息预设",
                value = restPresetLabel,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DetailMetaChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.34f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StatusStrip(lines: List<String>) {
    SteelPanel(soft = true) {
        lines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReadySettingRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun BooleanOptionRow(
    enabled: Boolean,
    enabledText: String,
    disabledText: String,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TogglePill(
            modifier = Modifier.weight(1f),
            selected = enabled,
            text = enabledText,
            onClick = { onToggle(true) },
        )
        TogglePill(
            modifier = Modifier.weight(1f),
            selected = !enabled,
            text = disabledText,
            onClick = { onToggle(false) },
        )
    }
}

@Composable
private fun VoiceModeSelector(
    selectedMode: VoiceGuideMode,
    onSelect: (VoiceGuideMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            VoiceGuideMode.Command to "起 / 落 / 停",
            VoiceGuideMode.Counting to "按秒报数",
            VoiceGuideMode.Breathing to "吸气 / 稳住 / 呼气",
        ).forEach { (mode, label) ->
            TogglePill(
                modifier = Modifier.fillMaxWidth(),
                selected = selectedMode == mode,
                text = label,
                onClick = { onSelect(mode) },
            )
        }
    }
}

@Composable
private fun VoicePersonSelector(
    voices: List<DeviceVoiceOption>,
    selectedVoiceId: String,
    onSelect: (String) -> Unit,
) {
    if (voices.isEmpty()) {
        MutedBody(text = "当前设备没有返回可选语音人物，将继续使用系统默认语音。")
        return
    }

    val selectedLabel = voices.firstOrNull { it.id == selectedVoiceId }?.label ?: "系统默认"
    var expanded by remember(selectedVoiceId, voices.size) {
        mutableStateOf(false)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            SelectorDropdownField(
                label = "语音人物",
                value = selectedLabel,
                onClick = { expanded = true },
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.94f),
            ) {
                voices.forEach { voice ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (selectedVoiceId == voice.id) "当前: ${voice.label}" else voice.label,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelect(voice.id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportHistoryConfirmDialog(
    onDismiss: () -> Unit,
    onReplace: () -> Unit,
    onMerge: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "导入训练历史",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text("请选择导入方式。替换会清空当前历史后再导入，合并会保留当前历史，并把备份中的记录追加进来。")
        },
        confirmButton = {
            TextButton(onClick = onReplace) {
                Text("替换导入")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onMerge) {
                    Text("合并导入")
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
    )
}

@Composable
private fun SelectorDropdownField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    actionLabel: String = "展开",
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(18.dp),
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.24f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SecondsSelector(
    options: List<Int>,
    selectedSeconds: Int,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { seconds ->
                    TogglePill(
                        modifier = Modifier.weight(1f),
                        selected = selectedSeconds == seconds,
                        text = "${seconds}秒",
                        onClick = { onSelect(seconds) },
                    )
                }
                repeat(3 - rowOptions.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TogglePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                },
                shape = RoundedCornerShape(18.dp),
            )
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CadenceCorePanel(
    phaseLabel: String,
    phaseSecondLabel: String,
    cueText: String,
    activeBeatIndex: Int,
) {
    SteelPanel {
        SectionKicker(text = "节奏")
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(246.dp)
                    .clip(CircleShape)
                    .border(
                        width = 8.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.primary,
                            ),
                        ),
                        shape = CircleShape,
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = phaseLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = phaseSecondLabel,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = cueText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        BreathBars(activeBeatIndex = activeBeatIndex)
    }
}

@Composable
private fun BreathBars(activeBeatIndex: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
    ) {
        listOf(10.dp, 18.dp, 28.dp, 18.dp, 10.dp).forEachIndexed { index, height ->
            val highlighted = index == activeBeatIndex
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(height)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (highlighted) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun MetricPlate(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                shape = RoundedCornerShape(20.dp),
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f))
            .padding(horizontal = 10.dp, vertical = 14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = value,
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SummarySetEditor(
    row: SummarySetRowPreview,
    editable: Boolean,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "第 ${row.setIndex} 组",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "结束 ${row.endedAtLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (editable) {
            OutlinedTextField(
                value = row.repValue,
                onValueChange = onValueChange,
                label = { Text("次数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = "次数 ${row.repValue}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        MutedBody(text = "时长 ${row.durationLabel}")
    }
}

@Composable
private fun StandardsIllustrationGallery(
    illustrations: List<StandardsIllustrationPreview>,
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { illustrations.size },
    )
    val coroutineScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            StandardsIllustrationItem(illustration = illustrations[page])
        }

        if (illustrations.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${illustrations.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    illustrations.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .width(if (index == pagerState.currentPage) 20.dp else 10.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (index == pagerState.currentPage) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                                    },
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterBar(
    familyOptions: List<String>,
    selectedFamily: String,
    onSelectFamily: (String) -> Unit,
    stepOptions: List<String>,
    selectedStep: String,
    onSelectStep: (String) -> Unit,
    monthOptions: List<String>,
    selectedMonth: String,
    onSelectMonth: (String) -> Unit,
) {
    val defaultFamily = familyOptions.firstOrNull().orEmpty()
    val defaultStep = stepOptions.firstOrNull().orEmpty()
    val defaultMonth = monthOptions.firstOrNull().orEmpty()
    val activeFilterCount = listOf(
        selectedFamily != defaultFamily,
        selectedStep != defaultStep,
        selectedMonth != defaultMonth,
    ).count { it }
    val summaryLine = buildList {
        if (selectedFamily != defaultFamily) add("六艺: $selectedFamily")
        if (selectedStep != defaultStep) add("动作: $selectedStep")
        if (selectedMonth != defaultMonth) add("月份: $selectedMonth")
    }.joinToString("  ·  ").ifBlank { "当前显示全部训练记录" }
    var expanded by remember { mutableStateOf(false) }

    SteelPanel(soft = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SectionKicker(text = "筛选条件")
                    Text(
                        text = if (activeFilterCount == 0) "全部记录" else "已筛选 $activeFilterCount 项",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = summaryLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起" else "展开筛选")
                }
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HistoryDropdownSelector(
                            modifier = Modifier.weight(1f),
                            label = "六艺",
                            options = familyOptions,
                            selected = selectedFamily,
                            onSelect = {
                                onSelectFamily(it)
                                onSelectStep(defaultStep)
                            },
                        )
                        HistoryDropdownSelector(
                            modifier = Modifier.weight(1f),
                            label = "月份",
                            options = monthOptions,
                            selected = selectedMonth,
                            onSelect = onSelectMonth,
                        )
                    }
                    HistoryDropdownSelector(
                        label = "动作",
                        options = stepOptions,
                        selected = selectedStep,
                        onSelect = onSelectStep,
                    )
                    if (activeFilterCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                onClick = {
                                    onSelectFamily(defaultFamily)
                                    onSelectStep(defaultStep)
                                    onSelectMonth(defaultMonth)
                                },
                            ) {
                                Text("清空筛选")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryDropdownSelector(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(selected, options.size) { mutableStateOf(false) }

    Box(modifier = modifier) {
        SelectorDropdownField(
            label = label,
            value = selected,
            actionLabel = "选择",
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.94f),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (selected == option) "当前: $option" else option,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryMonthDivider(
    label: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
        )
    }
}

@Composable
private fun StandardsIllustrationItem(
    illustration: StandardsIllustrationPreview,
) {
    val context = LocalContext.current
    val imageBitmap = remember(illustration.assetPath) {
        runCatching {
            context.assets.open(illustration.assetPath).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (imageBitmap == null) {
            StandardsIllustrationFrame()
        } else {
            Image(
                bitmap = imageBitmap,
                contentDescription = illustration.caption,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
            )
        }

        if (illustration.caption.isNotBlank()) {
            MutedBody(text = illustration.caption)
        }
    }
}

@Composable
private fun StandardsIllustrationFrame() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f),
                shape = RoundedCornerShape(20.dp),
            )
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "后续将在这里显示对应式的示意图",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
    }
}

@Composable
private fun DetailActionRow(
    canSave: Boolean,
    onSave: () -> Unit,
    onReuse: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompactActionPill(
            text = "保存修改",
            enabled = canSave,
            onClick = onSave,
            modifier = Modifier.weight(1f),
        )
        CompactActionPill(
            text = "载入训练",
            enabled = true,
            onClick = onReuse,
            modifier = Modifier.weight(1f),
        )
        CompactActionPill(
            text = "删除记录",
            enabled = true,
            onClick = onDelete,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CompactActionPill(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.12f else 0.05f),
                shape = RoundedCornerShape(999.dp),
            )
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.26f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.14f)
                },
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun HistoryListCard(
    preview: HistoryRowPreview,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PrisonPanelShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                shape = PrisonPanelShape,
            )
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = preview.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = preview.dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                    textAlign = TextAlign.End,
                )
            }

            HistorySetBand(
                setPreview = preview.setPreview,
            )

            Text(
                text = "${preview.setCountLabel} · 总${preview.totalRepsLabel}次",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun HistorySetBand(
    setPreview: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.42f))
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Text(
            text = setPreview,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DetailSetBlock(
    detail: HistorySetDetailPreview,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f))
                    .padding(horizontal = 10.dp, vertical = 9.dp),
            ) {
                Text(
                    text = "第${detail.setIndex}组",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            OutlinedTextField(
                value = detail.repValue,
                onValueChange = onValueChange,
                label = { Text("次数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DetailInlineMetaChip(
                text = "时长 ${detail.durationLabel}",
                modifier = Modifier.weight(1f),
            )
            DetailInlineMetaChip(
                text = detail.restAfterLabel,
                modifier = Modifier.weight(1f),
            )
            DetailInlineMetaChip(
                text = "结束 ${detail.endedAtLabel}",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DetailInlineMetaChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.28f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SpeechCueEffect(
    enabled: Boolean,
    cueKey: String?,
    cueText: String?,
    selectedVoiceId: String = "",
) {
    val context = LocalContext.current
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var engineReady by remember { mutableStateOf(false) }
    var lastSpokenKey by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engineReady = true
            } else {
                engineReady = false
            }
        }
        textToSpeech = engine
        engineReady = false

        onDispose {
            engine.stop()
            engine.shutdown()
            textToSpeech = null
            engineReady = false
        }
    }

    LaunchedEffect(textToSpeech, engineReady, selectedVoiceId) {
        val engine = textToSpeech ?: return@LaunchedEffect
        if (!engineReady) {
            return@LaunchedEffect
        }
        if (selectedVoiceId.isBlank()) {
            engine.language = Locale.SIMPLIFIED_CHINESE
        } else {
            engine.voices
                ?.firstOrNull { it.name == selectedVoiceId }
                ?.let { voice -> engine.voice = voice }
        }
    }

    LaunchedEffect(enabled) {
        if (!enabled) {
            lastSpokenKey = null
        }
    }

    LaunchedEffect(enabled, cueKey, cueText, textToSpeech, engineReady) {
        if (!enabled || cueKey.isNullOrBlank() || cueText.isNullOrBlank() || !engineReady) {
            return@LaunchedEffect
        }
        if (cueKey != lastSpokenKey) {
            val speakResult = textToSpeech?.speak(cueText, TextToSpeech.QUEUE_FLUSH, null, cueKey)
            if (speakResult == TextToSpeech.SUCCESS) {
                lastSpokenKey = cueKey
            }
        }
    }
}

fun buildTrainingEntryPreview(
    context: ExerciseContext,
    restPresetSeconds: Int,
    cadenceLabel: String,
    cadenceSeconds: Long,
): TrainingEntryPreview = TrainingEntryPreview(
    context = context,
    cadenceLabel = cadenceLabel,
    cadenceSeconds = cadenceSeconds,
    restPresetLabel = "${restPresetSeconds} 秒",
)

fun buildSettingsPreview(
    speechEnabled: Boolean,
    voiceGuideMode: VoiceGuideMode,
    availableVoices: List<DeviceVoiceOption>,
    selectedVoiceId: String,
    restPresetSeconds: Int,
    restPresetOptions: List<Int>,
    preparationSeconds: Int,
    preparationOptions: List<Int>,
    restCountdownVoiceEnabled: Boolean,
): TrainingSettingsPreview = TrainingSettingsPreview(
    speechEnabled = speechEnabled,
    voiceGuideMode = voiceGuideMode,
    voiceModeLabel = if (speechEnabled) voiceGuideMode.labelZh() else "语音关闭",
    availableVoices = availableVoices,
    selectedVoiceId = selectedVoiceId,
    selectedVoiceLabel = availableVoices.firstOrNull { it.id == selectedVoiceId }?.label ?: "系统默认",
    restPresetSeconds = restPresetSeconds,
    restPresetOptions = restPresetOptions,
    preparationSeconds = preparationSeconds,
    preparationOptions = preparationOptions,
    restCountdownVoiceEnabled = restCountdownVoiceEnabled,
)

fun buildPreparingPreview(
    context: ExerciseContext,
    state: TrainingSessionState.PreparingSet,
    nowUtc: Instant,
): PreparingPreview {
    val remainingMs = Duration.between(nowUtc, state.targetSetStartedAtUtc).toMillis().coerceAtLeast(0L)
    val remainingSeconds = ((remainingMs + 999L) / 1000L).coerceAtLeast(1L)
    return PreparingPreview(
        context = context,
        currentSetIndex = state.completedSets.size + 1,
        completedSetCount = state.completedSets.size,
        totalRepCount = state.completedSets.sumOf { it.completedRepCount },
        countdownLabel = remainingSeconds.toString(),
        hintLabel = "就位后按 2-1-2 开始",
        speechCueKey = "prep-$remainingSeconds",
        speechCueText = remainingSeconds.toString(),
    )
}

fun buildRunningPreview(
    context: ExerciseContext,
    state: TrainingSessionState.SetRunning,
    nowUtc: Instant,
    voiceGuideMode: VoiceGuideMode,
    speechEnabled: Boolean,
): RunningPreview {
    val progress = trackLiveCadence(
        cadenceProfile = state.cadenceProfile,
        elapsedMs = Duration.between(state.setStartedAtUtc, nowUtc).toMillis(),
    )
    val phaseSecond = ((progress.phaseElapsedMs / 1000L).toInt() + 1).coerceAtLeast(1)
    val speechCue = buildTrainingSpeechCue(
        phase = progress.phase,
        phaseSecond = phaseSecond,
        repIndex = progress.completedRepCount,
        voiceGuideMode = voiceGuideMode,
        speechEnabled = speechEnabled,
    )
    return RunningPreview(
        context = context,
        currentPhaseLabel = progress.phase.displayLabelZh(),
        phaseSecondLabel = String.format(Locale.getDefault(), "%.1f", progress.phaseElapsedMs / 1000f),
        phaseCueText = progress.phase.displayCueText(
            voiceGuideMode = voiceGuideMode,
            phaseSecond = phaseSecond,
        ),
        activeBeatIndex = progress.phase.activeBeatIndex(),
        speechCueKey = speechCue?.first,
        speechCueText = speechCue?.second,
        currentRepCount = progress.completedRepCount,
        currentSetIndex = state.completedSets.size + 1,
        completedSetCount = state.completedSets.size,
        totalRepCount = state.completedSets.sumOf { it.completedRepCount } + progress.completedRepCount,
        sessionElapsedLabel = formatStopwatch(Duration.between(state.sessionStartedAtUtc, nowUtc).toMillis()),
    )
}

fun buildRestPreview(
    context: ExerciseContext,
    state: TrainingSessionState,
    nowUtc: Instant,
    restCountdownVoiceEnabled: Boolean,
    speechEnabled: Boolean,
): RestPreview {
    val restState = when (state) {
        is TrainingSessionState.RestRunning -> state
        is TrainingSessionState.RestOvertime -> state
        else -> error("Rest preview requires a rest state.")
    }
    val restStartedAtUtc = when (restState) {
        is TrainingSessionState.RestRunning -> restState.restStartedAtUtc
        is TrainingSessionState.RestOvertime -> restState.restStartedAtUtc
        else -> error("Unsupported rest state.")
    }
    val restPreset = when (restState) {
        is TrainingSessionState.RestRunning -> restState.restPreset
        is TrainingSessionState.RestOvertime -> restState.restPreset
        else -> error("Unsupported rest state.")
    }
    val completedSets = when (restState) {
        is TrainingSessionState.RestRunning -> restState.completedSets
        is TrainingSessionState.RestOvertime -> restState.completedSets
        else -> error("Unsupported rest state.")
    }
    val snapshot = snapshotRestState(
        restStartedAtUtc = restStartedAtUtc,
        restPreset = restPreset,
        nowUtc = nowUtc,
    )
    val speechCue = if (speechEnabled && restCountdownVoiceEnabled) restSpeechCue(snapshot) else null

    return RestPreview(
        context = context,
        completedSetCount = completedSets.size,
        totalRepCount = completedSets.sumOf { it.completedRepCount },
        restHeadline = if (snapshot.isOvertime) "休息超时" else "建议休息中",
        restTimeLabel = if (snapshot.isOvertime) "+${formatStopwatch(snapshot.overtimeMs)}" else formatStopwatch(snapshot.remainingMs),
        restHint = if (snapshot.isOvertime) "已超出建议休息时长，继续显示正计时。" else "倒计时结束前可随时开始下一组。",
        presetLabel = "${restPreset.defaultRestSeconds} 秒",
        speechCueKey = speechCue?.first,
        speechCueText = speechCue?.second,
    )
}

fun buildSummaryPreview(
    context: ExerciseContext,
    state: TrainingSessionState,
    nowUtc: Instant,
    repDrafts: List<String>,
    isSaved: Boolean,
): SummaryPreview {
    val sessionStartedAtUtc: Instant
    val sessionEndedAtUtc: Instant
    val completedSets: List<WorkoutSetResult>
    val isEditable = state is TrainingSessionState.Completed

    when (state) {
        is TrainingSessionState.Completed -> {
            sessionStartedAtUtc = state.sessionStartedAtUtc
            sessionEndedAtUtc = state.sessionEndedAtUtc
            completedSets = state.completedSets
        }
        is TrainingSessionState.PreparingSet -> {
            sessionStartedAtUtc = state.sessionStartedAtUtc ?: nowUtc
            sessionEndedAtUtc = nowUtc
            completedSets = state.completedSets
        }
        is TrainingSessionState.SetRunning -> {
            sessionStartedAtUtc = state.sessionStartedAtUtc
            sessionEndedAtUtc = nowUtc
            completedSets = state.completedSets
        }
        is TrainingSessionState.RestRunning -> {
            sessionStartedAtUtc = state.sessionStartedAtUtc
            sessionEndedAtUtc = nowUtc
            completedSets = state.completedSets
        }
        is TrainingSessionState.RestOvertime -> {
            sessionStartedAtUtc = state.sessionStartedAtUtc
            sessionEndedAtUtc = nowUtc
            completedSets = state.completedSets
        }
        TrainingSessionState.Idle -> {
            sessionStartedAtUtc = nowUtc
            sessionEndedAtUtc = nowUtc
            completedSets = emptyList()
        }
    }

    val setRows = completedSets.mapIndexed { index, result ->
        SummarySetRowPreview(
            setIndex = index + 1,
            repValue = repDrafts.getOrNull(index) ?: result.completedRepCount.toString(),
            durationLabel = formatStopwatch(result.elapsedMs),
            endedAtLabel = UiTimeFormatter.format(result.endedAtUtc),
        )
    }

    return SummaryPreview(
        context = context,
        sessionStartLabel = UiTimeFormatter.format(sessionStartedAtUtc),
        sessionEndLabel = UiTimeFormatter.format(sessionEndedAtUtc),
        totalSets = setRows.size,
        totalReps = setRows.sumOf { it.repValue.toIntOrNull() ?: 0 },
        setRows = setRows,
        isEditable = isEditable,
        isSaved = isSaved,
    )
}

fun buildStandardsPreview(context: ExerciseContext): StandardsPreview {
    val article = ExerciseStandardsCatalog.find(
        familyId = context.family.id,
        stepLevel = context.step.level,
    )

    return StandardsPreview(
        context = context,
        sourceLabel = article?.sourceLabel ?: sourceChapterTitle(context.family.id),
        sections = article?.sections?.map { it.title to it.body } ?: listOf(
            "标准说明" to "这里用于放入这一式来自原书的核心动作标准，只保留训练前真正需要快速复核的内容。",
            "动作要点 / 常见错误" to "这里用于整理动作路径、呼吸、节奏配合，以及可能需要的辅助信息。",
        ),
        illustrations = article?.illustrations?.map {
            StandardsIllustrationPreview(
                assetPath = it.assetPath,
                caption = it.caption,
            )
        } ?: emptyList(),
    )
}

fun buildHistoryPreview(
    sessions: List<TrainingSessionWithSets>,
    transferStatus: String? = null,
    latestExportLabel: String? = null,
    latestExportUri: String? = null,
): HistoryPreview = HistoryPreview(
    rows = sessions.map { sessionWithSets ->
        val family = ExerciseCatalog.families.firstOrNull { it.id == sessionWithSets.session.familyId }
        val step = family?.steps?.firstOrNull { it.level == sessionWithSets.session.stepLevel }
        HistoryRowPreview(
            sessionId = sessionWithSets.session.sessionId,
            familyLabel = family?.titleZh ?: sessionWithSets.session.familyId,
            stepLabel = step?.label ?: "第${sessionWithSets.session.stepLevel}式",
            title = buildString {
                append(family?.titleZh ?: sessionWithSets.session.familyId)
                append("·")
                append(step?.label ?: "第${sessionWithSets.session.stepLevel}式")
            },
            totalRepsLabel = sessionWithSets.session.totalReps.toString(),
            setCountLabel = "${sessionWithSets.session.totalSets} 组",
            setPreview = sessionWithSets.sets
                .sortedBy { it.setIndex }
                .joinToString(separator = " + ") { set -> set.completedRepCount.toString() }
                .ifBlank { "暂无分组明细" },
            monthLabel = UiMonthFormatter.format(
                Instant.ofEpochMilli(sessionWithSets.session.sessionEndedAtUtcEpochMs),
            ),
            dateLabel = UiTimeFormatter.format(
                Instant.ofEpochMilli(sessionWithSets.session.sessionEndedAtUtcEpochMs),
            ),
        )
    },
    transferStatus = transferStatus,
    latestExportLabel = latestExportLabel,
    latestExportUri = latestExportUri,
)

fun buildHistoryDetailPreview(
    sessionWithSets: TrainingSessionWithSets?,
    repDrafts: List<String>,
    hasPendingEdits: Boolean,
): HistoryDetailPreview? {
    if (sessionWithSets == null) {
        return null
    }

    val family = ExerciseCatalog.families.firstOrNull { it.id == sessionWithSets.session.familyId }
    val step = family?.steps?.firstOrNull { it.level == sessionWithSets.session.stepLevel }
    val startedAt = Instant.ofEpochMilli(sessionWithSets.session.sessionStartedAtUtcEpochMs)
    val endedAt = Instant.ofEpochMilli(sessionWithSets.session.sessionEndedAtUtcEpochMs)
    val sessionDurationMs = (sessionWithSets.session.sessionEndedAtUtcEpochMs - sessionWithSets.session.sessionStartedAtUtcEpochMs).coerceAtLeast(0L)
    val sortedSets = sessionWithSets.sets.sortedBy { it.setIndex }
    val totalReps = sortedSets.mapIndexed { index, set ->
        repDrafts.getOrNull(index)?.toIntOrNull() ?: set.completedRepCount
    }.sum()

    return HistoryDetailPreview(
        sessionId = sessionWithSets.session.sessionId,
        title = buildString {
            append(family?.titleZh ?: sessionWithSets.session.familyId)
            append("·")
            append(step?.label ?: "第${sessionWithSets.session.stepLevel}式")
        },
        totalRepsLabel = "${totalReps}次",
        totalSetsLabel = "${sessionWithSets.session.totalSets}组",
        startedAtLabel = UiTimeFormatter.format(startedAt),
        endedAtLabel = UiTimeFormatter.format(endedAt),
        durationLabel = formatStopwatch(sessionDurationMs),
        restPresetLabel = "${sessionWithSets.session.restPresetSeconds}秒",
        setDetails = sortedSets.mapIndexed { index, set ->
            val restAfterLabel = if (index == sortedSets.lastIndex) {
                val finalGapMs = (sessionWithSets.session.sessionEndedAtUtcEpochMs - set.endedAtUtcEpochMs)
                    .coerceAtLeast(0L)
                if (finalGapMs > 0L) {
                    "休息 ${formatStopwatch(finalGapMs)}"
                } else {
                    "本组后结束"
                }
            } else {
                val nextSet = sortedSets[index + 1]
                val restGapMs = (nextSet.startedAtUtcEpochMs - set.endedAtUtcEpochMs).coerceAtLeast(0L)
                "休息 ${formatStopwatch(restGapMs)}"
            }
            HistorySetDetailPreview(
                setId = set.setId,
                setIndex = set.setIndex,
                repValue = repDrafts.getOrNull(index) ?: set.completedRepCount.toString(),
                durationLabel = formatStopwatch(set.elapsedMs),
                restAfterLabel = restAfterLabel,
                endedAtLabel = UiClockFormatter.format(Instant.ofEpochMilli(set.endedAtUtcEpochMs)),
            )
        },
        isSaveEnabled = hasPendingEdits,
    )
}

private fun sourceChapterTitle(familyId: String): String = when (familyId) {
    "pushup" -> "第五章 · text00013.html"
    "squat" -> "第六章 · text00014.html"
    "pullup" -> "第七章 · text00015.html"
    "leg_raise" -> "第八章 · text00016.html"
    "bridge" -> "第九章 · text00017.html"
    "handstand_pushup" -> "第十章 · text00018.html"
    else -> "六艺章节"
}

private fun formatStopwatch(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

private fun defaultHistoryBackupFileName(): String {
    val stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        .withZone(ZoneId.systemDefault())
        .format(Instant.now())
    return "liuyi-history-$stamp.json"
}

private fun shareHistoryBackup(
    context: android.content.Context,
    uri: Uri,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri("liuyi-history-backup", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(sendIntent, "分享训练历史备份"))
}

private fun restSpeechCue(snapshot: com.liuyi.trainer.model.RestSnapshot): Pair<String, String>? {
    if (snapshot.isOvertime) {
        return if (snapshot.overtimeMs < 1_000L) {
            "rest-overtime" to "超时"
        } else {
            null
        }
    }

    val remainingSeconds = ((snapshot.remainingMs + 999L) / 1000L).toInt()
    return if (remainingSeconds in 1..3) {
        "rest-$remainingSeconds" to remainingSeconds.toString()
    } else {
        null
    }
}

private fun buildTrainingSpeechCue(
    phase: CadencePhase,
    phaseSecond: Int,
    repIndex: Int,
    voiceGuideMode: VoiceGuideMode,
    speechEnabled: Boolean,
): Pair<String, String>? {
    if (!speechEnabled) {
        return null
    }

    return when (voiceGuideMode) {
        VoiceGuideMode.Command -> {
            "cmd-${repIndex}-${phase.name}" to phase.voiceCue()
        }

        VoiceGuideMode.Counting -> {
            "count-${repIndex}-${phase.name}-$phaseSecond" to phase.countingCue(phaseSecond)
        }

        VoiceGuideMode.Breathing -> {
            "breath-${repIndex}-${phase.name}" to phase.breathCue()
        }
    }
}

private fun VoiceGuideMode.labelZh(): String = when (this) {
    VoiceGuideMode.Command -> "起落停"
    VoiceGuideMode.Counting -> "按秒报数"
    VoiceGuideMode.Breathing -> "呼吸提示"
}

private fun CadencePhase.displayLabelZh(): String = when (this) {
    CadencePhase.Lowering -> "下落"
    CadencePhase.BottomHold -> "停顿"
    CadencePhase.Rising -> "起身"
}

private fun CadencePhase.displayCueText(
    voiceGuideMode: VoiceGuideMode,
    phaseSecond: Int,
): String = when (voiceGuideMode) {
    VoiceGuideMode.Command -> voiceCue()
    VoiceGuideMode.Counting -> countingCue(phaseSecond)
    VoiceGuideMode.Breathing -> breathCue()
}

private fun CadencePhase.activeBeatIndex(): Int = when (this) {
    CadencePhase.Lowering -> 1
    CadencePhase.BottomHold -> 2
    CadencePhase.Rising -> 3
}

private fun CadencePhase.voiceCue(): String = when (this) {
    CadencePhase.Lowering -> "落"
    CadencePhase.BottomHold -> "停"
    CadencePhase.Rising -> "起"
}

private fun CadencePhase.countingCue(phaseSecond: Int): String = when (this) {
    CadencePhase.Lowering -> if (phaseSecond <= 1) "一" else "二"
    CadencePhase.BottomHold -> "停"
    CadencePhase.Rising -> if (phaseSecond <= 1) "一" else "二"
}

private fun CadencePhase.breathCue(): String = when (this) {
    CadencePhase.Lowering -> "吸气"
    CadencePhase.BottomHold -> "稳住"
    CadencePhase.Rising -> "呼气"
}

fun defaultExerciseContext(): ExerciseContext {
    val family = ExerciseCatalog.families.first()
    val step = family.steps[3]
    return ExerciseContext(
        family = family,
        step = step,
    )
}

fun defaultRestPreset(): RestPreset = RestPreset(
    defaultRestSeconds = 90,
    presetOptionsSeconds = listOf(45, 60, 90, 120, 180),
)
