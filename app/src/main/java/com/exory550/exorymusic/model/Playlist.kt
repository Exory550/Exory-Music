package com.exory550.exorymusic.model

import android.content.Context
import android.os.Parcelable
import com.exory550.exorymusic.repository.RealPlaylistRepository
import com.exory550.exorymusic.util.MusicUtil
import kotlinx.parcelize.Parcelize
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Parcelize
open class Playlist(
    val id: Long,
    val name: String
) : Parcelable, KoinComponent {

    companion object {
        val empty = Playlist(-1, "")
    }

    fun getSongs(): List<Song> {
        return RealPlaylistRepository(get()).playlistSongs(id)
    }

    open fun getInfoString(context: Context): String {
        val songCount = getSongs().size
        val songCountString = MusicUtil.getSongCountString(context, songCount)
        return MusicUtil.buildInfoString(
            songCountString,
            ""
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Playlist

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
