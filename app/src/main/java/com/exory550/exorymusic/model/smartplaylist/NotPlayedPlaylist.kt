package com.exory550.exorymusic.model.smartplaylist

import com.exory550.exorymusic.App
import com.exory550.exorymusic.R
import com.exory550.exorymusic.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class NotPlayedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.not_recently_played),
    iconRes = R.drawable.ic_audiotrack
) {
    override fun songs(): List<Song> {
        return topPlayedRepository.notRecentlyPlayedTracks()
    }
}
