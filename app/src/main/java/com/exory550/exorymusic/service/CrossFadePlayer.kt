package com.exory550.exorymusic.service

import android.animation.Animator
import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.PowerManager
import com.exory550.appthemehelper.util.VersionUtils.hasMarshmallow
import com.exory550.exorymusic.R
import com.exory550.exorymusic.extensions.showToast
import com.exory550.exorymusic.extensions.uri
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.service.AudioFader.Companion.createFadeAnimator
import com.exory550.exorymusic.service.playback.Playback.PlaybackCallbacks
import com.exory550.exorymusic.util.PreferenceUtil
import com.exory550.exorymusic.util.logE
import kotlinx.coroutines.*

class CrossFadePlayer(context: Context) : LocalPlayback(context) {

    private var currentPlayer: CurrentPlayer = CurrentPlayer.NOT_SET
    private var player1 = MediaPlayer()
    private var player2 = MediaPlayer()
    private var durationListener = DurationListener()
    private var mIsInitialized = false
    private var hasDataSource: Boolean = false
    private var nextDataSource:String? = null
    private var crossFadeAnimator: Animator? = null
    override var callbacks: PlaybackCallbacks? = null
    private var crossFadeDuration = PreferenceUtil.crossFadeDuration
    var isCrossFading = false

    init {
        player1.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        player2.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        currentPlayer = CurrentPlayer.PLAYER_ONE
    }

    override fun start(): Boolean {
        super.start()
        durationListener.start()
        resumeFade()
        return try {
            getCurrentPlayer()?.start()
            if (isCrossFading) {
                getNextPlayer()?.start()
            }
            true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            false
        }
    }

    override fun release() {
        stop()
        cancelFade()
        getCurrentPlayer()?.release()
        getNextPlayer()?.release()
        durationListener.cancel()
    }

    override fun stop() {
        super.stop()
        getCurrentPlayer()?.reset()
        mIsInitialized = false
    }

    override fun pause(): Boolean {
        super.pause()
        durationListener.stop()
        pauseFade()
        getCurrentPlayer()?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        getNextPlayer()?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        return true
    }

    override fun seek(whereto: Int, force: Boolean): Int {
        if (force) {
            endFade()
        }
        getNextPlayer()?.stop()
        return try {
            getCurrentPlayer()?.seekTo(whereto)
            whereto
        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
            -1
        }
    }

    override fun setVolume(vol: Float): Boolean {
        cancelFade()
        return try {
            getCurrentPlayer()?.setVolume(vol, vol)
            true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            false
        }
    }

    override val isInitialized: Boolean
        get() = mIsInitialized

    override val isPlaying: Boolean
        get() = mIsInitialized && getCurrentPlayer()?.isPlaying == true

    override fun setDataSource(
        song: Song,
        force: Boolean,
        completion: (success: Boolean) -> Unit,
    ) {
        if (force) hasDataSource = false
        mIsInitialized = false
        if (!hasDataSource) {
            getCurrentPlayer()?.let {
                setDataSourceImpl(it, song.uri.toString()) { success ->
                    mIsInitialized = success
                    completion(success)
                }
            }
            hasDataSource = true
        } else {
            completion(true)
            mIsInitialized = true
        }
    }

    override fun setNextDataSource(path: String?) {
        nextDataSource = path
    }

    override fun setAudioSessionId(sessionId: Int): Boolean {
        return try {
            getCurrentPlayer()?.audioSessionId = sessionId
            true
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            false
        }
    }

    override val audioSessionId: Int
        get() = getCurrentPlayer()?.audioSessionId!!

    override fun duration(): Int {
        return if (!mIsInitialized) {
            -1
        } else try {
            getCurrentPlayer()?.duration!!
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            -1
        }
    }

