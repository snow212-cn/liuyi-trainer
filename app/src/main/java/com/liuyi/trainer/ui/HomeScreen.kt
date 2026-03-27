package com.liuyi.trainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                HeroCard(
                    title = "六艺训练台",
                    description = "当前 Android 实现以训练执行流为核心：首页负责选艺、选式、休息时长设置与进入训练，训练页再按一级/二级/三级信息层级展开。",
                )
            }

            item {
                SelectedExerciseCard(
                    family = selectedFamily,
                    step = selectedStep,
                    restPresetSeconds = restPresetSeconds,
                    cadenceLabel = previewCadenceLabel,
                    cadenceSeconds = previewCadenceSeconds,
                    onStartTraining = onStartTraining,
                    onOpenSummary = onOpenSummary,
                    onOpenHistory = onOpenHistory,
                    onOpenStandards = onOpenStandards,
                )
            }

            item {
                SectionTitle(title = "选择式")
            }

            item {
                StepSelector(
                    steps = selectedFamily.steps,
                    selectedStepLevel = selectedStep.level,
                    onSelectStep = onSelectStep,
                )
            }

            item {
                SectionTitle(title = "组间休息")
            }

            item {
                RestPresetSelector(
                    restPresetOptions = restPresetOptions,
                    selectedRestPresetSeconds = restPresetSeconds,
                    onSelectRestPreset = onSelectRestPreset,
                )
            }

            item {
                SectionTitle(title = "六艺总览")
            }

            items(
                items = families,
                key = { it.id },
            ) { family ->
                FamilyCard(
                    family = family,
                    isSelected = family.id == selectedFamily.id,
                    onSelect = { onSelectFamily(family.id) },
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    title: String,
    description: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SelectedExerciseCard(
    family: MovementFamily,
    step: MovementStep,
    restPresetSeconds: Int,
    cadenceLabel: String,
    cadenceSeconds: Long,
    onStartTraining: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStandards: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "当前练习入口",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = family.titleZh,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${family.titleEn} · ${step.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.large,
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "${family.steps.size} 式",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Text(
                text = family.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "预设节奏：$cadenceLabel，每完整循环 ${cadenceSeconds} 秒；当前默认组间休息 ${restPresetSeconds} 秒。",
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onStartTraining,
                ) {
                    Text("进入训练")
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenStandards,
                ) {
                    Text("动作标准")
                }
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenSummary,
            ) {
                Text("查看当前训练总结")
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenHistory,
            ) {
                Text("查看训练历史")
            }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                restPresetOptions.forEach { seconds ->
                    val selected = seconds == selectedRestPresetSeconds
                    if (selected) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectRestPreset(seconds) },
                            contentPadding = PaddingValues(vertical = 10.dp),
                        ) {
                            Text(text = "${seconds}s")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectRestPreset(seconds) },
                            contentPadding = PaddingValues(vertical = 10.dp),
                        ) {
                            Text(text = "${seconds}s")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStep(step.level) },
                                contentPadding = PaddingValues(vertical = 10.dp),
                            ) {
                                Text(text = step.label)
                            }
                        } else {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStep(step.level) },
                                contentPadding = PaddingValues(vertical = 10.dp),
                            ) {
                                Text(text = step.label)
                            }
                        }
                    }
                    repeat(5 - rowSteps.size) {
                        Spacer(modifier = Modifier.weight(1f))
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
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun FamilyCard(
    family: MovementFamily,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = family.titleZh,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = family.titleEn,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = if (isSelected) "当前选择" else "可切换",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = family.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "训练节奏源：${family.previewCadence.source}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!isSelected) {
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSelect,
                ) {
                    Text("切换到这一艺")
                }
            }
        }
    }
}
