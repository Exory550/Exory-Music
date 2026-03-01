package com.exory550.exorymusic.service

import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import com.exory550.exorymusic.auto.AutoMediaIDHelper
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.helper.MusicPlayerRemote.cycleRepeatMode
import com.exory550.exorymusic.helper.ShuffleHelper.makeShuffleList
import com.exory550.exorymusic.model.Album
import com.exory550.exorymusic.model.Artist
import com.exory550.exorymusic.model.Playlist
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.repository.*
import com.exory550.exorymusic.service.MusicService.Companion.CYCLE_REPEAT
import com.exory550.exorymusic.service.MusicService.Companion.TOGGLE_FAVORITE
import com.exory550.exorymusic.service.MusicService.Companion.TOGGLE_SHUFFLE
import com.exory550.exorymusic.util.MusicUtil
import com.exory550.exorymusic.util.logD
import com.exory550.exorymusic.util.logE
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MediaSessionCallback(
    private val musicService: MusicService,
) : MediaSessionCompat.Callback(), KoinComponent {

    private val songRepository by inject<SongRepository>()
    private val albumRepository by inject<AlbumRepository>()
    private val artistRepository by inject<ArtistRepository>()
    private val genreRepository by inject<GenreRepository>()
    private val playlistRepository by inject<PlaylistRepository>()
    private val topPlayedRepository by inject<TopPlayedRepository>()

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        val musicId = AutoMediaIDHelper.extractMusicID(mediaId!!)
        logD("Music Id $musicId")
        val itemId = musicId?.toLong() ?: -1
        val songs: ArrayList<Song> = ArrayList()
        when (val category = AutoMediaIDHelper.extractCategory(mediaId)) {
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM -> {
                val album: Album = albumRepository.album(itemId)
                songs.addAll(album.songs)
                musicService.openQueue(songs, 0, true)
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST -> {
                val artist: Artist = artistRepository.artist(itemId)
                songs.addAll(artist.songs)
                musicService.openQueue(songs, 0, true)
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM_ARTIST -> {
                val artist: Artist =
                    artistRepository.albumArtist(albumRepository.album(itemId).albumArtist!!)
                songs.addAll(artist.songs)
                musicService.openQueue(songs, 0, true)
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST -> {
                val playlist: Playlist = playlistRepository.playlist(itemId)
                songs.addAll(playlist.getSongs())
                musicService.openQueue(songs, 0, true)
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE -> {
                songs.addAll(genreRepository.songs(itemId))
                musicService.openQueue(songs, 0, true)
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE -> {
                val allSongs: ArrayList<Song> = songRepository.songs() as ArrayList<Song>
                makeShuffleList(allSongs, -1)
                musicService.openQueue(allSongs, 0, true)
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY,
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SUGGESTIONS,
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS,
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE,
            -> {
                val tracks: List<Song> = when (category) {
                    AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY -> topPlayedRepository.recentlyPlayedTracks()
                    AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SUGGESTIONS -> topPlayedRepository.recentlyPlayedTracks()
                    AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS -> topPlayedRepository.recentlyPlayedTracks()
                    else -> musicService.playingQueue
                }
                songs.addAll(tracks)
                var songIndex = MusicUtil.indexOfSongInList(tracks, itemId)
                if (songIndex == -1) {
                    songIndex = 0
                }
                musicService.openQueue(songs, songIndex, true)
            }
        }
        musicService.play()
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        val songs = ArrayList<Song>()
        if (query.isNullOrEmpty()) {
            songs.addAll(songRepository.songs())
        } else {
            val mediaFocus: String? = extras?.getString(MediaStore.EXTRA_MEDIA_FOCUS)
            if (mediaFocus == MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE) {
                val artistQuery = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)
                if (artistQuery != null) {
                    artistRepository.artists(artistQuery).forEach {
                        songs.addAll(it.songs)
                    }
                }
            } else if (mediaFocus == MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE) {
                val albumQuery = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM)
                if (albumQuery != null) {
                    albumRepository.albums(albumQuery).forEach {
                        songs.addAll(it.songs)
                    }
                }
            }
        }

        if (songs.isEmpty()) {
            query?.also {
                songs.addAll(songRepository.songs(it))
            }
        }

        musicService.openQueue(songs, 0, true)

        musicService.play()
    }

    override fun onPrepare() {
        super.onPrepare()
        if (musicService.currentSong != Song.emptySong)
            musicService.restoreState(::onPlay)
    }

    override fun onPlay() {
        super.onPlay()
        if (musicService.currentSong != Song.emptySong) musicService.play()
    }

    override fun onPause() {
        super.onPause()
        musicService.pause()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        musicService.playNextSong(true)
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        musicService.playPreviousSong(true)
    }

    override fun onStop() {
        super.onStop()
        musicService.quit()
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        musicService.seek(pos.toInt())
    }

    override fun onCustomAction(action: String, extras: Bundle?) {
        when (action) {
            CYCLE_REPEAT -> {
                cycleRepeatMode()
                musicService.updateMediaSessionPlaybackState()
            }

            TOGGLE_SHUFFLE -> {
                musicService.toggleShuffle()
                musicService.updateMediaSessionPlaybackState()
            }
            TOGGLE_FAVORITE -> {
                musicService.toggleFavorite()
            }
            else -> {
                logE("Unsupported action: $action")
            }
        }
    }

    private fun checkAndStartPlaying(songs: ArrayList<Song>, itemId: Long) {
        var songIndex = MusicUtil.indexOfSongInList(songs, itemId)
        if (songIndex == -1) {
            songIndex = 0
        }
        openQueue(songs, songIndex)
    }

    private fun openQueue(songs: ArrayList<Song>, index: Int, startPlaying: Boolean = true) {
        MusicPlayerRemote.openQueue(songs, index, startPlaying)
    }
}
