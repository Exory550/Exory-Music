package com.exory550.exorymusic.service

import android.os.Handler
import com.exory550.exorymusic.service.MusicService.Companion.PLAY_STATE_CHANGED

class ThrottledSeekHandler(
    private val musicService: MusicService,
    private val handler: Handler
) : Runnable {

    fun notifySeek() {
        musicService.updateMediaSessionPlaybackState()
        handler.removeCallbacks(this)
        handler.postDelayed(this, THROTTLE)
    }

    override fun run() {
        musicService.savePositionInTrack()
        musicService.sendPublicIntent(PLAY_STATE_CHANGED)
    }

    companion object {
        private const val THROTTLE: Long = 500
    }
}
