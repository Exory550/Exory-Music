package com.exory550.exorymusic.interfaces

import com.exory550.exorymusic.model.Album
import com.exory550.exorymusic.model.Artist
import com.exory550.exorymusic.model.Genre

interface IHomeClickListener {
    fun onAlbumClick(album: Album)

    fun onArtistClick(artist: Artist)

    fun onGenreClick(genre: Genre)
}
