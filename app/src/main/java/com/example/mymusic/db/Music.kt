package com.example.mymusic.db

import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import com.example.mymusic.PlayerActivity

import com.example.mymusic.PlayerActivity.Companion.musicService
import kotlin.system.exitProcess

data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val path: String,
    val duration: Long = 0,
)

fun setSongPosition(increment: Boolean) {
    if (!PlayerActivity.repeate) {
        if (increment) {
            if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition) {
                PlayerActivity.songPosition = 0
            } else {
                ++PlayerActivity.songPosition
            }

        } else {
            if (0 == PlayerActivity.songPosition) {
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
            } else {
                --PlayerActivity.songPosition
            }
        }
    }
}

fun exitApplication() {
    if (musicService != null) {
        musicService!!.audioManager.abandonAudioFocus(musicService)
        musicService!!.stopForeground(true)
        musicService!!.mediaPlayer!!.release()
        musicService = null
    }
    exitProcess(1)

}

fun requestAudioFocusMy(context: Context) {
    musicService!!.audioManager =
        context.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
    musicService!!.audioManager.requestAudioFocus(
        musicService,
        AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN
    )
}