package com.liuyi.trainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
                HomeHeaderCard(
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

            item {
                FamilyGrid(
                    families = families,
                    selectedFamilyId = selectedFamily.id,
                    onSelectFamily = onSelectFamily,
                )
            }

            item {
                SectionTitle("十式")
            }

            item {
                CompactStepSelector(
                    steps = selectedFamily.steps,
                    selectedStep = selectedStep,
                    onSelectStep = onSelectStep,
                )
            }

            item {
                SectionTitle("组间休息")
            }

            item {
                CompactRestPresetSelector(
                    restPresetOptions = restPresetOptions,
                    selectedRestPresetSeconds = restPresetSeconds,
                    onSelectRestPreset = onSelectRestPreset,
                )
            }
        }
    }
}

@Composable
private fun HomeHeaderCard(
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
                text = "${family.titleZh} · 第${step.level}式",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = step.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "节奏 $cadenceLabel · ${cadenceSeconds} 秒/次 · 休息 ${restPresetSeconds} 秒",
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartTraining,
            ) {
                Text("进入训练")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenStandards,
                ) {
                    Text("动作标准")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenHistory,
                ) {
                    Text("训练历史")
                }
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenSummary,
            ) {
                Text("当前总结")
            }
        }
    }
}

@Composable
private fun FamilyGrid(
    families: List<MovementFamily>,
    selectedFamilyId: String,
    onSelectFamily: (String) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            families.chunked(3).forEach { rowFamilies ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowFamilies.forEach { family ->
                        FamilyChoiceCell(
                            modifier = Modifier.weight(1f),
                            family = family,
                            selected = family.id == selectedFamilyId,
                            onClick = { onSelectFamily(family.id) },
                        )
                    }
                    repeat(3 - rowFamilies.size) {
                        Row(modifier = Modifier.weight(1f)) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyChoiceCell(
    modifier: Modifier = Modifier,
    family: MovementFamily,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier.heightIn(min = 74.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = family.titleZh,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = family.titleEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompactStepSelector(
    steps: List<MovementStep>,
    selectedStep: MovementStep,
    onSelectStep: (Int) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Card(
                    modifier = Modifier.width(68.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = selectedStep.level.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally),
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "第${selectedStep.level}式",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = selectedStep.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                steps.forEach { step ->
                    val selected = step.level == selectedStep.level
                    if (selected) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectStep(step.level) },
                            contentPadding = PaddingValues(vertical = 10.dp),
                        ) {
                            Text(step.level.toString())
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectStep(step.level) },
                            contentPadding = PaddingValues(vertical = 10.dp),
                        ) {
                            Text(step.level.toString())
                        }
                    }
                }
            }

            val previousStep = steps.getOrNull((selectedStep.level - 2).coerceAtLeast(0))
            val nextStep = steps.getOrNull(selectedStep.level)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StepNameHint(
                    modifier = Modifier.weight(1f),
                    title = "上一式",
                    step = previousStep ?: selectedStep,
                    onClick = { onSelectStep(previousStep?.level ?: selectedStep.level) },
                )
                StepNameHint(
                    modifier = Modifier.weight(1f),
                    title = "当前式",
                    step = selectedStep,
                    onClick = { onSelectStep(selectedStep.level) },
                )
                StepNameHint(
                    modifier = Modifier.weight(1f),
                    title = "下一式",
                    step = nextStep ?: selectedStep,
                    onClick = { onSelectStep(nextStep?.level ?: selectedStep.level) },
                )
            }
        }
    }
}

@Composable
private fun StepNameHint(
    modifier: Modifier = Modifier,
    title: String,
    step: MovementStep,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = "第${step.level}式",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = step.label,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun CompactRestPresetSelector(
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
                            Text("${seconds}s")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectRestPreset(seconds) },
                            contentPadding = PaddingValues(vertical = 10.dp),
                        ) {
                            Text("${seconds}s")
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
