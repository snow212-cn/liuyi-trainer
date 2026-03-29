package com.liuyi.trainer.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.liuyi.trainer.model.ExerciseCatalog
import com.liuyi.trainer.model.TrainingSessionState
import com.liuyi.trainer.ui.HomeScreen
import com.liuyi.trainer.ui.StandardsScreen
import com.liuyi.trainer.ui.TrainingHistoryDetailScreen
import com.liuyi.trainer.ui.TrainingHistoryScreen
import com.liuyi.trainer.ui.TrainingPreparationScreen
import com.liuyi.trainer.ui.TrainingReadyScreen
import com.liuyi.trainer.ui.TrainingRestScreen
import com.liuyi.trainer.ui.TrainingRunningScreen
import com.liuyi.trainer.ui.TrainingSummaryScreen
import com.liuyi.trainer.ui.buildHistoryDetailPreview
import com.liuyi.trainer.ui.buildHistoryPreview
import com.liuyi.trainer.ui.buildPreparingPreview
import com.liuyi.trainer.ui.buildRestPreview
import com.liuyi.trainer.ui.buildRunningPreview
import com.liuyi.trainer.ui.buildStandardsPreview
import com.liuyi.trainer.ui.buildSummaryPreview
import com.liuyi.trainer.ui.buildTrainingEntryPreview

private object Routes {
    const val Home = "home"
    const val Training = "training"
    const val Rest = "rest"
    const val Summary = "summary"
    const val History = "history"
    const val HistoryDetail = "history_detail"
    const val Standards = "standards"
}

@Composable
fun LiuyiTrainerApp(
    appViewModel: LiuyiTrainerViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val activeContext = appViewModel.activeContext
    val activeRoute = routeForSessionState(appViewModel.sessionState)

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
    ) {
        composable(Routes.Home) {
            HomeScreen(
                families = ExerciseCatalog.families,
                selectedFamilyId = appViewModel.selectedFamilyId,
                selectedStepLevel = appViewModel.selectedStepLevel,
                restPresetSeconds = appViewModel.restPresetSeconds,
                restPresetOptions = appViewModel.restPresetOptions,
                previewCadenceLabel = ExerciseCatalog.previewCadence.label,
                previewCadenceSeconds = ExerciseCatalog.previewCadence.cycleDurationMs / 1000,
                onSelectFamily = appViewModel::selectFamily,
                onSelectStep = appViewModel::selectStep,
                onSelectRestPreset = appViewModel::selectRestPreset,
                onStartTraining = {
                    navController.navigate(Routes.Training)
                },
                hasActiveSession = appViewModel.sessionState !is TrainingSessionState.Idle &&
                    appViewModel.sessionState !is TrainingSessionState.Completed,
                activeSessionLabel = activeSessionHomeLabel(appViewModel.sessionState),
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
                onOpenSummary = {
                    navController.navigate(Routes.Summary)
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
                    ),
                    speechEnabled = appViewModel.speechEnabled,
                    onToggleSpeech = appViewModel::updateSpeechEnabled,
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
                        cadenceLabel = ExerciseCatalog.previewCadence.label,
                        cadenceSeconds = ExerciseCatalog.previewCadence.cycleDurationMs / 1000,
                    ),
                    speechEnabled = appViewModel.speechEnabled,
                    onToggleSpeech = appViewModel::updateSpeechEnabled,
                    onBack = {
                        navController.popBackStack(Routes.Home, false)
                    },
                    onStartSet = {
                        appViewModel.beginTraining()
                        navController.navigate(Routes.Training) {
                            popUpTo(Routes.Training) {
                                inclusive = true
                            }
                        }
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
                    ),
                    speechEnabled = appViewModel.speechEnabled,
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

        composable(Routes.History) {
            TrainingHistoryScreen(
                preview = buildHistoryPreview(appViewModel.recentSessions),
                onBack = {
                    navController.popBackStack()
                },
                onBackHome = {
                    navController.popBackStack(Routes.Home, false)
                },
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
                onSave = appViewModel::saveSelectedHistoryEdits,
            )
        }

        composable(Routes.Standards) {
            StandardsScreen(
                preview = buildStandardsPreview(activeContext),
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
