package com.liuyi.trainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
    onSelectFamily: (String) -> Unit,
    onSelectStep: (Int) -> Unit,
    onStartTraining: () -> Unit,
    hasActiveSession: Boolean,
    activeSessionLabel: String?,
    settingsSummary: String,
    onOpenActiveSession: () -> Unit,
    onFinishActiveSession: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStandards: () -> Unit,
) {
    val selectedFamily = families.firstOrNull { it.id == selectedFamilyId } ?: families.first()
    val selectedStep = selectedFamily.steps.firstOrNull { it.level == selectedStepLevel }
        ?: selectedFamily.steps.first()
    val previousStep = selectedFamily.steps.getOrNull(selectedStep.level - 2)
    val nextStep = selectedFamily.steps.getOrNull(selectedStep.level)

    PrisonSurface {
        PrisonScrollColumn {
            HomeTitleBar()

            SteelPanel {
                SectionKicker(text = "当前训练")
                Text(
                    text = "${selectedFamily.titleZh}·${selectedStep.label}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MutedBody(text = selectedStep.label)
                    MutedBody(text = settingsSummary)
                }
                if (hasActiveSession && activeSessionLabel != null) {
                    ActiveSessionStrip(
                        label = activeSessionLabel,
                        onOpenActiveSession = onOpenActiveSession,
                        onFinishActiveSession = onFinishActiveSession,
                    )
                }
                HomeActionGrid(
                    hasActiveSession = hasActiveSession,
                    onStartTraining = onStartTraining,
                    onOpenActiveSession = onOpenActiveSession,
                    onOpenSettings = onOpenSettings,
                    onOpenStandards = onOpenStandards,
                    onOpenHistory = onOpenHistory,
                )
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "六艺切换",
                    subtitle = "6 项全览",
                )
                FamilyGrid(
                    families = families,
                    selectedFamilyId = selectedFamily.id,
                    onSelectFamily = onSelectFamily,
                )
            }

            SteelPanel(soft = true) {
                SteelSectionHeader(
                    title = "十式切换",
                    subtitle = "第${selectedStep.level}式",
                )
                StepSelectionBlock(
                    currentStep = selectedStep,
                    totalSteps = selectedFamily.steps.size,
                    previousStep = previousStep,
                    nextStep = nextStep,
                    onSelectStep = onSelectStep,
                )
            }

        }
    }
}

@Composable
private fun HomeTitleBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "六艺总表",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun HomeActionGrid(
    hasActiveSession: Boolean,
    onStartTraining: () -> Unit,
    onOpenActiveSession: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStandards: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SteelPrimaryButton(
                text = if (hasActiveSession) "回到训练" else "进入训练",
                onClick = if (hasActiveSession) onOpenActiveSession else onStartTraining,
                modifier = Modifier.weight(1f),
            )
            SteelSecondaryButton(
                text = "动作标准",
                onClick = onOpenStandards,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SteelSecondaryButton(
                text = "训练历史",
                onClick = onOpenHistory,
                modifier = Modifier.weight(1f),
            )
            SteelSecondaryButton(
                text = "训练设置",
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActiveSessionStrip(
    label: String,
    onOpenActiveSession: () -> Unit,
    onFinishActiveSession: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenActiveSession)
                .clip(RoundedInner)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        SteelSecondaryButton(
            text = "结束本次",
            onClick = onFinishActiveSession,
            modifier = Modifier.weight(0.72f),
        )
    }
}

@Composable
private fun HomeMetaTag(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedInner)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FamilyGrid(
    families: List<MovementFamily>,
    selectedFamilyId: String,
    onSelectFamily: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        families.chunked(3).forEach { rowFamilies ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowFamilies.forEach { family ->
                    val selected = family.id == selectedFamilyId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedInner)
                            .border(
                                width = 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                                },
                                shape = RoundedInner,
                            )
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)
                                },
                            )
                            .clickable { onSelectFamily(family.id) }
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = family.titleZh,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = family.titleEn.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepSelectionBlock(
    currentStep: MovementStep,
    totalSteps: Int,
    previousStep: MovementStep?,
    nextStep: MovementStep?,
    onSelectStep: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedInner)
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedInner)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        shape = RoundedInner,
                    )
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.36f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = currentStep.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    MutedBody(text = "${currentStep.level}/$totalSteps")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NeighborStepTile(
                modifier = Modifier.weight(1f),
                label = "上一式",
                step = previousStep,
                onClick = { previousStep?.let { onSelectStep(it.level) } },
            )
            NeighborStepTile(
                modifier = Modifier.weight(1f),
                label = "下一式",
                step = nextStep,
                onClick = { nextStep?.let { onSelectStep(it.level) } },
            )
        }
    }
}

@Composable
private fun NeighborStepTile(
    label: String,
    step: MovementStep?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedInner)
            .border(
                width = 1.dp,
                color = if (step != null) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                },
                shape = RoundedInner,
            )
            .background(
                if (step != null) {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
                },
            )
            .clickable(enabled = step != null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = if (step == null) "无" else "${step.level}式",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = step?.label ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RestNotchRow(
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedInner)
                    .border(
                        width = 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                        },
                        shape = RoundedInner,
                    )
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)
                        },
                    )
                    .clickable { onSelectRestPreset(seconds) }
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(if (selected) 22.dp else 16.dp)
                            .clip(RoundedFull)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                },
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

private val RoundedInner = PrisonPanelShape
private val RoundedFull = RoundedInner
