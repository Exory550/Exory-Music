package com.exory550.exorymusic.fragments.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.exory550.exorymusic.db.PlaylistWithSongs
import com.exory550.exorymusic.db.SongEntity
import com.exory550.exorymusic.repository.RealRepository

class PlaylistDetailsViewModel(
    private val realRepository: RealRepository,
    private var playlistId: Long
) : ViewModel() {
    fun getSongs(): LiveData<List<SongEntity>> =
        realRepository.playlistSongs(playlistId)

    fun playlistExists(): LiveData<Boolean> =
        realRepository.checkPlaylistExists(playlistId)

    fun getPlaylist(): LiveData<PlaylistWithSongs> = realRepository.getPlaylist(playlistId)
}
