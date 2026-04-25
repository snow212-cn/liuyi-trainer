package com.liuyi.trainer.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

private const val TrainingMusicVolume = 0.52f

@Composable
fun TrainingBackgroundMusicEffect(
    enabled: Boolean,
    trackResId: Int?,
) {
    val context = LocalContext.current

    DisposableEffect(context, enabled, trackResId) {
        if (!enabled || trackResId == null) {
            onDispose { }
        } else {
            val player = buildTrainingMusicPlayer(
                context = context,
                trackResId = trackResId,
            )
            player?.start()

            onDispose {
                player?.stop()
                player?.release()
            }
        }
    }
}

private fun buildTrainingMusicPlayer(
    context: Context,
    trackResId: Int,
): MediaPlayer? = MediaPlayer.create(context.applicationContext, trackResId)?.apply {
    isLooping = true
    setVolume(TrainingMusicVolume, TrainingMusicVolume)
}
