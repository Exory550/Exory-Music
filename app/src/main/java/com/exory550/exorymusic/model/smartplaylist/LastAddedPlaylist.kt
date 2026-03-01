package com.exory550.exorymusic.model.smartplaylist

import com.exory550.exorymusic.App
import com.exory550.exorymusic.R
import com.exory550.exorymusic.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class LastAddedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.last_added),
    iconRes = R.drawable.ic_library_add
) {
    override fun songs(): List<Song> {
        return lastAddedRepository.recentSongs()
    }
}
