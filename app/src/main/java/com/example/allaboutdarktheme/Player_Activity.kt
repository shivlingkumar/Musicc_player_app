package com.example.allaboutdarktheme

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import com.example.allaboutdarktheme.databinding.ActivityPlayerBinding
import com.example.allaboutdarktheme.db.Music
import com.example.allaboutdarktheme.utils.CommonMethod
import com.example.allaboutdarktheme.utils.MusicLoader
import java.io.IOException

class Player_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private val handler = Handler()
   private lateinit var musicList : ArrayList<Music>
    private var currentTrackIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.playerPageBackBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        musicList =  arrayListOf()
        musicList = MusicLoader.getAllAudio(this)

        binding.songName.isSelected = true


        val songPath = intent.getStringExtra("songPath")
        val songName = intent.getStringExtra("songName")
         currentTrackIndex = intent.getIntExtra("currentPosition",0)

        binding.songName.text = songName

        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer.setDataSource(songPath)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaPlayer.setOnCompletionListener {
            playNextTrack()
        }


        binding.playSongNext.setOnClickListener {
            playNextTrack()
        }
        binding.playSongBack.setOnClickListener {
            playPreviousTrack()
        }


        seekBar = binding.seekbar
        seekBar.max = mediaPlayer.duration
        // Start audio playback initially
        mediaPlayer.start()

        updateSeekBar()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }
        })


        // Get the final duration and display it
        val finalDuration = CommonMethod.formatDuration(mediaPlayer.duration)
        binding.enadTime.text = finalDuration

        binding.apply {
            playSongBtn.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                   playSongBtn.setIconResource(R.drawable.baseline_play_arrow_24)
                } else {
                    mediaPlayer.start()
                    playSongBtn.setIconResource(R.drawable.baseline_pause_24)
                    updateSeekBar()
                }
            }
                share.setOnClickListener {
                    shareCurrentMusic()
                }
            var isloop = false
            repeat.setOnClickListener {
                isloop = if (isloop){
                    repeat.setImageResource(R.drawable.baseline_repeat_24)
                    mediaPlayer.isLooping = false
                    false
                }else{
                    repeat.setImageResource(R.drawable.baseline_repeat_one_24)
                    mediaPlayer.isLooping = true
                    true
                }
            }

        }

    }
    private fun playNextTrack() {
        if (currentTrackIndex < musicList.size - 1) {
            try {
                currentTrackIndex++
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.setDataSource(musicList[currentTrackIndex].path)
                mediaPlayer.prepare()
                mediaPlayer.start()
                binding.songName.text = musicList[currentTrackIndex].title
                updateSeekBar()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // If at the end of the list, loop back to the first track
            currentTrackIndex = 0
            playTrackAtIndex(currentTrackIndex)
        }
    }

    private fun playPreviousTrack() {
        if (currentTrackIndex > 0) {
            try {
                currentTrackIndex--
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.setDataSource(musicList[currentTrackIndex].path)
                mediaPlayer.prepare()
                mediaPlayer.start()
                binding.songName.text = musicList[currentTrackIndex].title
                updateSeekBar()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // If at the first track, loop to the end of the list
            currentTrackIndex = musicList.size - 1
            playTrackAtIndex(currentTrackIndex)
        }
    }


    private fun playTrackAtIndex(index: Int) {
        try {
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(musicList[index].path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            binding.songName.text = musicList[index].title
            updateSeekBar()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun shareCurrentMusic() {
        val currentMusic = musicList[currentTrackIndex] // Get the current music from the list


        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(currentMusic.path))

        startActivity(Intent.createChooser(shareIntent, "Share Music via"))
    }

    private fun updateSeekBar() {
        seekBar.max = mediaPlayer.duration
        binding.enadTime.text = CommonMethod.formatDuration(mediaPlayer.duration)

        val runnable = object : Runnable {
            override fun run() {
                seekBar.progress = mediaPlayer.currentPosition
                binding.startTime.text = CommonMethod.formatDuration(mediaPlayer.currentPosition)
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}


