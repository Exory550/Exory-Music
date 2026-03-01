package com.exory550.exorymusic.model.smartplaylist

import com.exory550.exorymusic.App
import com.exory550.exorymusic.R
import com.exory550.exorymusic.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class ShuffleAllPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.action_shuffle_all),
    iconRes = R.drawable.ic_shuffle
) {
    override fun songs(): List<Song> {
        return songRepository.songs()
    }
}
