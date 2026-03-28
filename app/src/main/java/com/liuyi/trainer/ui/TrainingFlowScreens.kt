package com.liuyi.trainer.ui

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.liuyi.trainer.data.TrainingSessionWithSets
import com.liuyi.trainer.model.CadencePhase
import com.liuyi.trainer.model.CadenceProfile
import com.liuyi.trainer.model.ContentStatus
import com.liuyi.trainer.model.ExerciseCatalog
import com.liuyi.trainer.model.MovementFamily
import com.liuyi.trainer.model.MovementStep
import com.liuyi.trainer.model.RestPreset
import com.liuyi.trainer.model.TrainingSessionState
import com.liuyi.trainer.model.WorkoutSetResult
import com.liuyi.trainer.model.snapshotRestState
import com.liuyi.trainer.model.trackLiveCadence
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val UiTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("M月d日 HH:mm").withZone(ZoneId.systemDefault())

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

data class RunningPreview(
    val context: ExerciseContext,
    val currentPhaseLabel: String,
    val phaseElapsedLabel: String,
    val phaseCueText: String,
    val currentRepCount: Int,
    val currentSetIndex: Int,
    val completedSetCount: Int,
    val totalRepCount: Int,
    val sessionStartLabel: String,
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
    val contentStatusLabel: String,
    val statusHint: String,
    val sections: List<Pair<String, String>>,
)

data class HistoryRowPreview(
    val sessionId: Long,
    val title: String,
    val dateLabel: String,
    val totalsLabel: String,
    val setPreview: String,
)

data class HistoryPreview(
    val rows: List<HistoryRowPreview>,
)

data class HistoryDetailPreview(
    val title: String,
    val timeRangeLabel: String,
    val totalsLabel: String,
    val metaLines: List<String>,
    val setDetails: List<String>,
)

@Composable
fun TrainingReadyScreen(
    preview: TrainingEntryPreview,
    speechEnabled: Boolean,
    onToggleSpeech: (Boolean) -> Unit,
    onBack: () -> Unit,
    onStartSet: () -> Unit,
    onOpenStandards: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeaderRow(
                title = "训练准备",
                action = "返回首页",
                onAction = onBack,
            )

            FocusMetricCard(
                eyebrow = preview.context.family.titleZh,
                title = preview.context.step.label,
                metric = "节奏 ${preview.cadenceLabel}",
                supporting = "训练不会自动开始，进入后先停在准备态，由你手动开始本组。",
            )

            CompactInfoCard(
                title = "本次设置",
                lines = listOf(
                    "每次完整循环 ${preview.cadenceSeconds} 秒",
                    "组间休息 ${preview.restPresetLabel}",
                    if (speechEnabled) "语音提示已开启" else "语音提示已关闭",
                ),
            )

            SettingToggleCard(
                title = "语音提示",
                value = if (speechEnabled) "开启" else "关闭",
                hint = "训练中按阶段播报“落 / 停 / 起”。",
                onToggle = { onToggleSpeech(!speechEnabled) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onStartSet,
                ) {
                    Text("开始本组")
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenStandards,
                ) {
                    Text("动作标准")
                }
            }
        }
    }
}

@Composable
fun TrainingRunningScreen(
    preview: RunningPreview,
    speechEnabled: Boolean,
    onToggleSpeech: (Boolean) -> Unit,
    onBack: () -> Unit,
    onFinishSet: () -> Unit,
    onCompleteTraining: () -> Unit,
) {
    TrainingVoiceCueEffect(
        enabled = speechEnabled,
        cueText = preview.phaseCueText,
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeaderRow(
                title = "训练执行",
                action = "返回首页",
                onAction = onBack,
            )

            CompactStatusStrip(
                lines = listOf(
                    "${preview.context.family.titleZh} · ${preview.context.step.label}",
                    "第 ${preview.currentSetIndex} 组 · 已完成 ${preview.completedSetCount} 组",
                    "本次训练累计 ${preview.totalRepCount} 次",
                ),
            )

            FocusMetricCard(
                eyebrow = "当前阶段",
                title = preview.currentPhaseLabel,
                metric = preview.phaseElapsedLabel,
                supporting = "一级信息只保留动作阶段和阶段秒数。",
            )

            FocusMetricCard(
                eyebrow = "当前组次数",
                title = preview.currentRepCount.toString(),
                metric = "完整循环",
                supporting = "只计完整 2-1-2 循环。",
            )

            SettingToggleCard(
                title = "语音提示",
                value = if (speechEnabled) "开启" else "关闭",
                hint = "当前阶段切换时播报 ${preview.phaseCueText}。",
                onToggle = { onToggleSpeech(!speechEnabled) },
            )

            CompactInfoCard(
                title = "辅助信息",
                lines = listOf(
                    "开始时间 ${preview.sessionStartLabel}",
                    "累计时长 ${preview.sessionElapsedLabel}",
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onFinishSet,
                ) {
                    Text("结束本组")
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCompleteTraining,
                ) {
                    Text("结束训练")
                }
            }
        }
    }
}

