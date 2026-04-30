package com.liuyi.trainer.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.liuyi.trainer.model.ExerciseCatalog
import com.liuyi.trainer.model.TrainingSessionState
import com.liuyi.trainer.ui.HomeScreen
import com.liuyi.trainer.ui.StandardsScreen
import com.liuyi.trainer.ui.TrainingBackgroundMusicEffect
import com.liuyi.trainer.ui.TrainingHistoryDetailScreen
import com.liuyi.trainer.ui.TrainingHistoryScreen
import com.liuyi.trainer.ui.TrainingPreparationScreen
import com.liuyi.trainer.ui.TrainingReadyScreen
import com.liuyi.trainer.ui.TrainingRestScreen
import com.liuyi.trainer.ui.TrainingRunningScreen
import com.liuyi.trainer.ui.TrainingSettingsScreen
import com.liuyi.trainer.ui.TrainingSummaryScreen
import com.liuyi.trainer.ui.buildHistoryDetailPreview
import com.liuyi.trainer.ui.buildHistoryPreview
import com.liuyi.trainer.ui.buildPreparingPreview
import com.liuyi.trainer.ui.buildRestPreview
import com.liuyi.trainer.ui.buildRunningPreview
import com.liuyi.trainer.ui.buildSettingsPreview
import com.liuyi.trainer.ui.buildStandardsPreview
import com.liuyi.trainer.ui.buildSummaryPreview
import com.liuyi.trainer.ui.buildTrainingEntryPreview
import com.liuyi.trainer.ui.findTrainingBackgroundMusicOption

private object Routes {
    const val Home = "home"
    const val Training = "training"
    const val Rest = "rest"
    const val Summary = "summary"
    const val History = "history"
    const val HistoryDetail = "history_detail"
    const val Standards = "standards"
    const val Settings = "settings"
}

