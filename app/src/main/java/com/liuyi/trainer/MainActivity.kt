package com.liuyi.trainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.liuyi.trainer.app.LiuyiTrainerApp
import com.liuyi.trainer.ui.theme.LiuyiTrainerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiuyiTrainerTheme {
                LiuyiTrainerApp()
            }
        }
    }
}

