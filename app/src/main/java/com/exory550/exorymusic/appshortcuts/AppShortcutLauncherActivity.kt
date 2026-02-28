package com.exory550.exorymusic.appshortcuts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import com.exory550.exorymusic.appshortcuts.shortcuttype.LastAddedShortcutType
import com.exory550.exorymusic.appshortcuts.shortcuttype.ShuffleAllShortcutType
import com.exory550.exorymusic.appshortcuts.shortcuttype.TopTracksShortcutType
import com.exory550.exorymusic.extensions.extraNotNull
import com.exory550.exorymusic.model.Playlist
import com.exory550.exorymusic.model.smartplaylist.LastAddedPlaylist
import com.exory550.exorymusic.model.smartplaylist.ShuffleAllPlaylist
import com.exory550.exorymusic.model.smartplaylist.TopTracksPlaylist
import com.exory550.exorymusic.service.MusicService
import com.exory550.exorymusic.service.MusicService.Companion.ACTION_PLAY_PLAYLIST
import com.exory550.exorymusic.service.MusicService.Companion.INTENT_EXTRA_PLAYLIST
import com.exory550.exorymusic.service.MusicService.Companion.INTENT_EXTRA_SHUFFLE_MODE
import com.exory550.exorymusic.service.MusicService.Companion.SHUFFLE_MODE_NONE
import com.exory550.exorymusic.service.MusicService.Companion.SHUFFLE_MODE_SHUFFLE

class AppShortcutLauncherActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (extraNotNull(KEY_SHORTCUT_TYPE, SHORTCUT_TYPE_NONE).value) {
            SHORTCUT_TYPE_SHUFFLE_ALL -> {
                startServiceWithPlaylist(
                    SHUFFLE_MODE_SHUFFLE, ShuffleAllPlaylist()
                )
                DynamicShortcutManager.reportShortcutUsed(this, ShuffleAllShortcutType.id)
            }
            SHORTCUT_TYPE_TOP_TRACKS -> {
                startServiceWithPlaylist(
                    SHUFFLE_MODE_NONE, TopTracksPlaylist()
                )
                DynamicShortcutManager.reportShortcutUsed(this, TopTracksShortcutType.id)
            }
            SHORTCUT_TYPE_LAST_ADDED -> {
                startServiceWithPlaylist(
                    SHUFFLE_MODE_NONE, LastAddedPlaylist()
                )
                DynamicShortcutManager.reportShortcutUsed(this, LastAddedShortcutType.id)
            }
        }
        finish()
    }

    private fun startServiceWithPlaylist(shuffleMode: Int, playlist: Playlist) {
        val intent = Intent(this, MusicService::class.java)
        intent.action = ACTION_PLAY_PLAYLIST

        val bundle = bundleOf(
            INTENT_EXTRA_PLAYLIST to playlist,
            INTENT_EXTRA_SHUFFLE_MODE to shuffleMode
        )

        intent.putExtras(bundle)

        startService(intent)
    }

    companion object {
        const val KEY_SHORTCUT_TYPE = "io.github.exory550.ExoryMusic.appshortcuts.ShortcutType"
        const val SHORTCUT_TYPE_SHUFFLE_ALL = 0L
        const val SHORTCUT_TYPE_TOP_TRACKS = 1L
        const val SHORTCUT_TYPE_LAST_ADDED = 2L
        const val SHORTCUT_TYPE_NONE = 4L
    }
}
