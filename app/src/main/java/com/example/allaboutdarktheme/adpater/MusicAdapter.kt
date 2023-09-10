package com.example.allaboutdarktheme.adpater

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.allaboutdarktheme.Player_Activity
import com.example.allaboutdarktheme.databinding.MusicViewBinding
import com.example.allaboutdarktheme.db.Music
import com.example.allaboutdarktheme.utils.CommonMethod

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
        holder.binding.songName.text = currentSong.title
        holder.binding.songDuration.text = CommonMethod.formatDuration(currentSong.duration.toInt())
        holder.binding.songArtist.text = currentSong.artist.toString()
        // Set other properties like album, image, duration if needed

        holder.binding.musicContainer.setOnClickListener {
            val intent = Intent(context,Player_Activity::class.java)
            intent.putExtra("songPath",currentSong.path)
            intent.putExtra("songName",currentSong.title)
            intent.putExtra("currentPosition",position)
            context.startActivity(intent)
        }


    }
}
