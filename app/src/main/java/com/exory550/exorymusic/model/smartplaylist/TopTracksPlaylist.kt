package com.exory550.exorymusic.model.smartplaylist

import com.exory550.exorymusic.App
import com.exory550.exorymusic.R
import com.exory550.exorymusic.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class TopTracksPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.my_top_tracks),
    iconRes = R.drawable.ic_trending_up
) {
    override fun songs(): List<Song> {
        return topPlayedRepository.topTracks()
    }
}
