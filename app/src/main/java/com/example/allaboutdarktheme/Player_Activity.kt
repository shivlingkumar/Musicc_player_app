package com.example.allaboutdarktheme

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import com.example.allaboutdarktheme.databinding.ActivityPlayerBinding
import java.io.IOException

class Player_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val songPath = intent.getStringExtra("songPath")
        val songName = intent.getStringExtra("songName")
        binding.songName.text = songName

        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer.setDataSource(songPath)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
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
        val finalDuration = formatDuration(mediaPlayer.duration)
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

        }

    }

    private fun updateSeekBar() {
        seekBar.max = mediaPlayer.duration

        val runnable = object : Runnable {
            override fun run() {
                seekBar.progress = mediaPlayer.currentPosition
                binding.startTime.text = formatDuration(mediaPlayer.currentPosition)
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
private fun formatDuration(duration: Int): String {
    val minutes = duration / 1000 / 60
    val seconds = duration / 1000 % 60
    return String.format("%02d:%02d", minutes, seconds)
}