    override fun position(): Int {
        return if (!mIsInitialized) {
            -1
        } else try {
            getCurrentPlayer()?.currentPosition!!
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            -1
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (mp == getCurrentPlayer()) {
            callbacks?.onTrackEnded()
        }
    }

    private fun getCurrentPlayer(): MediaPlayer? {
        return when (currentPlayer) {
            CurrentPlayer.PLAYER_ONE -> {
                player1
            }
            CurrentPlayer.PLAYER_TWO -> {
                player2
            }
            CurrentPlayer.NOT_SET -> {
                null
            }
        }
    }

    private fun getNextPlayer(): MediaPlayer? {
        return when (currentPlayer) {
            CurrentPlayer.PLAYER_ONE -> {
                player2
            }
            CurrentPlayer.PLAYER_TWO -> {
                player1
            }
            CurrentPlayer.NOT_SET -> {
                null
            }
        }
    }

    private fun crossFade(fadeInMp: MediaPlayer, fadeOutMp: MediaPlayer) {
        isCrossFading = true
        crossFadeAnimator = createFadeAnimator(context, fadeInMp, fadeOutMp) {
            crossFadeAnimator = null
            durationListener.start()
            isCrossFading = false
        }
        crossFadeAnimator?.start()
    }

    private fun endFade() {
        crossFadeAnimator?.end()
        crossFadeAnimator = null
    }

    private fun cancelFade() {
        crossFadeAnimator?.cancel()
        crossFadeAnimator = null
    }

    private fun pauseFade() {
        crossFadeAnimator?.pause()
    }

    private fun resumeFade() {
        if (crossFadeAnimator?.isPaused == true) {
            crossFadeAnimator?.resume()
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mIsInitialized = false
        mp?.release()
        player1 = MediaPlayer()
        player2 = MediaPlayer()
        mIsInitialized = true
        mp?.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        context.showToast(R.string.unplayable_file)
        logE(what.toString() + extra)
        return false
    }

    enum class CurrentPlayer {
        PLAYER_ONE,
        PLAYER_TWO,
        NOT_SET
    }

    inner class DurationListener : CoroutineScope by crossFadeScope() {

        private var job: Job? = null

        fun start() {
            job?.cancel()
            job = launch {
                while (isActive) {
                    delay(250)
                    onDurationUpdated(position(), duration())
                }
            }
        }

        fun stop() {
            job?.cancel()
        }
    }

    fun onDurationUpdated(progress: Int, total: Int) {
        if (total > 0 && (total - progress).div(1000) == crossFadeDuration) {
            getNextPlayer()?.let { player ->
                val nextSong = MusicPlayerRemote.nextSong
                if (nextSong != null && nextSong != Song.emptySong) {
                    nextDataSource = null
                    setDataSourceImpl(player, nextSong.uri.toString()) { success ->
                        if (success) switchPlayer()
                    }
                }
                else if (!nextDataSource.isNullOrEmpty()) {
                    setDataSourceImpl(player, nextDataSource!!) { success ->
                        if (success) switchPlayer()
                        nextDataSource = null
                    }
                }
            }
        }
    }

    private fun switchPlayer() {
        getNextPlayer()?.start()
        crossFade(getNextPlayer()!!, getCurrentPlayer()!!)
        currentPlayer =
            if (currentPlayer == CurrentPlayer.PLAYER_ONE || currentPlayer == CurrentPlayer.NOT_SET) {
                CurrentPlayer.PLAYER_TWO
            } else {
                CurrentPlayer.PLAYER_ONE
            }
        callbacks?.onTrackEndedWithCrossfade()
    }

    override fun setCrossFadeDuration(duration: Int) {
        crossFadeDuration = duration
    }

    override fun setPlaybackSpeedPitch(speed: Float, pitch: Float) {
        getCurrentPlayer()?.setPlaybackSpeedPitch(speed, pitch)
        if (getNextPlayer()?.isPlaying == true) {
            getNextPlayer()?.setPlaybackSpeedPitch(speed, pitch)
        }
    }

    companion object {
        val TAG: String = CrossFadePlayer::class.java.simpleName
    }
}

internal fun crossFadeScope(): CoroutineScope = CoroutineScope(Job() + Dispatchers.Default)

fun MediaPlayer.setPlaybackSpeedPitch(speed: Float, pitch: Float) {
    if (hasMarshmallow()) {
        val wasPlaying = isPlaying
        playbackParams = PlaybackParams().setSpeed(speed).setPitch(pitch)
        if (!wasPlaying) {
            pause()
        }
    }
}
