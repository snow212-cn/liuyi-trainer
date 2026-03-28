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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                TrainingBoardCard(
                    family = selectedFamily,
                    step = selectedStep,
                    cadenceLabel = previewCadenceLabel,
                    cadenceSeconds = previewCadenceSeconds,
                    restPresetSeconds = restPresetSeconds,
                    onStartTraining = onStartTraining,
                    onOpenStandards = onOpenStandards,
                )
            }

            item {
                SectionCard(
                    title = "六艺切换",
                    subtitle = "6 项全览",
                ) {
                    FamilySteelGrid(
                        families = families,
                        selectedFamilyId = selectedFamily.id,
                        onSelectFamily = onSelectFamily,
                    )
                }
            }

            item {
                SectionCard(
                    title = "十式切换",
                    subtitle = "第 ${selectedStep.level} 式",
                ) {
                    StepDialCard(
                        currentStep = selectedStep,
                        previousStep = previousStep,
                        nextStep = nextStep,
                        onSelectStep = onSelectStep,
                    )
                }
            }

            item {
                SectionCard(
                    title = "组间休息",
                    subtitle = "刻度预设",
                ) {
                    RestRail(
                        restPresetOptions = restPresetOptions,
                        selectedRestPresetSeconds = restPresetSeconds,
                        onSelectRestPreset = onSelectRestPreset,
                    )
                }
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
private fun TrainingBoardCard(
    family: MovementFamily,
    step: MovementStep,
    cadenceLabel: String,
    cadenceSeconds: Long,
    restPresetSeconds: Int,
    onStartTraining: () -> Unit,
    onOpenStandards: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "训练指挥台",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = step.level.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = family.titleZh,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = step.label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${cadenceSeconds} 秒/次  ·  休息 ${restPresetSeconds} 秒",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BoardTag(
                    modifier = Modifier.weight(1f),
                    label = cadenceLabel,
                )
                BoardTag(
                    modifier = Modifier.weight(1f),
                    label = "第 ${step.level} 式",
                )
            }

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
        }
    }
}

@Composable
private fun BoardTag(
    modifier: Modifier = Modifier,
    label: String,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun FamilySteelGrid(
    families: List<MovementFamily>,
    selectedFamilyId: String,
    onSelectFamily: (String) -> Unit,
) {
    families.chunked(3).forEach { rowFamilies ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rowFamilies.forEach { family ->
                val selected = family.id == selectedFamilyId
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 72.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                    border = if (selected) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                    } else {
                        null
                    },
                    onClick = { onSelectFamily(family.id) },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = family.titleZh,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = family.titleEn.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun StepDialCard(
    currentStep: MovementStep,
    previousStep: MovementStep?,
    nextStep: MovementStep?,
    onSelectStep: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = currentStep.level.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
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
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "当前式名",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = currentStep.label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        currentStepRange().chunked(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { level ->
                    val selected = level == currentStep.level
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                        border = if (selected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                        } else {
                            null
                        },
                        onClick = { onSelectStep(level) },
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = level.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NeighborStepCard(
                modifier = Modifier.weight(1f),
                label = "上一式",
                step = previousStep,
                active = false,
                onClick = { previousStep?.let { onSelectStep(it.level) } },
            )
            NeighborStepCard(
                modifier = Modifier.weight(1f),
                label = "当前式",
                step = currentStep,
                active = true,
                onClick = { onSelectStep(currentStep.level) },
            )
            NeighborStepCard(
                modifier = Modifier.weight(1f),
                label = "下一式",
                step = nextStep,
                active = false,
                onClick = { nextStep?.let { onSelectStep(it.level) } },
            )
        }
    }
}

private fun currentStepRange(): List<Int> = (1..10).toList()

@Composable
private fun NeighborStepCard(
    modifier: Modifier = Modifier,
    label: String,
    step: MovementStep?,
    active: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.heightIn(min = 82.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        border = if (active) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        } else {
            null
        },
        onClick = onClick,
        enabled = step != null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (step == null) "无" else "第 ${step.level} 式",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = step?.label ?: "",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun RestRail(
    restPresetOptions: List<Int>,
    selectedRestPresetSeconds: Int,
    onSelectRestPreset: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        restPresetOptions.forEach { seconds ->
            val selected = seconds == selectedRestPresetSeconds
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(76.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
                border = if (selected) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                } else {
                    null
                },
                onClick = { onSelectRestPreset(seconds) },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(if (selected) 24.dp else 18.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                    Text(
                        text = "${seconds}s",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
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
