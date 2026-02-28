package com.exory550.exorymusic.activities

import android.app.KeyguardManager
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.exory550.appthemehelper.util.VersionUtils
import com.exory550.exorymusic.R
import com.exory550.exorymusic.activities.base.AbsMusicServiceActivity
import com.exory550.exorymusic.databinding.ActivityLockScreenBinding
import com.exory550.exorymusic.extensions.hideStatusBar
import com.exory550.exorymusic.extensions.setTaskDescriptionColorAuto
import com.exory550.exorymusic.extensions.whichFragment
import com.exory550.exorymusic.fragments.player.lockscreen.LockScreenControlsFragment
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension.asBitmapPalette
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension.songCoverOptions
import com.exory550.exorymusic.glide.ExoryMusicColoredTarget
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.util.color.MediaNotificationProcessor
import com.bumptech.glide.Glide
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrListener
import com.r0adkll.slidr.model.SlidrPosition

class LockScreenActivity : AbsMusicServiceActivity() {
    private lateinit var binding: ActivityLockScreenBinding
    private var fragment: LockScreenControlsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenInit()
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideStatusBar()
        setTaskDescriptionColorAuto()

        val config = SlidrConfig.Builder().listener(object : SlidrListener {
            override fun onSlideStateChanged(state: Int) {
            }

            override fun onSlideChange(percent: Float) {
            }

            override fun onSlideOpened() {
            }

            override fun onSlideClosed(): Boolean {
                if (VersionUtils.hasOreo()) {
                    val keyguardManager =
                        getSystemService<KeyguardManager>()
                    keyguardManager?.requestDismissKeyguard(this@LockScreenActivity, null)
                }
                finish()
                return true
            }
        }).position(SlidrPosition.BOTTOM).build()

        Slidr.attach(this, config)

        fragment = whichFragment<LockScreenControlsFragment>(R.id.playback_controls_fragment)

        binding.slide.apply {
            translationY = 100f
            alpha = 0f
            animate().translationY(0f).alpha(1f).setDuration(1500).start()
        }
    }

    @Suppress("Deprecation")
    private fun lockScreenInit() {
        if (VersionUtils.hasOreoMR1()) {
            setShowWhenLocked(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSongs()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSongs()
    }

    private fun updateSongs() {
        val song = MusicPlayerRemote.currentSong
        Glide.with(this)
            .asBitmapPalette()
            .songCoverOptions(song)
            .load(ExoryMusicGlideExtension.getSongModel(song))
            .dontAnimate()
            .into(object : ExoryMusicColoredTarget(binding.image) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    fragment?.setColor(colors)
                }
            })
    }
}
