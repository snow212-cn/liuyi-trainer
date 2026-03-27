package com.liuyi.trainer.app

import android.app.Application
import com.liuyi.trainer.data.LiuyiTrainerDatabase
import com.liuyi.trainer.data.TrainingHistoryRepository

class LiuyiTrainerApplication : Application() {
    val database: LiuyiTrainerDatabase by lazy {
        LiuyiTrainerDatabase.build(this)
    }

    val trainingHistoryRepository: TrainingHistoryRepository by lazy {
        TrainingHistoryRepository(database.trainingHistoryDao())
    }
}

