package com.example.allaboutdarktheme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.allaboutdarktheme.databinding.ActivityPlayerBinding
import com.example.allaboutdarktheme.databinding.ActivityPlaylistBinding

class Playlist_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.playlistPageBackBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

    }
}