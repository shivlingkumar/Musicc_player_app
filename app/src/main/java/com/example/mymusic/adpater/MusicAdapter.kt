package com.example.mymusic.adpater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.databinding.MusicViewBinding
import com.example.mymusic.PlayerActivity


import com.example.mymusic.db.Music
import com.example.mymusic.utils.CommonMethod
import java.io.File

class MusicAdapter(private val context: Context, private val musicList: ArrayList<Music>) :
    RecyclerView.Adapter<MusicAdapter.MyHolder>() {

    private fun removeItem(position: Int) {
        if (position >= 0 && position < musicList.size) {
            musicList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, musicList.size)
        }
    }
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
        holder.binding.songArtist.text = currentSong.artist
        // Set other properties like album, image, duration if needed

        holder.binding.musicContainer.setOnClickListener {

            when (musicList[position].id) {
                PlayerActivity.nowPlayingId ->
                    sendIntent("NowPlaying", PlayerActivity.songPosition)

                else -> sendIntent("MusicAdapter", position)
            }

        }
        holder.binding.menuIcon.setOnClickListener {
            showPopupMenu(holder.binding.menuIcon,currentSong.path)
        }

    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        context.startActivity(intent)
    }

    private fun showPopupMenu(view: View,path: String) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.pop_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.pop_menu_delete -> {
                    deleteAudioFile(path)
                    true
                }

                R.id.pop_menu_share -> {
                    // Share the PDF
                    shareMusic(path)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun deleteAudioFile(path: String) {
        val fileToDelete = File(path)

        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("Confirm Delete")
        alertDialog.setMessage("Are you sure you want to delete this audio file?")
        alertDialog.setPositiveButton("Delete") { _, _ ->
            // User confirmed, attempt to delete the file
            try {
                val deleted = fileToDelete.delete()
                if (deleted) {
                    // File deleted successfully
                    val position = musicList.indexOfFirst { it.path == path }
                    if (position != -1) {
                        removeItem(position)
                    }
                } else {
                    // Failed to delete the file, log an error message
                    Log.e("DeleteAudioFile", "Failed to delete file: $path")
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle any exceptions that might occur during file deletion
                Log.e("DeleteAudioFile", "Error deleting file: $path", e)
                Toast.makeText(context, "Error deleting file", Toast.LENGTH_SHORT).show()
            }
        }
        alertDialog.setNegativeButton("Cancel") { dialog, _ ->
            // User canceled the delete operation, dismiss the dialog
            dialog.dismiss()
        }
        alertDialog.show()
    }


    private fun shareMusic(path: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "audio/*"
        shareIntent.putExtra(
            Intent.EXTRA_STREAM,
            Uri.parse(path)
        )
        context.startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))

    }


}
