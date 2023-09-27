package com.example.mymusic.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.app.NotificationCompat
import com.example.mymusic.R
import com.example.mymusic.ApplicationClass
import com.example.mymusic.MainActivity
import com.example.mymusic.PlayerActivity
import com.example.mymusic.brodcast.NotificationReceiver
import com.example.mymusic.fragment.NowPlayingFragment
import com.example.mymusic.utils.CommonMethod

class MyMusicService : Service(), AudioManager.OnAudioFocusChangeListener {

    var mediaPlayer: MediaPlayer? = null
    private var myBinder = MyBinder()
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager
    var isFocusLoass = false

    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MyMusicService {
            return this@MyMusicService
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn: Int) {
        val intent = Intent(baseContext, MainActivity::class.java)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.PREVIOUS
        )
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)

        val exitIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)


        val notification =
            androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
                .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
                .setSmallIcon(R.drawable.musical)
                .setStyle(
                    NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken)
                )
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setNotificationSilent()
                .addAction(R.drawable.privous, "Previous", prevPendingIntent)
                .addAction(playPauseBtn, "Play", playPendingIntent)
                .addAction(R.drawable.baseline_navigate_next_24, "Next", nextPendingIntent)
                .addAction(R.drawable.baseline_exit_to_app_24, "Exit", exitPendingIntent)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playbackSpeed = if (PlayerActivity.isPlaying) 1F else 0F
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            val playBackState = PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer!!.currentPosition.toLong(),
                    playbackSpeed
                )
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSession.setPlaybackState(playBackState)
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {


                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    if (PlayerActivity.isPlaying) {
                        //pause music
                        PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_play_arrow_24)
                        // NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
                        PlayerActivity.isPlaying = false
                        mediaPlayer!!.pause()
                        showNotification(R.drawable.baseline_play_arrow_24)
                    } else {
                        //play music
                        PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_pause_24)
                        // NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24)
                        PlayerActivity.isPlaying = true
                        mediaPlayer!!.start()
                        showNotification(R.drawable.baseline_pause_24)
                    }
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }

                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }
            })
        }

        startForeground(13, notification)
    }

    fun createMediaPlayer() {

        try {
            if (mediaPlayer == null) mediaPlayer = MediaPlayer()
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            mediaPlayer!!.prepare()
            PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_pause_24)
            showNotification(R.drawable.baseline_pause_24)
            PlayerActivity.binding.startTime.text =
                CommonMethod.formatDuration(mediaPlayer!!.currentPosition)
            PlayerActivity.binding.enadTime.text =
                CommonMethod.formatDuration(mediaPlayer!!.duration)
            PlayerActivity.binding.seekbar.progress = 0
            PlayerActivity.binding.seekbar.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id

        } catch (e: Exception) {
            return
        }
    }

    fun seekBarSetup() {
        runnable = Runnable {
            PlayerActivity.binding.startTime.text =
                CommonMethod.formatDuration(mediaPlayer!!.currentPosition)
            PlayerActivity.binding.seekbar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }



    override fun onAudioFocusChange(focusChange: Int) {

        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> { PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_play_arrow_24)
            NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
            PlayerActivity.isPlaying = false
                isFocusLoass = true
            mediaPlayer!!.pause()
            showNotification(R.drawable.baseline_play_arrow_24)
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_play_arrow_24)
                PlayerActivity.isPlaying = false
                mediaPlayer!!.pause()
                showNotification(R.drawable.baseline_play_arrow_24)
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_pause_24)
                NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24)
                PlayerActivity.isPlaying = true
                mediaPlayer!!.start()
                showNotification(R.drawable.baseline_pause_24)
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

}