@Composable
fun LiuyiTrainerApp(
    appViewModel: LiuyiTrainerViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val activeContext = appViewModel.activeContext
    val activeRoute = routeForSessionState(appViewModel.sessionState)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val selectedBackgroundMusic = findTrainingBackgroundMusicOption(appViewModel.selectedBackgroundMusicId)
    val hasLiveTrainingStage = when (appViewModel.sessionState) {
        is TrainingSessionState.PreparingSet,
        is TrainingSessionState.SetRunning,
        is TrainingSessionState.RestRunning,
        is TrainingSessionState.RestOvertime -> true

        else -> false
    }
    val shouldPlayTrainingMusic =
        appViewModel.backgroundMusicEnabled &&
            selectedBackgroundMusic != null &&
            hasLiveTrainingStage &&
            (currentRoute == Routes.Training || currentRoute == Routes.Rest)

    TrainingBackgroundMusicEffect(
        enabled = shouldPlayTrainingMusic,
        trackResId = selectedBackgroundMusic?.rawResId,
    )

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
    ) {
        composable(Routes.Home) {
            HomeScreen(
                families = ExerciseCatalog.families,
                selectedFamilyId = appViewModel.selectedFamilyId,
                selectedStepLevel = appViewModel.selectedStepLevel,
                onSelectFamily = appViewModel::selectFamily,
                onSelectStep = appViewModel::selectStep,
                onStartTraining = {
                    navController.navigate(Routes.Training)
                },
                hasActiveSession = appViewModel.sessionState !is TrainingSessionState.Idle &&
                    appViewModel.sessionState !is TrainingSessionState.Completed,
                activeSessionLabel = activeSessionHomeLabel(appViewModel.sessionState),
                settingsSummary = homeSettingsSummary(appViewModel),
                onOpenActiveSession = {
                    navController.navigate(activeRoute)
                },
                onFinishActiveSession = {
                    val movedToSummary = appViewModel.finishActiveTrainingFromAnywhere()
                    navController.navigate(if (movedToSummary) Routes.Summary else Routes.Home) {
                        popUpTo(Routes.Home) {
                            inclusive = false
                        }
                    }
                },
                onOpenSettings = {
                    navController.navigate(Routes.Settings)
                },
                onOpenHistory = {
                    navController.navigate(Routes.History)
                },
                onOpenStandards = {
                    navController.navigate(Routes.Standards)
                },
            )
        }

        composable(Routes.Training) {
            val currentState = appViewModel.sessionState
            if (currentState is TrainingSessionState.PreparingSet) {
                TrainingPreparationScreen(
                    preview = buildPreparingPreview(
                        context = activeContext,
                        state = currentState,
                        nowUtc = appViewModel.nowUtc,
                    ),
                    speechEnabled = appViewModel.speechEnabled,
                    selectedVoiceId = appViewModel.selectedVoiceId,
                    lastSpokenCueToken = appViewModel.lastSpokenCueToken,
                    onSpeechCueSpoken = appViewModel::markSpeechCueSpoken,
                    onBack = {
                        navController.popBackStack(Routes.Home, false)
                    },
                )
            } else if (currentState is TrainingSessionState.SetRunning) {
                TrainingRunningScreen(
                    preview = buildRunningPreview(
                        context = activeContext,
                        state = currentState,
                        nowUtc = appViewModel.nowUtc,
                        voiceGuideMode = appViewModel.voiceGuideMode,
                        speechEnabled = appViewModel.speechEnabled,
                    ),
                    selectedVoiceId = appViewModel.selectedVoiceId,
                    lastSpokenCueToken = appViewModel.lastSpokenCueToken,
                    onSpeechCueSpoken = appViewModel::markSpeechCueSpoken,
                    lastPlayedWhistleCueToken = appViewModel.lastPlayedWhistleCueToken,
                    lastPlayedWhistleCompletedAtMs = appViewModel.lastPlayedWhistleCompletedAtMs,
                    onWhistleCuePlayed = appViewModel::markWhistleCuePlayed,
                    onBack = {
                        navController.popBackStack(Routes.Home, false)
                    },
                    onFinishSet = {
                        appViewModel.finishSet()
                        navController.navigate(Routes.Rest)
                    },
                    onCompleteTraining = {
                        appViewModel.completeTraining()
                        navController.navigate(Routes.Summary)
                    },
                )
            } else {
                TrainingReadyScreen(
                    preview = buildTrainingEntryPreview(
                        context = appViewModel.selectedContext,
                        restPresetSeconds = appViewModel.restPresetSeconds,
                        cadenceLabel = "${appViewModel.customEccentricSeconds}-${appViewModel.customBottomPauseSeconds}-${appViewModel.customConcentricSeconds} 自定义节奏",
                        cadenceSeconds = (appViewModel.customEccentricSeconds + appViewModel.customBottomPauseSeconds + appViewModel.customConcentricSeconds).toLong(),
                    ),
                    onBack = {
                        navController.popBackStack(Routes.Home, false)
                    },
                    onStartSet = {
                        appViewModel.beginTraining()
                    },
                    onOpenStandards = {
                        navController.navigate(Routes.Standards)
                    },
                )
            }
        }

        composable(Routes.Rest) {
            val currentState = appViewModel.sessionState
            if (currentState is TrainingSessionState.RestRunning ||
                currentState is TrainingSessionState.RestOvertime
            ) {
                TrainingRestScreen(
                    preview = buildRestPreview(
                        context = activeContext,
                        state = currentState,
                        nowUtc = appViewModel.nowUtc,
                        restCountdownVoiceEnabled = appViewModel.restCountdownVoiceEnabled,
                        speechEnabled = appViewModel.speechEnabled,
                    ),
                    speechEnabled = appViewModel.speechEnabled,
                    selectedVoiceId = appViewModel.selectedVoiceId,
                    lastSpokenCueToken = appViewModel.lastSpokenCueToken,
                    onSpeechCueSpoken = appViewModel::markSpeechCueSpoken,
                    lastPlayedWhistleCueToken = appViewModel.lastPlayedWhistleCueToken,
                    lastPlayedWhistleCompletedAtMs = appViewModel.lastPlayedWhistleCompletedAtMs,
                    onWhistleCuePlayed = appViewModel::markWhistleCuePlayed,
                    onBack = {
                        navController.popBackStack(Routes.Home, false)
                    },
                    onStartNextSet = {
                        appViewModel.beginNextSet()
                        navController.navigate(Routes.Training) {
                            popUpTo(Routes.Training) {
                                inclusive = true
                            }
                        }
                    },
                    onCompleteTraining = {
                        appViewModel.completeTraining()
                        navController.navigate(Routes.Summary)
                    },
                )
            }
        }

        composable(Routes.Summary) {
            TrainingSummaryScreen(
                preview = buildSummaryPreview(
                    context = activeContext,
                    state = appViewModel.sessionState,
                    nowUtc = appViewModel.nowUtc,
                    repDrafts = appViewModel.summaryRepDrafts,
                    isSaved = appViewModel.summarySaved,
                ),
                onBack = {
                    navController.popBackStack()
                },
                onBackHome = {
                    navController.popBackStack(Routes.Home, false)
                },
                onOpenHistory = {
                    navController.navigate(Routes.History)
                },
                onUpdateRep = appViewModel::updateSummaryRep,
                onSave = appViewModel::saveCompletedTraining,
            )
        }

        composable(Routes.Settings) {
            TrainingSettingsScreen(
                preview = buildSettingsPreview(
                    speechEnabled = appViewModel.speechEnabled,
                    voiceGuideMode = appViewModel.voiceGuideMode,
                    availableVoices = appViewModel.availableVoices,
                    selectedVoiceId = appViewModel.selectedVoiceId,
                    restPresetSeconds = appViewModel.restPresetSeconds,
                    restPresetOptions = appViewModel.restPresetOptions,
                    preparationSeconds = appViewModel.preparationSeconds,
                    preparationOptions = appViewModel.preparationOptions,
                    restCountdownVoiceEnabled = appViewModel.restCountdownVoiceEnabled,
                    backgroundMusicEnabled = appViewModel.backgroundMusicEnabled,
                    backgroundMusicOptions = appViewModel.backgroundMusicOptions,
                    selectedBackgroundMusicId = appViewModel.selectedBackgroundMusicId,
                    customEccentricSeconds = appViewModel.customEccentricSeconds,
                    customBottomPauseSeconds = appViewModel.customBottomPauseSeconds,
                    customConcentricSeconds = appViewModel.customConcentricSeconds,
                ),
                onBack = {
                    navController.popBackStack()
                },
                onBackHome = {
                    navController.popBackStack(Routes.Home, false)
                },
                onUpdateSpeechEnabled = appViewModel::updateSpeechEnabled,
                onUpdateVoiceGuideMode = appViewModel::updateVoiceGuideMode,
                onUpdateSelectedVoice = appViewModel::updateSelectedVoice,
                onUpdateRestPreset = appViewModel::selectRestPreset,
                onUpdatePreparationSeconds = appViewModel::updatePreparationSeconds,
                onUpdateRestCountdownVoiceEnabled = appViewModel::updateRestCountdownVoiceEnabled,
                onUpdateBackgroundMusicEnabled = appViewModel::updateBackgroundMusicEnabled,
                onUpdateSelectedBackgroundMusic = appViewModel::updateSelectedBackgroundMusic,
                onUpdateCustomEccentricSeconds = appViewModel::updateCustomEccentricSeconds,
                onUpdateCustomBottomPauseSeconds = appViewModel::updateCustomBottomPauseSeconds,
                onUpdateCustomConcentricSeconds = appViewModel::updateCustomConcentricSeconds,
            )
        }

        composable(Routes.History) {
            TrainingHistoryScreen(
                preview = buildHistoryPreview(
                    sessions = appViewModel.recentSessions,
                    transferStatus = appViewModel.historyTransferStatus,
                    latestExportLabel = appViewModel.latestHistoryExportLabel,
                    latestExportUri = appViewModel.latestHistoryExportUri,
                ),
                onBack = {
                    navController.popBackStack()
                },
                onBackHome = {
                    navController.popBackStack(Routes.Home, false)
                },
                onExportToUri = appViewModel::exportHistoryBackup,
                onImportFromUri = appViewModel::importHistoryBackup,
                onOpenDetail = { sessionId ->
                    appViewModel.selectHistorySession(sessionId)
                    navController.navigate(Routes.HistoryDetail)
                },
            )
        }

        composable(Routes.HistoryDetail) {
            TrainingHistoryDetailScreen(
                preview = buildHistoryDetailPreview(
                    sessionWithSets = appViewModel.selectedHistorySession,
                    repDrafts = appViewModel.historyRepDrafts,
                    durationDrafts = appViewModel.historyDurationDrafts,
                    hasPendingEdits = appViewModel.historyEditsDirty,
                ),
                onBack = {
                    navController.popBackStack()
                },
                onBackHome = {
                    navController.popBackStack(Routes.Home, false)
                },
                onReuse = {
                    appViewModel.loadSelectedHistoryAsCurrent()
                    navController.popBackStack(Routes.Home, false)
                },
                onDelete = {
                    appViewModel.deleteSelectedHistorySession()
                    navController.popBackStack()
                },
                onUpdateRep = appViewModel::updateHistoryRep,
                onUpdateDuration = appViewModel::updateHistoryDuration,
                onSave = appViewModel::saveSelectedHistoryEdits,
            )
        }

        composable(Routes.Standards) {
            StandardsScreen(
                preview = buildStandardsPreview(appViewModel.selectedContext),
                onBack = {
                    navController.popBackStack()
                },
                onBackHome = {
                    navController.popBackStack(Routes.Home, false)
                },
                onOpenTraining = {
                    navController.navigate(Routes.Training)
                },
            )
        }
    }
}

