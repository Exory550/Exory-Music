package com.exory550.exorymusic.fragments.player.simple

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import com.exory550.appthemehelper.util.ATHUtil
import com.exory550.appthemehelper.util.ColorUtil
import com.exory550.appthemehelper.util.MaterialValueHelper
import com.exory550.appthemehelper.util.TintHelper
import com.exory550.exorymusic.R
import com.exory550.exorymusic.databinding.FragmentSimpleControlsFragmentBinding
import com.exory550.exorymusic.extensions.accentColor
import com.exory550.exorymusic.extensions.getSongInfo
import com.exory550.exorymusic.extensions.hide
import com.exory550.exorymusic.extensions.show
import com.exory550.exorymusic.fragments.base.AbsPlayerControlsFragment
import com.exory550.exorymusic.fragments.base.goToAlbum
import com.exory550.exorymusic.fragments.base.goToArtist
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.util.MusicUtil
import com.exory550.exorymusic.util.PreferenceUtil
import com.exory550.exorymusic.util.color.MediaNotificationProcessor

class SimplePlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_simple_controls_fragment) {

    private var _binding: FragmentSimpleControlsFragmentBinding? = null
    private val binding get() = _binding!!

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override val nextButton: ImageButton
        get() = binding.nextButton

    override val previousButton: ImageButton
        get() = binding.previousButton

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimpleControlsFragmentBinding.bind(view)
        setUpPlayPauseFab()
        binding.title.isSelected = true
        binding.text.setOnClickListener {
            goToArtist(requireActivity())
        }

        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.text.setOnClickListener {
            goToArtist(requireActivity())
        }
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = song.artistName

        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(song)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    public override fun show() {
        binding.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        binding.playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.songCurrentProgress.text = String.format(
            "%s / %s",
            MusicUtil.getReadableDurationString(progress.toLong()),
            MusicUtil.getReadableDurationString(total.toLong())
        )
    }

    override fun setColor(color: MediaNotificationProcessor) {
        val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
        if (ColorUtil.isColorLight(colorBg)) {
            lastPlaybackControlsColor =
                MaterialValueHelper.getSecondaryTextColor(requireContext(), true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), true)
        } else {
            lastPlaybackControlsColor =
                MaterialValueHelper.getPrimaryTextColor(requireContext(), false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), false)
        }

        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            color.primaryTextColor
        } else {
            accentColor()
        }

        volumeFragment?.setTintable(colorFinal)

        TintHelper.setTintAuto(
            binding.playPauseButton,
            MaterialValueHelper.getPrimaryTextColor(
                requireContext(),
                ColorUtil.isColorLight(colorFinal)
            ),
            false
        )
        TintHelper.setTintAuto(binding.playPauseButton, colorFinal, true)
        binding.text.setTextColor(colorFinal)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
            it.showBounceAnimation()
        }
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
