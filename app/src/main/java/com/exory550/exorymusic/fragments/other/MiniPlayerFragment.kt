package com.exory550.exorymusic.fragments.other

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import com.exory550.exorymusic.R
import com.exory550.exorymusic.databinding.FragmentMiniPlayerBinding
import com.exory550.exorymusic.extensions.accentColor
import com.exory550.exorymusic.extensions.show
import com.exory550.exorymusic.extensions.textColorPrimary
import com.exory550.exorymusic.extensions.textColorSecondary
import com.exory550.exorymusic.fragments.base.AbsMusicServiceFragment
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension.songCoverOptions
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.helper.MusicProgressViewUpdateHelper
import com.exory550.exorymusic.helper.PlayPauseButtonOnClickHandler
import com.exory550.exorymusic.util.PreferenceUtil
import com.exory550.exorymusic.util.RetroUtil
import com.bumptech.glide.Glide
import kotlin.math.abs

open class MiniPlayerFragment : AbsMusicServiceFragment(R.layout.fragment_mini_player),
    MusicProgressViewUpdateHelper.Callback, View.OnClickListener {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.actionNext -> MusicPlayerRemote.playNextSong()
            R.id.actionPrevious -> MusicPlayerRemote.back()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMiniPlayerBinding.bind(view)
        view.setOnTouchListener(FlingPlayBackController(requireContext()))
        setUpMiniPlayer()
        setUpButtons()
    }

    fun setUpButtons() {
        if (RetroUtil.isTablet) {
            binding.actionNext.show()
            binding.actionPrevious.show()
        } else {
            binding.actionNext.isVisible = PreferenceUtil.isExtraControls
            binding.actionPrevious.isVisible = PreferenceUtil.isExtraControls
        }
        binding.actionNext.setOnClickListener(this)
        binding.actionPrevious.setOnClickListener(this)
    }

    private fun setUpMiniPlayer() {
        setUpPlayPauseButton()
        binding.progressBar.accentColor()
    }

    private fun setUpPlayPauseButton() {
        binding.miniPlayerPlayPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updateSongTitle() {

        val song = MusicPlayerRemote.currentSong

        val builder = SpannableStringBuilder()

        val title = song.title.toSpannable()
        title.setSpan(ForegroundColorSpan(textColorPrimary()), 0, title.length, 0)

        val text = song.artistName.toSpannable()
        text.setSpan(ForegroundColorSpan(textColorSecondary()), 0, text.length, 0)

        builder.append(title).append(" • ").append(text)

        binding.miniPlayerTitle.isSelected = true
        binding.miniPlayerTitle.text = builder
    }

    private fun updateSongCover() {
        val song = MusicPlayerRemote.currentSong
        Glide.with(requireContext())
            .load(ExoryMusicGlideExtension.getSongModel(song))
            .transition(ExoryMusicGlideExtension.getDefaultTransition())
            .songCoverOptions(song)
            .into(binding.image)
    }

    override fun onServiceConnected() {
        updateSongTitle()
        updateSongCover()
        updatePlayPauseDrawableState()
    }

    override fun onPlayingMetaChanged() {
        updateSongTitle()
        updateSongCover()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.progressBar.max = total
        binding.progressBar.progress = progress
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    protected fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    class FlingPlayBackController(context: Context) : View.OnTouchListener {

        private var flingPlayBackController = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (abs(velocityX) > abs(velocityY)) {
                        if (velocityX < 0) {
                            MusicPlayerRemote.playNextSong()
                            return true
                        } else if (velocityX > 0) {
                            MusicPlayerRemote.playPreviousSong()
                            return true
                        }
                    }
                    return false
                }
            })

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return flingPlayBackController.onTouchEvent(event)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