private fun routeForSessionState(state: TrainingSessionState): String = when (state) {
    is TrainingSessionState.RestRunning,
    is TrainingSessionState.RestOvertime -> Routes.Rest

    is TrainingSessionState.Completed -> Routes.Summary

    else -> Routes.Training
}

private fun activeSessionHomeLabel(state: TrainingSessionState): String? = when (state) {
    is TrainingSessionState.PreparingSet -> "准备倒计时中"
    is TrainingSessionState.SetRunning -> "当前正在训练"
    is TrainingSessionState.RestRunning -> "当前在组间休息"
    is TrainingSessionState.RestOvertime -> "当前休息已超时"
    else -> null
}

private fun homeSettingsSummary(appViewModel: LiuyiTrainerViewModel): String =
    buildList {
        add(if (appViewModel.speechEnabled) appViewModel.voiceGuideMode.labelZh() else "语音关闭")
        add("${appViewModel.restPresetSeconds}秒休息")
        add(
            if (appViewModel.backgroundMusicEnabled) {
                findTrainingBackgroundMusicOption(appViewModel.selectedBackgroundMusicId)?.label ?: "背景乐开启"
            } else {
                "背景乐关闭"
            },
        )
    }.joinToString("·")

private fun readySettingsSummary(appViewModel: LiuyiTrainerViewModel): String =
    buildList {
        add(if (appViewModel.speechEnabled) appViewModel.voiceGuideMode.labelZh() else "语音关闭")
        add("${appViewModel.restPresetSeconds}秒休息")
        add("${appViewModel.preparationSeconds}秒准备")
        add(
            if (appViewModel.backgroundMusicEnabled) {
                findTrainingBackgroundMusicOption(appViewModel.selectedBackgroundMusicId)?.label ?: "背景乐开启"
            } else {
                "背景乐关闭"
            },
        )
    }.joinToString("·")

private fun com.liuyi.trainer.model.VoiceGuideMode.labelZh(): String = when (this) {
    com.liuyi.trainer.model.VoiceGuideMode.Command -> "起落停"
    com.liuyi.trainer.model.VoiceGuideMode.Counting -> "按秒报数"
    com.liuyi.trainer.model.VoiceGuideMode.Breathing -> "呼吸提示"
}
