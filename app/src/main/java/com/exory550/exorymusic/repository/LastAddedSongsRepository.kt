package com.exory550.exorymusic.repository

import android.database.Cursor
import android.provider.MediaStore
import com.exory550.exorymusic.model.Album
import com.exory550.exorymusic.model.Artist
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.util.PreferenceUtil

interface LastAddedRepository {
    fun recentSongs(): List<Song>
    fun recentAlbums(): List<Album>
    fun recentArtists(): List<Artist>
}

class RealLastAddedRepository(
    private val songRepository: RealSongRepository,
    private val albumRepository: RealAlbumRepository,
    private val artistRepository: RealArtistRepository
) : LastAddedRepository {
    override fun recentSongs(): List<Song> {
        return songRepository.songs(makeLastAddedCursor())
    }

    override fun recentAlbums(): List<Album> {
        return albumRepository.splitIntoAlbums(recentSongs(), sorted = false)
    }

    override fun recentArtists(): List<Artist> {
        return artistRepository.splitIntoArtists(recentAlbums())
    }

    private fun makeLastAddedCursor(): Cursor? {
        val cutoff = PreferenceUtil.lastAddedCutoff
        return songRepository.makeSongCursor(
            MediaStore.Audio.Media.DATE_ADDED + ">?",
            arrayOf(cutoff.toString()),
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
    }
}
