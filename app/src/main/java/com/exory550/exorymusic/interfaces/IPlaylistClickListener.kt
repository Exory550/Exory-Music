package com.exory550.exorymusic.interfaces

import android.view.View
import com.exory550.exorymusic.db.PlaylistWithSongs

interface IPlaylistClickListener {
    fun onPlaylistClick(playlistWithSongs: PlaylistWithSongs, view: View)
}
