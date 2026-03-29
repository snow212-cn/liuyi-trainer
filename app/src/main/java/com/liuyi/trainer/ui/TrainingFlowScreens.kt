package com.liuyi.trainer.ui

import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.liuyi.trainer.data.TrainingSessionWithSets
import com.liuyi.trainer.model.CadencePhase
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
    val contentStatusLabel: String,
    val statusHint: String,
    val sections: List<Pair<String, String>>,
)

data class HistoryRowPreview(
    val sessionId: Long,
    val title: String,
    val totalRepsLabel: String,
    val setCountLabel: String,
    val setPreview: String,
    val dateLabel: String,
)

data class HistoryPreview(
    val rows: List<HistoryRowPreview>,
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
    val timeRangeLabel: String,
    val totalsLabel: String,
    val metaLines: List<String>,
    val setDetails: List<HistorySetDetailPreview>,
    val isSaveEnabled: Boolean,
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
    PrisonSurface {
        PrisonScrollColumn {
            ScreenTopBar(
                title = "训练准备",
                actionLabel = "返回首页",
                onAction = onBack,
            )

            SteelPanel {
                SectionKicker(text = "READY CHECK")
                Text(
                    text = "${preview.context.family.titleZh} · ${preview.context.step.label}",
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
                    title = "本次设置",
                    subtitle = "手动开始",
                )
                ReadySettingRow(label = "完整循环", value = "${preview.cadenceSeconds} 秒")
                ReadySettingRow(label = "提示词", value = "落 / 停 / 起")
                ReadySettingRow(label = "语音提示", value = if (speechEnabled) "开启" else "关闭")
                SteelSecondaryButton(
                    text = if (speechEnabled) "关闭语音提示" else "开启语音提示",
                    onClick = { onToggleSpeech(!speechEnabled) },
                )
                MutedBody(text = "进入训练页后不会自动开始，必须由你手动点“开始本组”。")
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
    onBack: () -> Unit,
) {
    SpeechCueEffect(
        enabled = speechEnabled,
        cueKey = preview.speechCueKey,
        cueText = preview.speechCueText,
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
                    "${preview.context.family.titleZh} · ${preview.context.step.label}",
                    "第 ${preview.currentSetIndex} 组 · 已完成 ${preview.completedSetCount} 组",
                ),
            )

            SteelPanel {
                SectionKicker(text = "SET PREP")
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
                            SectionKicker(text = "就位")
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
    speechEnabled: Boolean,
    onToggleSpeech: (Boolean) -> Unit,
    onBack: () -> Unit,
    onFinishSet: () -> Unit,
    onCompleteTraining: () -> Unit,
) {
    SpeechCueEffect(
        enabled = speechEnabled,
        cueKey = preview.phaseCueText,
        cueText = preview.phaseCueText,
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
                    "${preview.context.family.titleZh} · ${preview.context.step.label}",
                    "第 ${preview.currentSetIndex} 组 · 已完成 ${preview.completedSetCount} 组",
                ),
            )

            CadenceCorePanel(
                phaseLabel = preview.currentPhaseLabel,
                phaseSecondLabel = preview.phaseSecondLabel,
                cueText = preview.phaseCueText,
                speechEnabled = speechEnabled,
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
                    text = if (speechEnabled) "语音已开" else "语音已关",
                    onClick = { onToggleSpeech(!speechEnabled) },
                    modifier = Modifier.weight(1f),
                )
            }
            SteelSecondaryButton(
                text = "结束训练",
                onClick = onCompleteTraining,
            )
        }
    }
}

