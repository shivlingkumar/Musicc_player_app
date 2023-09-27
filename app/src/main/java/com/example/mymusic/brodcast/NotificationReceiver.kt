package com.example.mymusic.brodcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mymusic.R
import com.example.mymusic.ApplicationClass
import com.example.mymusic.PlayerActivity
import com.example.mymusic.db.exitApplication
import com.example.mymusic.db.requestAudioFocusMy
import com.example.mymusic.db.setSongPosition
import com.example.mymusic.fragment.NowPlayingFragment


class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        when(p1?.action){
            //only play next or prev song, when music list contains more than one song
            ApplicationClass.PREVIOUS -> if(PlayerActivity.musicListPA.size > 1) prevNextSong(increment = false, p0!!)
            ApplicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic() else playMusic(p0!!)
            ApplicationClass.NEXT -> if(PlayerActivity.musicListPA.size > 1) prevNextSong(increment = true, p0!!)
            ApplicationClass.EXIT ->{
                exitApplication()
            }
        }
    }
    private fun playMusic(context: Context){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.baseline_pause_24)
        PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_pause_24)
        try{ NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24) }catch (_: Exception){}
        if (PlayerActivity.musicService!!.isFocusLoass) {
            requestAudioFocusMy(context)
        }
    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
        PlayerActivity.binding.playSongBtn.setIconResource(R.drawable.baseline_play_arrow_24)
        try{ NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24) }catch (_: Exception){}
    }

    private fun prevNextSong(increment: Boolean,context: Context){

        setSongPosition(increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        PlayerActivity.binding.songName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        NowPlayingFragment.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic(context)

    }
}