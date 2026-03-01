package com.exory550.exorymusic.interfaces

interface IMusicServiceEventListener {
    fun onServiceConnected()
    fun onServiceDisconnected()
    fun onQueueChanged()
    fun onFavoriteStateChanged()
    fun onPlayingMetaChanged()
    fun onPlayStateChanged()
    fun onRepeatModeChanged()
    fun onShuffleModeChanged()
    fun onMediaStoreChanged()
}