@Composable
fun TrainingRestScreen(
    preview: RestPreview,
    onBack: () -> Unit,
    onStartNextSet: () -> Unit,
    onCompleteTraining: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeaderRow(
                title = "组间休息",
                action = "返回首页",
                onAction = onBack,
            )

            CompactStatusStrip(
                lines = listOf(
                    "${preview.context.family.titleZh} · ${preview.context.step.label}",
                    "已完成 ${preview.completedSetCount} 组 · 共 ${preview.totalRepCount} 次",
                    "休息预设 ${preview.presetLabel}",
                ),
            )

            FocusMetricCard(
                eyebrow = "休息状态",
                title = preview.restHeadline,
                metric = preview.restTimeLabel,
                supporting = preview.restHint,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onStartNextSet,
                ) {
                    Text("开始下一组")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCompleteTraining,
                ) {
                    Text("结束本次训练")
                }
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
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeaderRow(
                title = "训练总结",
                action = "返回上一页",
                onAction = onBack,
            )

            FocusMetricCard(
                eyebrow = "${preview.context.family.titleZh} · ${preview.context.step.label}",
                title = "${preview.totalSets} 组",
                metric = "累计 ${preview.totalReps} 次",
                supporting = "开始 ${preview.sessionStartLabel} · 结束 ${preview.sessionEndLabel}",
            )

            CompactInfoCard(
                title = "保存规则",
                lines = listOf(
                    if (preview.isEditable) "保存前可以逐组修正次数。" else "当前不是可编辑总结状态。",
                    if (preview.isSaved) "当前记录已保存到历史。" else "当前记录尚未保存到历史。",
                ),
            )

            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "分组明细",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    if (preview.setRows.isEmpty()) {
                        Text(
                            text = "当前还没有组记录。",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        preview.setRows.forEachIndexed { index, row ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "第 ${row.setIndex} 组",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (preview.isEditable && !preview.isSaved) {
                                    OutlinedTextField(
                                        value = row.repValue,
                                        onValueChange = { onUpdateRep(index, it) },
                                        label = { Text("次数") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                    )
                                } else {
                                    Text(
                                        text = "次数 ${row.repValue}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Text(
                                    text = "时长 ${row.durationLabel} · 结束 ${row.endedAtLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (index != preview.setRows.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onBackHome,
                ) {
                    Text("回到首页")
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenHistory,
                ) {
                    Text(if (preview.isSaved) "查看历史" else "历史列表")
                }
            }

            if (preview.isEditable && !preview.isSaved) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSave,
                ) {
                    Text("保存到历史")
                }
            }
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
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeaderRow(
                title = "动作标准",
                action = "返回上一页",
                onAction = onBack,
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${preview.context.family.titleZh} · ${preview.context.step.label}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    StatusBadge(label = preview.contentStatusLabel)
                    Text(
                        text = preview.statusHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            preview.sections.forEach { (title, body) ->
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onBackHome,
                ) {
                    Text("回到首页")
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenTraining,
                ) {
                    Text("进入训练")
                }
            }
        }
    }
}

@Composable
fun TrainingHistoryScreen(
    preview: HistoryPreview,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onOpenDetail: (Long) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeaderRow(
                title = "训练历史",
                action = "返回上一页",
                onAction = onBack,
            )

            if (preview.rows.isEmpty()) {
                FocusMetricCard(
                    eyebrow = "暂无数据",
                    title = "还没有历史记录",
                    metric = "先完成一次训练",
                    supporting = "保存后的训练会以可点击列表的形式出现在这里。",
                )
            } else {
                preview.rows.forEach { row ->
                    Card(onClick = { onOpenDetail(row.sessionId) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = row.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = row.dateLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = row.totalsLabel,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = row.setPreview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBackHome,
            ) {
                Text("回到首页")
            }
        }
    }
}

@Composable
fun TrainingHistoryDetailScreen(
    preview: HistoryDetailPreview?,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeaderRow(
                title = "历史详情",
                action = "返回列表",
                onAction = onBack,
            )

            if (preview == null) {
                FocusMetricCard(
                    eyebrow = "无可显示内容",
                    title = "没有选中记录",
                    metric = "返回列表重新选择",
                    supporting = "当前没有找到对应的历史训练记录。",
                )
            } else {
                FocusMetricCard(
                    eyebrow = preview.title,
                    title = preview.totalsLabel,
                    metric = preview.timeRangeLabel,
                    supporting = "每组详情放在下方单独区域。",
                )

                CompactInfoCard(
                    title = "本次训练信息",
                    lines = preview.metaLines,
                )

                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "分组详情",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        preview.setDetails.forEachIndexed { index, detail ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = detail,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (index != preview.setDetails.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBackHome,
            ) {
                Text("回到首页")
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

fun buildRunningPreview(
    context: ExerciseContext,
    state: TrainingSessionState.SetRunning,
    nowUtc: Instant,
): RunningPreview {
    val progress = trackLiveCadence(
        cadenceProfile = state.cadenceProfile,
        elapsedMs = Duration.between(state.setStartedAtUtc, nowUtc).toMillis(),
    )
    val phaseTotalMs = phaseDurationMs(progress.phase, state.cadenceProfile)
    val sessionElapsed = Duration.between(state.sessionStartedAtUtc, nowUtc)

    return RunningPreview(
        context = context,
        currentPhaseLabel = progress.phase.labelZh(),
        phaseElapsedLabel = formatPhaseElapsed(progress.phaseElapsedMs, phaseTotalMs),
        phaseCueText = progress.phase.voiceCue(),
        currentRepCount = progress.completedRepCount,
        currentSetIndex = state.completedSets.size + 1,
        completedSetCount = state.completedSets.size,
        totalRepCount = state.completedSets.sumOf { it.completedRepCount } + progress.completedRepCount,
        sessionStartLabel = UiTimeFormatter.format(state.sessionStartedAtUtc),
        sessionElapsedLabel = formatDuration(sessionElapsed),
    )
}

fun buildRestPreview(
    context: ExerciseContext,
    state: TrainingSessionState,
    nowUtc: Instant,
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

    return RestPreview(
        context = context,
        completedSetCount = completedSets.size,
        totalRepCount = completedSets.sumOf { it.completedRepCount },
        restHeadline = if (snapshot.isOvertime) "休息超时" else "建议休息中",
        restTimeLabel = if (snapshot.isOvertime) {
            "+${formatStopwatch(snapshot.overtimeMs)}"
        } else {
            formatStopwatch(snapshot.remainingMs)
        },
        restHint = if (snapshot.isOvertime) {
            "已超出建议休息时长，继续显示正计时。"
        } else {
            "倒计时结束前可随时开始下一组。"
        },
        presetLabel = "${restPreset.defaultRestSeconds} 秒",
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
    val statusLabel = when (context.step.contentStatus) {
        ContentStatus.Placeholder -> "占位内容"
        ContentStatus.Draft -> "草稿内容"
        ContentStatus.Ready -> "已就绪内容"
    }
    val hint = when (context.step.contentStatus) {
        ContentStatus.Placeholder -> "当前先放结构和来源位置，后续根据你提供的原书内容做整理接入。"
        ContentStatus.Draft -> "当前是重述草稿，仍需校对。"
        ContentStatus.Ready -> "当前内容已就绪，可直接用于训练前阅读。"
    }

    return StandardsPreview(
        context = context,
        contentStatusLabel = statusLabel,
        statusHint = hint,
        sections = listOf(
            "原书来源" to sourceChapterTitle(context.family.id),
            "当前接入策略" to "优先接动作标准、训练要点、常见错误和示意图位置；不在当前阶段盲目整本照搬。",
            "训练要点" to "训练前快速浏览，训练中不占据主视觉。内容结构已经预留，下一步可按你给出的原书内容继续填充。",
        ),
    )
}

fun buildHistoryPreview(
    sessions: List<TrainingSessionWithSets>,
): HistoryPreview = HistoryPreview(
    rows = sessions.map { sessionWithSets ->
        val family = ExerciseCatalog.families.firstOrNull {
            it.id == sessionWithSets.session.familyId
        }
        val step = family?.steps?.firstOrNull {
            it.level == sessionWithSets.session.stepLevel
        }
        val setPreview = sessionWithSets.sets
            .sortedBy { it.setIndex }
            .joinToString(separator = " / ") { set ->
                "第${set.setIndex}组 ${set.completedRepCount}次"
            }
            .ifBlank { "暂无分组明细" }

        HistoryRowPreview(
            sessionId = sessionWithSets.session.sessionId,
            title = buildString {
                append(family?.titleZh ?: sessionWithSets.session.familyId)
                append(" · ")
                append(step?.label ?: "第${sessionWithSets.session.stepLevel}式")
            },
            dateLabel = UiTimeFormatter.format(Instant.ofEpochMilli(sessionWithSets.session.sessionEndedAtUtcEpochMs)),
            totalsLabel = "共 ${sessionWithSets.session.totalSets} 组 · ${sessionWithSets.session.totalReps} 次",
            setPreview = setPreview,
        )
    },
)

fun buildHistoryDetailPreview(
    sessionWithSets: TrainingSessionWithSets?,
): HistoryDetailPreview? {
    if (sessionWithSets == null) {
        return null
    }

    val family = ExerciseCatalog.families.firstOrNull {
        it.id == sessionWithSets.session.familyId
    }
    val step = family?.steps?.firstOrNull {
        it.level == sessionWithSets.session.stepLevel
    }
    val startedAt = Instant.ofEpochMilli(sessionWithSets.session.sessionStartedAtUtcEpochMs)
    val endedAt = Instant.ofEpochMilli(sessionWithSets.session.sessionEndedAtUtcEpochMs)
    val sessionDurationMs = (sessionWithSets.session.sessionEndedAtUtcEpochMs - sessionWithSets.session.sessionStartedAtUtcEpochMs)
        .coerceAtLeast(0L)

    return HistoryDetailPreview(
        title = buildString {
            append(family?.titleZh ?: sessionWithSets.session.familyId)
            append(" · ")
            append(step?.label ?: "第${sessionWithSets.session.stepLevel}式")
        },
        timeRangeLabel = "开始 ${UiTimeFormatter.format(startedAt)} · 结束 ${UiTimeFormatter.format(endedAt)}",
        totalsLabel = "共 ${sessionWithSets.session.totalSets} 组，累计 ${sessionWithSets.session.totalReps} 次",
        metaLines = listOf(
            "训练时长 ${formatStopwatch(sessionDurationMs)}",
            "组间休息 ${sessionWithSets.session.restPresetSeconds} 秒",
        ),
        setDetails = sessionWithSets.sets
            .sortedBy { it.setIndex }
            .map { set ->
                "第 ${set.setIndex} 组 · ${set.completedRepCount} 次 · 时长 ${formatStopwatch(set.elapsedMs)} · 结束 ${UiTimeFormatter.format(Instant.ofEpochMilli(set.endedAtUtcEpochMs))}"
            },
    )
}

private fun sourceChapterTitle(familyId: String): String = when (familyId) {
    "pushup" -> "第五章 俯卧撑"
    "squat" -> "第六章 深蹲"
    "pullup" -> "第七章 引体向上"
    "leg_raise" -> "第八章 举腿"
    "bridge" -> "第九章 桥"
    "handstand_pushup" -> "第十章 倒立撑"
    else -> "六艺章节"
}

private fun phaseDurationMs(
    phase: CadencePhase,
    cadence: CadenceProfile,
): Long = when (phase) {
    CadencePhase.Lowering -> cadence.eccentricSeconds * 1000L
    CadencePhase.BottomHold -> cadence.bottomPauseSeconds * 1000L
    CadencePhase.Rising -> cadence.concentricSeconds * 1000L
}

private fun formatPhaseElapsed(
    phaseElapsedMs: Long,
    phaseTotalMs: Long,
): String {
    val elapsed = phaseElapsedMs / 1000f
    val total = phaseTotalMs / 1000f
    return String.format(Locale.getDefault(), "%.1f / %.1f 秒", elapsed, total)
}

private fun formatStopwatch(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatDuration(duration: Duration): String = formatStopwatch(duration.toMillis())

private fun CadencePhase.labelZh(): String = when (this) {
    CadencePhase.Lowering -> "下落"
    CadencePhase.BottomHold -> "停顿"
    CadencePhase.Rising -> "起身"
}

private fun CadencePhase.voiceCue(): String = when (this) {
    CadencePhase.Lowering -> "落"
    CadencePhase.BottomHold -> "停"
    CadencePhase.Rising -> "起"
}

@Composable
private fun HeaderRow(
    title: String,
    action: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        OutlinedButton(onClick = onAction) {
            Text(action)
        }
    }
}

@Composable
private fun CompactStatusStrip(lines: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun FocusMetricCard(
    eyebrow: String,
    title: String,
    metric: String,
    supporting: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = metric,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompactInfoCard(
    title: String,
    lines: List<String>,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SettingToggleCard(
    title: String,
    value: String,
    hint: String,
    onToggle: () -> Unit,
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$value · $hint",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onToggle) {
                Text(if (value == "开启") "关闭" else "开启")
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.large,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TrainingVoiceCueEffect(
    enabled: Boolean,
    cueText: String,
) {
    val context = LocalContext.current
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.SIMPLIFIED_CHINESE
            }
        }
        textToSpeech = engine

        onDispose {
            engine.stop()
            engine.shutdown()
            textToSpeech = null
        }
    }

    LaunchedEffect(enabled, cueText, textToSpeech) {
        if (enabled) {
            textToSpeech?.speak(cueText, TextToSpeech.QUEUE_FLUSH, null, cueText)
        }
    }
}

fun defaultExerciseContext(): ExerciseContext {
    val family = ExerciseCatalog.families.first()
    val step = family.steps[2]
    return ExerciseContext(
        family = family,
        step = step,
    )
}

fun defaultRestPreset(): RestPreset = RestPreset(defaultRestSeconds = 90)
