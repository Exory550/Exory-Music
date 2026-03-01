package com.exory550.exorymusic.helper

import android.provider.MediaStore
import com.exory550.exorymusic.ALBUM_ARTIST

class SortOrder {

    interface ArtistSortOrder {

        companion object {

            const val ARTIST_A_Z = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER

            const val ARTIST_Z_A = "$ARTIST_A_Z DESC"

            const val ARTIST_NUMBER_OF_SONGS = MediaStore.Audio.Artists.NUMBER_OF_TRACKS + " DESC"

            const val ARTIST_NUMBER_OF_ALBUMS = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS + " DESC"
        }
    }

    interface AlbumSortOrder {

        companion object {

            const val ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER

            const val ALBUM_Z_A = "$ALBUM_A_Z DESC"

            const val ALBUM_NUMBER_OF_SONGS =
                MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS + " DESC"

            const val ALBUM_ARTIST = "case when lower(album_artist) is null then 1 else 0 end, lower(album_artist)"

            const val ALBUM_YEAR = MediaStore.Audio.Media.YEAR + " DESC"
        }
    }

    interface SongSortOrder {

        companion object {

            const val SONG_DEFAULT = MediaStore.Audio.Media.DEFAULT_SORT_ORDER

            const val SONG_A_Z = MediaStore.Audio.Media.TITLE

            const val SONG_Z_A = "$SONG_A_Z DESC"

            const val SONG_ARTIST = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER

            const val SONG_ALBUM_ARTIST = ALBUM_ARTIST

            const val SONG_ALBUM = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER

            const val SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC"

            const val SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC"

            const val SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC"

            const val SONG_DATE_MODIFIED = MediaStore.Audio.Media.DATE_MODIFIED + " DESC"

            const val COMPOSER = MediaStore.Audio.Media.COMPOSER
        }
    }

    interface AlbumSongSortOrder {

        companion object {

            const val SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER

            const val SONG_Z_A = "$SONG_A_Z DESC"

            const val SONG_TRACK_LIST = (MediaStore.Audio.Media.TRACK + ", " +
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER)

            const val SONG_DURATION = SongSortOrder.SONG_DURATION
        }
    }

    interface ArtistSongSortOrder {

        companion object {

            const val SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER

            const val SONG_Z_A = "$SONG_A_Z DESC"

            const val SONG_ALBUM = MediaStore.Audio.Media.ALBUM

            const val SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC"

            const val SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC"

            const val SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC"
        }
    }

    interface ArtistAlbumSortOrder {

        companion object {

            const val ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER

            const val ALBUM_Z_A = "$ALBUM_A_Z DESC"

            const val ALBUM_YEAR = MediaStore.Audio.Media.YEAR + " DESC"

            const val ALBUM_YEAR_ASC = MediaStore.Audio.Media.YEAR + " ASC"
        }
    }

    interface GenreSortOrder {

        companion object {

            const val GENRE_A_Z = MediaStore.Audio.Genres.DEFAULT_SORT_ORDER

            const val ALBUM_Z_A = "$GENRE_A_Z DESC"
        }
    }

    interface PlaylistSortOrder {

        companion object {

            const val PLAYLIST_A_Z = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER

            const val PLAYLIST_Z_A = "$PLAYLIST_A_Z DESC"

            const val PLAYLIST_SONG_COUNT = "playlist_song_count"

            const val PLAYLIST_SONG_COUNT_DESC = "$PLAYLIST_SONG_COUNT DESC"
        }
    }
}
