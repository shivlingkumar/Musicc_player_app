package com.example.mymusic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import com.example.mymusic.db.Music
import java.io.File

class MusicLoader {

    companion object  {
        @SuppressLint("Recycle", "Range")
        fun getAllAudio(context: Context): ArrayList<Music> {
            val tempList = ArrayList<Music>()
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATA
            )
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC",
                null
            )

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val title =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                        val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                        val album =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                        val artist =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                        val duration =
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                        val music = Music(id, title, album, artist, path, duration)
                        val file = File(music.path)

                        if (file.exists()) {
                            tempList.add(music)
                        }
                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return tempList
        }
    }
}