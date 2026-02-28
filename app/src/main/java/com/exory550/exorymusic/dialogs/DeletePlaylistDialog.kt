package com.exory550.exorymusic.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.fragment.app.DialogFragment
import com.exory550.exorymusic.EXTRA_PLAYLIST
import com.exory550.exorymusic.R
import com.exory550.exorymusic.db.PlaylistEntity
import com.exory550.exorymusic.extensions.colorButtons
import com.exory550.exorymusic.extensions.extraNotNull
import com.exory550.exorymusic.extensions.materialDialog
import com.exory550.exorymusic.fragments.LibraryViewModel
import com.exory550.exorymusic.fragments.ReloadType
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DeletePlaylistDialog : DialogFragment() {

    private val libraryViewModel by activityViewModel<LibraryViewModel>()

    companion object {

        fun create(playlist: PlaylistEntity): DeletePlaylistDialog {
            val list = mutableListOf<PlaylistEntity>()
            list.add(playlist)
            return create(list)
        }

        fun create(playlists: List<PlaylistEntity>): DeletePlaylistDialog {
            return DeletePlaylistDialog().apply {
                arguments = bundleOf(EXTRA_PLAYLIST to playlists)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlists = extraNotNull<List<PlaylistEntity>>(EXTRA_PLAYLIST).value
        val title: Int
        val message: CharSequence
        if (playlists.size > 1) {
            title = R.string.delete_playlists_title
            message =
                String.format(getString(R.string.delete_x_playlists), playlists.size).parseAsHtml()
        } else {
            title = R.string.delete_playlist_title
            message =
                String.format(getString(R.string.delete_playlist_x), playlists[0].playlistName)
                    .parseAsHtml()
        }

        return materialDialog(title)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                libraryViewModel.deleteSongsFromPlaylist(playlists)
                libraryViewModel.deleteRoomPlaylist(playlists)
                libraryViewModel.forceReload(ReloadType.Playlists)
            }
            .create()
            .colorButtons()
    }
}
