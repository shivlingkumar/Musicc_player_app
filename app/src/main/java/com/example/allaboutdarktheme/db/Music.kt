package com.example.allaboutdarktheme.db

import com.example.allaboutdarktheme.Player_Activity

data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val path: String,
    val duration: Long = 0,
)
