package com.liuyi.trainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liuyi.trainer.model.MovementFamily
import com.liuyi.trainer.model.MovementStep

@Composable
fun HomeScreen(
    families: List<MovementFamily>,
    selectedFamilyId: String,
    selectedStepLevel: Int,
    restPresetSeconds: Int,
    restPresetOptions: List<Int>,
    previewCadenceLabel: String,
    previewCadenceSeconds: Long,
    onSelectFamily: (String) -> Unit,
    onSelectStep: (Int) -> Unit,
    onSelectRestPreset: (Int) -> Unit,
    onStartTraining: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStandards: () -> Unit,
) {
    val selectedFamily = families.firstOrNull { it.id == selectedFamilyId } ?: families.first()
    val selectedStep = selectedFamily.steps.firstOrNull { it.level == selectedStepLevel }
        ?: selectedFamily.steps.first()

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CompactHeaderCard(
                    family = selectedFamily,
                    step = selectedStep,
                    restPresetSeconds = restPresetSeconds,
                    cadenceLabel = previewCadenceLabel,
                    cadenceSeconds = previewCadenceSeconds,
                    onStartTraining = onStartTraining,
                    onOpenStandards = onOpenStandards,
                    onOpenSummary = onOpenSummary,
                    onOpenHistory = onOpenHistory,
                )
            }

            item {
                SectionTitle("六艺")
            }

            families.forEach { family ->
                item {
                    ChoiceCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = family.titleZh,
                        subtitle = family.titleEn,
                        meta = if (family.id == selectedFamily.id) "当前" else "点击选择",
                        selected = family.id == selectedFamily.id,
                        onClick = { onSelectFamily(family.id) },
                    )
                }
            }

            item {
                SectionTitle("十式")
            }

            item {
                StepSelector(
                    steps = selectedFamily.steps,
                    selectedStepLevel = selectedStep.level,
                    onSelectStep = onSelectStep,
                )
            }

            item {
                SectionTitle("组间休息")
            }

            item {
                RestPresetSelector(
                    restPresetOptions = restPresetOptions,
                    selectedRestPresetSeconds = restPresetSeconds,
                    onSelectRestPreset = onSelectRestPreset,
                )
            }
        }
    }
}

@Composable
private fun CompactHeaderCard(
    family: MovementFamily,
    step: MovementStep,
    restPresetSeconds: Int,
    cadenceLabel: String,
    cadenceSeconds: Long,
    onStartTraining: () -> Unit,
    onOpenStandards: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenHistory: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "六艺十式",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${family.titleZh} · ${step.label}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "节奏 $cadenceLabel · 每次循环 ${cadenceSeconds} 秒 · 休息 ${restPresetSeconds} 秒",
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartTraining,
            ) {
                Text("进入训练")
            }
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenStandards,
            ) {
                Text("动作标准")
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenSummary,
            ) {
                Text("当前总结")
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenHistory,
            ) {
                Text("训练历史")
            }
        }
    }
}

@Composable
private fun ChoiceCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    meta: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
    ) {
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
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = meta,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun RestPresetSelector(
    restPresetOptions: List<Int>,
    selectedRestPresetSeconds: Int,
    onSelectRestPreset: (Int) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            restPresetOptions.chunked(3).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowOptions.forEach { seconds ->
                        val selected = seconds == selectedRestPresetSeconds
                        if (selected) {
                            Button(
                                onClick = { onSelectRestPreset(seconds) },
                            ) {
                                Text("${seconds}s")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectRestPreset(seconds) },
                            ) {
                                Text("${seconds}s")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepSelector(
    steps: List<MovementStep>,
    selectedStepLevel: Int,
    onSelectStep: (Int) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            steps.chunked(5).forEach { rowSteps ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowSteps.forEach { step ->
                        val selected = step.level == selectedStepLevel
                        if (selected) {
                            Button(
                                onClick = { onSelectStep(step.level) },
                            ) {
                                Text(step.level.toString())
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectStep(step.level) },
                            ) {
                                Text(step.level.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}
