package com.example.mymusic

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mymusic.databinding.ActivityPlayerBinding
import com.example.mymusic.db.Music
import com.example.mymusic.db.requestAudioFocusMy
import com.example.mymusic.db.setSongPosition
import com.example.mymusic.services.MyMusicService
import com.example.mymusic.utils.CommonMethod.formatDuration
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MyMusicService? = null
        var nowPlayingId: String = ""
        var repeate: Boolean = false

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.data?.scheme.contentEquals("content")) {
            songPosition = 0
            val intentService = Intent(this, MyMusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            binding.songName.text = musicListPA[songPosition].title
        } else {
            initializeLayout()
        }




        binding.playerPageBackBtn.setOnClickListener {
            finish()
        }

        musicListPA = MainActivity.MusicListMA


        binding.apply {
            playSongBtn.setOnClickListener { if (isPlaying) pauseMusic() else playMusic() }
            repeat.setOnClickListener {
                if (!repeate) {
                    repeate = true
                    repeat.setImageResource(R.drawable.baseline_repeat_24)
                    musicService!!.mediaPlayer!!.isLooping = false

                } else {
                    repeate = false
                    repeat.setImageResource(R.drawable.baseline_repeat_one_24)
                    musicService!!.mediaPlayer!!.isLooping = true

                }
            }
            share.setOnClickListener {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "audio/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
                startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))

            }
            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        musicService!!.mediaPlayer!!.seekTo(progress)

                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
            playSongBack.setOnClickListener { prevNextSong(false) }
            playSongNext.setOnClickListener { prevNextSong(true) }

        }


    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "NowPlaying" -> {
                setLayout()
                musicListPA = ArrayList()
                binding.startTime.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition)
                binding.enadTime.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration)
                binding.seekbar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekbar.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.playSongBtn.setIconResource(R.drawable.baseline_pause_24)
                else binding.playSongBtn.setIconResource(R.drawable.baseline_play_arrow_24)
            }

            "MusicAdapter" -> initServiceAndPlaylist(MainActivity.MusicListMA)
            "MainActivity" -> {
                val startCommandIntent = Intent(this@PlayerActivity, MyMusicService::class.java)
                bindService(startCommandIntent, this, BIND_AUTO_CREATE)
                startService(startCommandIntent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
            }

        }
    }

    private fun initServiceAndPlaylist(playlist: ArrayList<Music>) {
        val intent = Intent(this, MyMusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        musicListPA = ArrayList()
        musicListPA.addAll(playlist)
        setLayout()

    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {

        if(musicService == null){
            val binder = p1 as MyMusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }



    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            binding.startTime.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition)
            binding.enadTime.text = formatDuration(musicService!!.mediaPlayer!!.duration)
            binding.seekbar.progress = 0
            binding.seekbar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
            playMusic()
            binding.playSongBtn.setIconResource(R.drawable.baseline_pause_24)
            musicService!!.showNotification(R.drawable.baseline_pause_24)
        } catch (e: Exception) {
            return
        }
    }


    private fun setLayout() {
        if (repeate) {
            binding.repeat.setImageResource(R.drawable.baseline_repeat_24)
        }
        binding.songName.text = musicListPA[songPosition].title

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!
            return Music(
                id = "Unknown",
                title = path.toString(),
                album = "Unknown",
                artist = "Unknown",
                duration = duration,
                path = path.toString()
            )
        } finally {
            cursor?.close()
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        setSongPosition(true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {
            return
        }
    }

    private fun playMusic() {
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        binding.playSongBtn.setIconResource(R.drawable.baseline_pause_24)
        musicService!!.showNotification(R.drawable.baseline_pause_24)
        if (musicService!!.isFocusLoass) {
            requestAudioFocusMy(this@PlayerActivity)
        }
    }



    private fun pauseMusic() {
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        binding.playSongBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        musicService!!.showNotification(R.drawable.baseline_pause_24)

    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(true)
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == "Unknown" && !isPlaying) exitProcess(1)
    }


}
