package com.liuyi.trainer.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

private val UiTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("M月d日 HH:mm").withZone(ZoneId.systemDefault())

data class ExerciseContext(
    val family: MovementFamily,
    val step: MovementStep,
)

data class RunningPreview(
    val context: ExerciseContext,
    val currentPhaseLabel: String,
    val phaseElapsedLabel: String,
    val phaseHint: String,
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

data class SummaryPreview(
    val context: ExerciseContext,
    val sessionStartLabel: String,
    val sessionEndLabel: String,
    val totalSets: Int,
    val totalReps: Int,
    val setRows: List<String>,
)

data class StandardsPreview(
    val context: ExerciseContext,
    val contentStatusLabel: String,
    val statusHint: String,
    val sections: List<Pair<String, String>>,
)

data class HistoryRowPreview(
    val title: String,
    val timeRangeLabel: String,
    val totalsLabel: String,
    val metrics: List<String>,
    val setDetails: List<String>,
)

data class HistoryPreview(
    val rows: List<HistoryRowPreview>,
)

@Composable
fun TrainingRunningScreen(
    preview: RunningPreview,
    onBack: () -> Unit,
    onFinishSet: () -> Unit,
    onCompleteTraining: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderRow(
                title = "训练执行",
                action = "返回首页",
                onAction = onBack,
            )

            SecondaryInfoStrip(
                lines = listOf(
                    "当前 ${preview.context.family.titleZh} · ${preview.context.step.label}",
                    "第 ${preview.currentSetIndex} 组 / 已完成 ${preview.completedSetCount} 组",
                    "本次训练已累计 ${preview.totalRepCount} 次",
                ),
            )

            PrimaryFocusCard(
                eyebrow = "一级信息",
                title = preview.currentPhaseLabel,
                metric = preview.phaseElapsedLabel,
                supporting = preview.phaseHint,
            )

            PrimaryFocusCard(
                eyebrow = "当前组完成次数",
                title = "${preview.currentRepCount}",
                metric = "完整循环",
                supporting = "只按完整 2-1-2 循环计次，未完成循环不计入。",
            )

            TertiaryInfoCard(
                title = "辅助信息",
                lines = listOf(
                    "本次训练开始：${preview.sessionStartLabel}",
                    "已训练时长：${preview.sessionElapsedLabel}",
                    "视觉风格入口预留给高对比、极简与强提示模式。",
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderRow(
                title = "组间休息",
                action = "返回首页",
                onAction = onBack,
            )

            SecondaryInfoStrip(
                lines = listOf(
                    "${preview.context.family.titleZh} · ${preview.context.step.label}",
                    "已完成 ${preview.completedSetCount} 组，共 ${preview.totalRepCount} 次",
                    "默认休息预设：${preview.presetLabel}",
                ),
            )

            PrimaryFocusCard(
                eyebrow = "休息主状态",
                title = preview.restHeadline,
                metric = preview.restTimeLabel,
                supporting = preview.restHint,
            )

            TertiaryInfoCard(
                title = "休息规则",
                lines = listOf(
                    "每组结束后自动进入休息；倒计时结束后自动切换为正计时。",
                    "当前先支持固定预设，后续再补自定义值和偏好保存。",
                ),
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
    onOpenStandards: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderRow(
                title = "训练总结",
                action = "返回上一页",
                onAction = onBack,
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "${preview.context.family.titleZh} · ${preview.context.step.label}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "开始 ${preview.sessionStartLabel} · 结束 ${preview.sessionEndLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "共 ${preview.totalSets} 组，累计 ${preview.totalReps} 次",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "分组明细",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (preview.setRows.isEmpty()) {
                        Text(
                            text = "当前还没有已完成的组记录。",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        preview.setRows.forEachIndexed { index, row ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "第 ${index + 1} 组",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = row,
                                    style = MaterialTheme.typography.bodyMedium,
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
                    onClick = onOpenStandards,
                ) {
                    Text("看动作标准")
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        .padding(20.dp),
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
                            .padding(20.dp),
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
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderRow(
                title = "训练历史",
                action = "返回上一页",
                onAction = onBack,
            )

            SecondaryInfoStrip(
                lines = listOf(
                    "最近训练次数：${preview.rows.size}",
                    "每条记录都显示本次训练时间、总组数、总次数和各组明细。",
                ),
            )

            if (preview.rows.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "还没有历史记录",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "完成一次训练后，这里会显示动作、组数、次数和时间。",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                preview.rows.forEach { row ->
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = row.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = row.timeRangeLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = row.totalsLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            row.metrics.forEach { metric ->
                                Text(
                                    text = metric,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            HorizontalDivider()

                            Text(
                                text = "分组明细",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )

                            if (row.setDetails.isEmpty()) {
                                Text(
                                    text = "当前没有分组明细。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                row.setDetails.forEach { setDetail ->
                                    Text(
                                        text = setDetail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
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
        phaseHint = "当前阶段秒数需要压过其他信息，维持单手扫视即可识别。",
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
            "已超出建议休息时间，继续正计时提醒用户当前多休了多久。"
        } else {
            "倒计时结束前随时可以手动开始下一组。"
        },
        presetLabel = "${restPreset.defaultRestSeconds} 秒",
    )
}

fun buildSummaryPreview(
    context: ExerciseContext,
    state: TrainingSessionState,
    nowUtc: Instant,
): SummaryPreview {
    val sessionStartedAtUtc: Instant
    val sessionEndedAtUtc: Instant
    val completedSets: List<WorkoutSetResult>

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

    return SummaryPreview(
        context = context,
        sessionStartLabel = UiTimeFormatter.format(sessionStartedAtUtc),
        sessionEndLabel = UiTimeFormatter.format(sessionEndedAtUtc),
        totalSets = completedSets.size,
        totalReps = completedSets.sumOf { it.completedRepCount },
        setRows = completedSets.map { result ->
            "完成 ${result.completedRepCount} 次，时长 ${formatStopwatch(result.elapsedMs)}，结束于 ${UiTimeFormatter.format(result.endedAtUtc)}"
        },
    )
}

fun buildStandardsPreview(context: ExerciseContext): StandardsPreview {
    val statusLabel = when (context.step.contentStatus) {
        ContentStatus.Placeholder -> "占位内容"
        ContentStatus.Draft -> "草稿内容"
        ContentStatus.Ready -> "已就绪内容"
    }
    val hint = when (context.step.contentStatus) {
        ContentStatus.Placeholder -> "当前不内置受版权约束的原书文字与图片，只保留正式页面结构和字段位置。"
        ContentStatus.Draft -> "草稿内容允许先行验证结构与可读性，但仍需来源校对。"
        ContentStatus.Ready -> "内容已准备好，可直接作为动作标准页面的正式展示。"
    }

    return StandardsPreview(
        context = context,
        contentStatusLabel = statusLabel,
        statusHint = hint,
        sections = listOf(
            "标准说明" to "这里保留动作标准正文位置，后续可接入原创重述内容或已获授权的正式内容。",
            "训练要点" to "用简短分点强调节奏、动作范围、呼吸与常见代偿，避免训练时阅读负担过重。",
            "常见错误" to "预留错误动作与纠正建议区块，后续可挂接示意图或局部特写资源。",
            "素材状态" to "示意图、音频提示、引用来源将在内容系统落地后接入；当前只保留页面骨架与状态标识。",
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
        val title = buildString {
            append(family?.titleZh ?: sessionWithSets.session.familyId)
            append(" · ")
            append(step?.label ?: "第 ${sessionWithSets.session.stepLevel} 式")
        }
        val startedAt = Instant.ofEpochMilli(sessionWithSets.session.sessionStartedAtUtcEpochMs)
        val endedAt = Instant.ofEpochMilli(sessionWithSets.session.sessionEndedAtUtcEpochMs)
        val timeRangeLabel = "开始 ${UiTimeFormatter.format(startedAt)} · 结束 ${UiTimeFormatter.format(endedAt)}"
        val totalsLabel = "共 ${sessionWithSets.session.totalSets} 组，累计 ${sessionWithSets.session.totalReps} 次"
        val sessionDurationMs = (sessionWithSets.session.sessionEndedAtUtcEpochMs - sessionWithSets.session.sessionStartedAtUtcEpochMs)
            .coerceAtLeast(0L)
        val averageReps = if (sessionWithSets.session.totalSets == 0) {
            "0.0"
        } else {
            String.format(
                "%.1f",
                sessionWithSets.session.totalReps.toDouble() / sessionWithSets.session.totalSets.toDouble(),
            )
        }
        val metrics = listOf(
            "训练时长 ${formatStopwatch(sessionDurationMs)}",
            "组间休息预设 ${sessionWithSets.session.restPresetSeconds} 秒",
            "平均每组 ${averageReps} 次",
        )
        val setDetails = sessionWithSets.sets
            .sortedBy { it.setIndex }
            .map { set ->
                "第 ${set.setIndex} 组 · ${set.completedRepCount} 次 · 时长 ${formatStopwatch(set.elapsedMs)} · 结束 ${UiTimeFormatter.format(Instant.ofEpochMilli(set.endedAtUtcEpochMs))}"
            }

        HistoryRowPreview(
            title = title,
            timeRangeLabel = timeRangeLabel,
            totalsLabel = totalsLabel,
            metrics = metrics,
            setDetails = setDetails,
        )
    },
)

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
    return String.format("%.1f / %.1f 秒", elapsed, total)
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
private fun SecondaryInfoStrip(lines: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
private fun PrimaryFocusCard(
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
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TertiaryInfoCard(
    title: String,
    lines: List<String>,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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

fun defaultExerciseContext(): ExerciseContext {
    val family = ExerciseCatalog.families.first()
    val step = family.steps[2]
    return ExerciseContext(
        family = family,
        step = step,
    )
}

fun defaultRestPreset(): RestPreset = RestPreset(defaultRestSeconds = 90)
