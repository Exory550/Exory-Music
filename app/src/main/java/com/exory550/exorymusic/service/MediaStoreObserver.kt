package com.exory550.exorymusic.service

import android.database.ContentObserver
import android.os.Handler

class MediaStoreObserver(
    private val musicService: MusicService,
    private val mHandler: Handler
) : ContentObserver(mHandler), Runnable {

    override fun onChange(selfChange: Boolean) {
        mHandler.removeCallbacks(this)
        mHandler.postDelayed(this, REFRESH_DELAY)
    }

    override fun run() {
        musicService.handleAndSendChangeInternal(MusicService.MEDIA_STORE_CHANGED)
    }

    companion object {
        private const val REFRESH_DELAY: Long = 500
    }
}