@Composable
fun TrainingRestScreen(
    preview: RestPreview,
    speechEnabled: Boolean,
    onBack: () -> Unit,
    onStartNextSet: () -> Unit,
    onCompleteTraining: () -> Unit,
) {
    SpeechCueEffect(
        enabled = speechEnabled,
        cueKey = preview.speechCueKey,
        cueText = preview.speechCueText,
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
                    "${preview.context.family.titleZh} · ${preview.context.step.label}",
                    "已完成 ${preview.completedSetCount} 组 · 累计 ${preview.totalRepCount} 次",
                    "休息预设 ${preview.presetLabel}",
                ),
            )

            SteelPanel {
                SectionKicker(text = "REST WINDOW")
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
                SectionKicker(text = "${preview.context.family.titleZh} · ${preview.context.step.label}")
                Text(
                    text = "共 ${preview.totalSets} 组 · ${preview.totalReps} 次",
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
                SectionKicker(text = "原书来源")
                Text(
                    text = "${preview.context.family.titleZh} · ${preview.context.step.label}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
                MutedBody(text = preview.sourceLabel)
                StatusBadge(label = preview.contentStatusLabel)
                MutedBody(text = preview.statusHint)
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

            SteelPanel(soft = true) {
                Text(
                    text = "示意图位",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                StandardsIllustrationFrame()
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
    onOpenDetail: (Long) -> Unit,
) {
    PrisonSurface {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ScreenTopBar(
                    title = "训练历史",
                    actionLabel = "返回上一页",
                    onAction = onBack,
                )
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
            } else {
                items(preview.rows, key = { it.sessionId }) { row ->
                    HistoryListCard(
                        preview = row,
                        onClick = { onOpenDetail(row.sessionId) },
                    )
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
                    Text(
                        text = preview.totalsLabel,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    MutedBody(text = preview.timeRangeLabel)
                }

                DetailActionRow(
                    canSave = preview.isSaveEnabled,
                    onSave = onSave,
                    onReuse = onReuse,
                    onDelete = onDelete,
                )

                SteelPanel(soft = true) {
                    SteelSectionHeader(
                        title = "本次训练信息",
                        subtitle = "记录",
                    )
                    preview.metaLines.forEach { line ->
                        MutedBody(text = line)
                    }
                }

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
private fun CadenceCorePanel(
    phaseLabel: String,
    phaseSecondLabel: String,
    cueText: String,
    speechEnabled: Boolean,
) {
    SteelPanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionKicker(text = "CADENCE CORE")
            MutedBody(text = if (speechEnabled) "语音 开启" else "语音 关闭")
        }
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
        BreathBars(activeCue = cueText)
    }
}

@Composable
private fun BreathBars(activeCue: String) {
    val cues = listOf("落", "停", "起")
    val activeIndex = cues.indexOf(activeCue).coerceAtLeast(0)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
    ) {
        listOf(10.dp, 18.dp, 28.dp, 18.dp, 10.dp).forEachIndexed { index, height ->
            val highlighted = index == activeIndex + 1
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
            text = "未来接入 EPUB 摘录图或用户提供的示意图",
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
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = preview.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = preview.totalRepsLabel,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                )
            }
            HistorySetBand(
                setCountLabel = preview.setCountLabel,
                setPreview = preview.setPreview,
            )
            Text(
                text = preview.dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistorySetBand(
    setCountLabel: String,
    setPreview: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.36f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = setCountLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = setPreview,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            )
        }
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "第 ${detail.setIndex} 组",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        OutlinedTextField(
            value = detail.repValue,
            onValueChange = onValueChange,
            label = { Text("次数") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        MutedBody(text = "时长 ${detail.durationLabel}")
        MutedBody(text = detail.restAfterLabel)
        MutedBody(text = "结束 ${detail.endedAtLabel}")
    }
}

@Composable
private fun StatusBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun SpeechCueEffect(
    enabled: Boolean,
    cueKey: String?,
    cueText: String?,
) {
    val context = LocalContext.current
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var engineReady by remember { mutableStateOf(false) }
    var lastSpokenKey by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.SIMPLIFIED_CHINESE
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
): RunningPreview {
    val progress = trackLiveCadence(
        cadenceProfile = state.cadenceProfile,
        elapsedMs = Duration.between(state.setStartedAtUtc, nowUtc).toMillis(),
    )
    return RunningPreview(
        context = context,
        currentPhaseLabel = progress.phase.displayLabelZh(),
        phaseSecondLabel = String.format(Locale.getDefault(), "%.1f", progress.phaseElapsedMs / 1000f),
        phaseCueText = progress.phase.voiceCue(),
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
    val speechCue = restSpeechCue(snapshot)

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
        sourceLabel = sourceChapterTitle(context.family.id),
        contentStatusLabel = statusLabel,
        statusHint = hint,
        sections = listOf(
            "标准说明" to "这里将放该式来自原书的核心动作标准，不做整章阅读，只保留训练前真正需要快速复核的内容。",
            "动作要点 / 常见错误" to "这里承接关键动作路径、呼吸、节奏配合，以及是否需要借助墙面、篮球等辅助信息。",
        ),
    )
}

fun buildHistoryPreview(
    sessions: List<TrainingSessionWithSets>,
): HistoryPreview = HistoryPreview(
    rows = sessions.map { sessionWithSets ->
        val family = ExerciseCatalog.families.firstOrNull { it.id == sessionWithSets.session.familyId }
        val step = family?.steps?.firstOrNull { it.level == sessionWithSets.session.stepLevel }
        HistoryRowPreview(
            sessionId = sessionWithSets.session.sessionId,
            title = buildString {
                append(family?.titleZh ?: sessionWithSets.session.familyId)
                append(" · ")
                append(step?.label ?: "第${sessionWithSets.session.stepLevel}式")
            },
            totalRepsLabel = sessionWithSets.session.totalReps.toString(),
            setCountLabel = "${sessionWithSets.session.totalSets} 组",
            setPreview = sessionWithSets.sets
                .sortedBy { it.setIndex }
                .joinToString(separator = " + ") { set -> set.completedRepCount.toString() }
                .ifBlank { "暂无分组明细" },
            dateLabel = UiTimeFormatter.format(Instant.ofEpochMilli(sessionWithSets.session.sessionEndedAtUtcEpochMs)),
        )
    },
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
            append(" · ")
            append(step?.label ?: "第${sessionWithSets.session.stepLevel}式")
        },
        timeRangeLabel = "开始 ${UiTimeFormatter.format(startedAt)} · 结束 ${UiTimeFormatter.format(endedAt)}",
        totalsLabel = "共 ${sessionWithSets.session.totalSets} 组 · ${totalReps} 次",
        metaLines = listOf(
            "训练时长 ${formatStopwatch(sessionDurationMs)}",
            "休息预设 ${sessionWithSets.session.restPresetSeconds} 秒",
            "下方可直接修正每组次数",
        ),
        setDetails = sortedSets.mapIndexed { index, set ->
            val restAfterLabel = if (index == sortedSets.lastIndex) {
                val finalGapMs = (sessionWithSets.session.sessionEndedAtUtcEpochMs - set.endedAtUtcEpochMs)
                    .coerceAtLeast(0L)
                if (finalGapMs > 0L) {
                    "本组后休息 ${formatStopwatch(finalGapMs)}"
                } else {
                    "本组后结束训练"
                }
            } else {
                val nextSet = sortedSets[index + 1]
                val restGapMs = (nextSet.startedAtUtcEpochMs - set.endedAtUtcEpochMs).coerceAtLeast(0L)
                "本组后休息 ${formatStopwatch(restGapMs)}"
            }
            HistorySetDetailPreview(
                setId = set.setId,
                setIndex = set.setIndex,
                repValue = repDrafts.getOrNull(index) ?: set.completedRepCount.toString(),
                durationLabel = formatStopwatch(set.elapsedMs),
                restAfterLabel = restAfterLabel,
                endedAtLabel = UiTimeFormatter.format(Instant.ofEpochMilli(set.endedAtUtcEpochMs)),
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

private fun CadencePhase.displayLabelZh(): String = when (this) {
    CadencePhase.Lowering -> "下落"
    CadencePhase.BottomHold -> "停顿"
    CadencePhase.Rising -> "起身"
}

private fun CadencePhase.voiceCue(): String = when (this) {
    CadencePhase.Lowering -> "落"
    CadencePhase.BottomHold -> "停"
    CadencePhase.Rising -> "起"
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
