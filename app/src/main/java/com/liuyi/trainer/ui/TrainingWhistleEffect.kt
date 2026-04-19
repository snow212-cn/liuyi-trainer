package com.liuyi.trainer.ui

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.liuyi.trainer.R
import kotlinx.coroutines.delay

private const val WhistleAudibleDurationMs = 1_000L

@Composable
fun WhistleCueEffect(
    sequenceId: String,
    cueKey: String?,
    lastPlayedCueToken: String?,
    onCuePlayed: (String) -> Unit,
) {
    val context = LocalContext.current
    val cueToken = cueKey?.let { "$sequenceId|$it" }

    LaunchedEffect(cueToken, lastPlayedCueToken) {
        if (cueToken.isNullOrBlank() || cueToken == lastPlayedCueToken) {
            return@LaunchedEffect
        }

        val player = MediaPlayer.create(context.applicationContext, R.raw.referee_whistle_real_alt)
        if (player == null) {
            return@LaunchedEffect
        }

        val playbackMs = player.duration
            .takeIf { it > 0 }
            ?.toLong()
            ?.coerceAtMost(WhistleAudibleDurationMs)
            ?: WhistleAudibleDurationMs

        try {
            player.start()
            delay(playbackMs)
            onCuePlayed(cueToken)
        } finally {
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            player.release()
        }
    }
}
