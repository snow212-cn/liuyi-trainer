package com.liuyi.trainer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val previousStep = selectedFamily.steps.getOrNull(selectedStep.level - 2)
    val nextStep = selectedFamily.steps.getOrNull(selectedStep.level)

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                HomeHeroCard(
                    family = selectedFamily,
                    step = selectedStep,
                    cadenceLabel = previewCadenceLabel,
                    cadenceSeconds = previewCadenceSeconds,
                    restPresetSeconds = restPresetSeconds,
                    onStartTraining = onStartTraining,
                )
            }

            item {
                FamilyCompactGrid(
                    families = families,
                    selectedFamilyId = selectedFamily.id,
                    onSelectFamily = onSelectFamily,
                )
            }

            item {
                StepDeckCard(
                    currentStep = selectedStep,
                    previousStep = previousStep,
                    nextStep = nextStep,
                    onSelectStep = onSelectStep,
                )
            }

            item {
                RestPresetCard(
                    restPresetOptions = restPresetOptions,
                    selectedRestPresetSeconds = restPresetSeconds,
                    onSelectRestPreset = onSelectRestPreset,
                )
            }

            item {
                QuickEntryRow(
                    onOpenStandards = onOpenStandards,
                    onOpenHistory = onOpenHistory,
                    onOpenSummary = onOpenSummary,
                )
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    family: MovementFamily,
    step: MovementStep,
    cadenceLabel: String,
    cadenceSeconds: Long,
    restPresetSeconds: Int,
    onStartTraining: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "囚徒健身",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = step.level.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = family.titleZh,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = step.label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${cadenceLabel} · ${cadenceSeconds} 秒/次 · 休息 ${restPresetSeconds} 秒",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartTraining,
            ) {
                Text("进入训练")
            }
        }
    }
}

@Composable
private fun FamilyCompactGrid(
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
            Text(
                text = "六艺",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            families.chunked(3).forEach { rowFamilies ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowFamilies.forEach { family ->
                        val selected = family.id == selectedFamilyId
                        val colors = if (selected) {
                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        } else {
                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 62.dp),
                            colors = colors,
                            border = if (selected) {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            },
                            onClick = { onSelectFamily(family.id) },
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = family.titleZh,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    repeat(3 - rowFamilies.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StepDeckCard(
    currentStep: MovementStep,
    previousStep: MovementStep?,
    nextStep: MovementStep?,
    onSelectStep: (Int) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "十式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StepSideCard(
                    modifier = Modifier.weight(0.9f),
                    title = previousStep?.label ?: "",
                    level = previousStep?.level,
                    enabled = previousStep != null,
                    onClick = { previousStep?.let { onSelectStep(it.level) } },
                )
                CurrentStepCard(
                    modifier = Modifier.weight(1.2f),
                    step = currentStep,
                )
                StepSideCard(
                    modifier = Modifier.weight(0.9f),
                    title = nextStep?.label ?: "",
                    level = nextStep?.level,
                    enabled = nextStep != null,
                    onClick = { nextStep?.let { onSelectStep(it.level) } },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val targetLevel = (currentStep.level - 1).coerceAtLeast(1)
                        onSelectStep(targetLevel)
                    },
                    enabled = currentStep.level > 1,
                ) {
                    Text("上一式")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val targetLevel = (currentStep.level + 1).coerceAtMost(10)
                        onSelectStep(targetLevel)
                    },
                    enabled = currentStep.level < 10,
                ) {
                    Text("下一式")
                }
            }

            StepIndexStrip(
                selectedLevel = currentStep.level,
                onSelectStep = onSelectStep,
            )
        }
    }
}

@Composable
private fun StepSideCard(
    modifier: Modifier = Modifier,
    title: String,
    level: Int?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        onClick = onClick,
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (level == null) "" else level.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (enabled) title else "无",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CurrentStepCard(
    modifier: Modifier = Modifier,
    step: MovementStep,
) {
    Card(
        modifier = modifier.height(136.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "第 ${step.level} 式",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = step.label,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "当前选择",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StepIndexStrip(
    selectedLevel: Int,
    onSelectStep: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (1..10).forEach { level ->
            val selected = level == selectedLevel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
                onClick = { onSelectStep(level) },
            ) {
                Text(
                    text = level.toString(),
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun RestPresetCard(
    restPresetOptions: List<Int>,
    selectedRestPresetSeconds: Int,
    onSelectRestPreset: (Int) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "组间休息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
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
                        ) {
                            Text("${seconds}s")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
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

@Composable
private fun QuickEntryRow(
    onOpenStandards: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSummary: () -> Unit,
) {
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
            Text("历史")
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onOpenSummary,
        ) {
            Text("总结")
        }
    }
}
