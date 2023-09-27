package com.example.mymusic.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mymusic.R
import com.example.mymusic.databinding.FragmentNowPlayingBinding
import com.example.mymusic.PlayerActivity
import com.example.mymusic.PlayerActivity.Companion.musicService
import com.example.mymusic.db.requestAudioFocusMy
import com.example.mymusic.db.setSongPosition

class NowPlayingFragment : Fragment() {


    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.GONE
        binding.songNameNP.isSelected
        binding.playPauseBtnNP.setOnClickListener {
            if (PlayerActivity.isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        binding.nextBtnNP.setOnClickListener {
            setSongPosition(true)
            musicService!!.createMediaPlayer()
            PlayerActivity.binding.songName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            musicService!!.showNotification(R.drawable.baseline_pause_24)
            playMusic()
        }
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(),PlayerActivity::class.java)
            intent.putExtra("index",PlayerActivity.songPosition)
            intent.putExtra("class","NowPlaying")
            requireContext().startActivity(intent)

        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (musicService!=null){
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            binding.songNameNP.isSelected
            if (PlayerActivity.isPlaying) binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24)
            else binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
        }
    }

    private fun playMusic(){
        if (musicService!!.isFocusLoass){
        requestAudioFocusMy(requireContext())}
        musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24)
        musicService!!.showNotification(R.drawable.baseline_pause_24)
        PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_pause_24)
        PlayerActivity.isPlaying = true

    }
    private fun pauseMusic(){
        musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
        musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
        PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        PlayerActivity.isPlaying = false
    }

}