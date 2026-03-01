package com.exory550.exorymusic.fragments.player.normal

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.exory550.appthemehelper.util.ToolbarContentTintHelper
import com.exory550.exorymusic.R
import com.exory550.exorymusic.SNOWFALL
import com.exory550.exorymusic.databinding.FragmentPlayerBinding
import com.exory550.exorymusic.extensions.*
import com.exory550.exorymusic.fragments.base.AbsPlayerFragment
import com.exory550.exorymusic.fragments.player.PlayerAlbumCoverFragment
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.util.PreferenceUtil
import com.exory550.exorymusic.util.ViewUtil
import com.exory550.exorymusic.util.color.MediaNotificationProcessor
import com.exory550.exorymusic.views.DrawableGradient

class PlayerFragment : AbsPlayerFragment(R.layout.fragment_player),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor

    private lateinit var controlsFragment: PlayerPlaybackControlsFragment
    private var valueAnimator: ValueAnimator? = null

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private fun colorize(i: Int) {
        if (valueAnimator != null) {
            valueAnimator?.cancel()
        }

        valueAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            surfaceColor(),
            i
        )
        valueAnimator?.addUpdateListener { animation ->
            if (isAdded) {
                val drawable = DrawableGradient(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        animation.animatedValue as Int,
                        surfaceColor()
                    ), 0
                )
                binding.colorGradientBackground.background = drawable
            }
        }
        valueAnimator?.setDuration(ViewUtil.EXORY_MUSIC_ANIM_TIME.toLong())?.start()
    }

    override fun onShow() {
        controlsFragment.show()
    }

    override fun onHide() {
        controlsFragment.hide()
    }

    override fun toolbarIconColor() = colorControlNormal()

    override fun onColorChanged(color: MediaNotificationProcessor) {
        controlsFragment.setColor(color)
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            colorControlNormal(),
            requireActivity()
        )

        if (PreferenceUtil.isAdaptiveColor) {
            colorize(color.backgroundColor)
        }
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
        startOrStopSnow(PreferenceUtil.isSnowFalling)

        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
        playerToolbar().drawAboveSystemBars()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        _binding = null
    }

    private fun setUpSubFragments() {
        controlsFragment = whichFragment(R.id.playbackControlsFragment)
        val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment.setCallbacks(this)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.inflateMenu(R.menu.menu_player)
        binding.playerToolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.playerToolbar.setOnMenuItemClickListener(this)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            colorControlNormal(),
            requireActivity()
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == SNOWFALL) {
            startOrStopSnow(PreferenceUtil.isSnowFalling)
        }
    }

    private fun startOrStopSnow(isSnowFalling: Boolean) {
        if (_binding == null) return
        if (isSnowFalling && !surfaceColor().isColorLight) {
            binding.snowfallView.isVisible = true
            binding.snowfallView.restartFalling()
        } else {
            binding.snowfallView.isVisible = false
            binding.snowfallView.stopFalling()
        }
    }

    override fun onServiceConnected() {
        updateIsFavorite()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
    }

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    companion object {
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}
