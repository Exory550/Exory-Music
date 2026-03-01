package com.exory550.exorymusic.util

import com.exory550.exorymusic.db.PlaylistWithSongs
import com.exory550.exorymusic.helper.M3UWriter.writeIO
import java.io.File
import java.io.IOException

object PlaylistsUtil {
    @Throws(IOException::class)
    fun savePlaylistWithSongs(playlist: PlaylistWithSongs?): File {
        return writeIO(
            File(getExternalStorageDirectory(), "Playlists"), playlist!!
        )
    }
}
