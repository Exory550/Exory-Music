package com.exory550.exorymusic.fragments.player.card

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.exory550.appthemehelper.util.ATHUtil
import com.exory550.appthemehelper.util.ColorUtil
import com.exory550.appthemehelper.util.MaterialValueHelper
import com.exory550.appthemehelper.util.TintHelper
import com.exory550.exorymusic.R
import com.exory550.exorymusic.databinding.FragmentCardPlayerPlaybackControlsBinding
import com.exory550.exorymusic.extensions.*
import com.exory550.exorymusic.fragments.base.AbsPlayerControlsFragment
import com.exory550.exorymusic.fragments.base.goToAlbum
import com.exory550.exorymusic.fragments.base.goToArtist
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.util.PreferenceUtil
import com.exory550.exorymusic.util.color.MediaNotificationProcessor

class CardPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_card_player_playback_controls) {

    private var _binding: FragmentCardPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    override val seekBar: SeekBar
        get() = binding.progressSlider

    override val shuffleButton: ImageButton
        get() = binding.mediaButton.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.mediaButton.repeatButton

    override val nextButton: ImageButton
        get() = binding.mediaButton.nextButton

    override val previousButton: ImageButton
        get() = binding.mediaButton.previousButton

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCardPlayerPlaybackControlsBinding.bind(view)
        setUpPlayPauseFab()
        binding.title.isSelected = true
        binding.text.isSelected = true
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
            binding.songInfo.text = getSongInfo(MusicPlayerRemote.currentSong)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun setColor(color: MediaNotificationProcessor) {
        if (!ATHUtil.isWindowBackgroundDark(requireContext())
        ) {
            lastPlaybackControlsColor = MaterialValueHelper.getSecondaryTextColor(activity, true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(activity, true)
        } else {
            lastPlaybackControlsColor = MaterialValueHelper.getPrimaryTextColor(activity, false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(activity, false)
        }

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
        updatePlayPauseColor()
        updateProgressTextColor()

        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            color.primaryTextColor
        } else {
            accentColor().ripAlpha()
        }
        binding.image.setColorFilter(colorFinal, PorterDuff.Mode.SRC_IN)
        TintHelper.setTintAuto(
            binding.mediaButton.playPauseButton,
            MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(colorFinal)),
            false
        )
        TintHelper.setTintAuto(binding.mediaButton.playPauseButton, colorFinal, true)

        volumeFragment?.setTintable(colorFinal)
    }

    private fun updatePlayPauseColor() {
    }

    private fun setUpPlayPauseFab() {
        binding.mediaButton.playPauseButton.setOnClickListener {
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
            binding.mediaButton.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.mediaButton.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun updateProgressTextColor() {
        val color = MaterialValueHelper.getPrimaryTextColor(context, false)
        binding.songTotalTime.setTextColor(color)
        binding.songCurrentProgress.setTextColor(color)
    }

    public override fun show() {}

    public override fun hide() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
