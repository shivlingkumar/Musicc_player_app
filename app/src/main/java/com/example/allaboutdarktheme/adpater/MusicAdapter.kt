package com.example.allaboutdarktheme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.allaboutdarktheme.databinding.MusicViewBinding
import com.example.allaboutdarktheme.db.Music

class MusicAdapter(private val context: Context, private val musicList: ArrayList<Music>) : RecyclerView.Adapter<MusicAdapter.MyHolder>() {

    class MyHolder(val binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = MusicViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyHolder(binding)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val currentSong = musicList[position]
        holder.binding.songname.text = currentSong.toString()
        // Set other properties like album, image, duration if needed
    }
}
