package com.liuyi.trainer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrainingSessionEntity::class,
        TrainingSetEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class LiuyiTrainerDatabase : RoomDatabase() {
    abstract fun trainingHistoryDao(): TrainingHistoryDao

    companion object {
        fun build(context: Context): LiuyiTrainerDatabase = Room.databaseBuilder(
            context,
            LiuyiTrainerDatabase::class.java,
            "liuyi_trainer.db",
        ).fallbackToDestructiveMigration()
            .build()
    }
}
