package com.exory550.exorymusic.repository

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import com.exory550.exorymusic.Constants.NUMBER_OF_TOP_TRACKS
import com.exory550.exorymusic.model.Album
import com.exory550.exorymusic.model.Artist
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.providers.HistoryStore
import com.exory550.exorymusic.providers.SongPlayCountStore
import com.exory550.exorymusic.util.PreferenceUtil

interface TopPlayedRepository {
    fun recentlyPlayedTracks(): List<Song>
    fun topTracks(): List<Song>
    fun notRecentlyPlayedTracks(): List<Song>
    fun topAlbums(): List<Album>
    fun topArtists(): List<Artist>
}

class RealTopPlayedRepository(
    private val context: Context,
    private val songRepository: RealSongRepository,
    private val albumRepository: RealAlbumRepository,
    private val artistRepository: RealArtistRepository
) : TopPlayedRepository {

    override fun recentlyPlayedTracks(): List<Song> {
        return songRepository.songs(makeRecentTracksCursorAndClearUpDatabase())
    }

    override fun topTracks(): List<Song> {
        return songRepository.songs(makeTopTracksCursorAndClearUpDatabase())
    }

    override fun notRecentlyPlayedTracks(): List<Song> {
        val allSongs = mutableListOf<Song>().apply {
            addAll(
                songRepository.songs(
                    songRepository.makeSongCursor(
                        null, null,
                        MediaStore.Audio.Media.DATE_ADDED + " ASC"
                    )
                )
            )
        }
        val playedSongs = songRepository.songs(
            makePlayedTracksCursorAndClearUpDatabase()
        )
        val notRecentlyPlayedSongs = songRepository.songs(
            makeNotRecentTracksCursorAndClearUpDatabase()
        )
        allSongs.removeAll(playedSongs.toSet())
        allSongs.addAll(notRecentlyPlayedSongs)
        return allSongs
    }

    override fun topAlbums(): List<Album> {
        return albumRepository.splitIntoAlbums(topTracks(), sorted = false)
    }

    override fun topArtists(): List<Artist> {
        return artistRepository.splitIntoArtists(topAlbums())
    }

    private fun makeTopTracksCursorAndClearUpDatabase(): Cursor? {
        val retCursor = makeTopTracksCursorImpl()
        if (retCursor != null) {
            val missingIds = retCursor.missingIds
            if (missingIds != null && missingIds.size > 0) {
                for (id in missingIds) {
                    SongPlayCountStore.getInstance(context).removeItem(id)
                }
            }
        }
        return retCursor
    }

    private fun makeRecentTracksCursorImpl(): SortedLongCursor? {
        val songs = HistoryStore.getInstance(context).queryRecentIds()
        songs.use {
            return makeSortedCursor(
                it,
                it.getColumnIndex(HistoryStore.RecentStoreColumns.ID)
            )
        }
    }

    private fun makeTopTracksCursorImpl(): SortedLongCursor? {
        val cursor =
            SongPlayCountStore.getInstance(context).getTopPlayedResults(NUMBER_OF_TOP_TRACKS)

        cursor.use { songs ->
            return makeSortedCursor(
                songs,
                songs.getColumnIndex(SongPlayCountStore.SongPlayCountColumns.ID)
            )
        }
    }

    private fun makeSortedCursor(
        cursor: Cursor?, idColumn: Int
    ): SortedLongCursor? {

        if (cursor != null && cursor.moveToFirst()) {
            val selection = StringBuilder()
            selection.append(BaseColumns._ID)
            selection.append(" IN (")

            val order = LongArray(cursor.count)

            var id = cursor.getLong(idColumn)
            selection.append(id)
            order[cursor.position] = id

            while (cursor.moveToNext()) {
                selection.append(",")

                id = cursor.getLong(idColumn)
                order[cursor.position] = id
                selection.append(id.toString())
            }

            selection.append(")")

            val songCursor = songRepository.makeSongCursor(selection.toString(), null)
            if (songCursor != null) {
                return SortedLongCursor(
                    songCursor,
                    order,
                    BaseColumns._ID
                )
            }
        }

        return null
    }

    private fun makeRecentTracksCursorAndClearUpDatabase(): Cursor? {
        return makeRecentTracksCursorAndClearUpDatabaseImpl(
            ignoreCutoffTime = false,
            reverseOrder = false
        )
    }

    private fun makePlayedTracksCursorAndClearUpDatabase(): Cursor? {
        return makeRecentTracksCursorAndClearUpDatabaseImpl(
            ignoreCutoffTime = true,
            reverseOrder = false
        )
    }

    private fun makeNotRecentTracksCursorAndClearUpDatabase(): Cursor? {
        return makeRecentTracksCursorAndClearUpDatabaseImpl(
            ignoreCutoffTime = false,
            reverseOrder = true
        )
    }

    private fun makeRecentTracksCursorAndClearUpDatabaseImpl(
        ignoreCutoffTime: Boolean,
        reverseOrder: Boolean
    ): SortedLongCursor? {
        val retCursor = makeRecentTracksCursorImpl(ignoreCutoffTime, reverseOrder)
        if (retCursor != null) {
            val missingIds = retCursor.missingIds
            if (missingIds != null && missingIds.size > 0) {
                for (id in missingIds) {
                    HistoryStore.getInstance(context).removeSongId(id)
                }
            }
        }
        return retCursor
    }

    private fun makeRecentTracksCursorImpl(
        ignoreCutoffTime: Boolean,
        reverseOrder: Boolean
    ): SortedLongCursor? {
        val cutoff =
            (if (ignoreCutoffTime) 0 else PreferenceUtil.getRecentlyPlayedCutoffTimeMillis()).toLong()
        val songs =
            HistoryStore.getInstance(context).queryRecentIds(cutoff * if (reverseOrder) -1 else 1)
        return songs.use {
            makeSortedCursor(
                it,
                it.getColumnIndex(HistoryStore.RecentStoreColumns.ID)
            )
        }
    }
}
