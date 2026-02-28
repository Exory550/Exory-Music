package com.exory550.exorymusic.fragments.player.cardblur

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.exory550.appthemehelper.util.ToolbarContentTintHelper
import com.exory550.exorymusic.NEW_BLUR_AMOUNT
import com.exory550.exorymusic.R
import com.exory550.exorymusic.databinding.FragmentCardBlurPlayerBinding
import com.exory550.exorymusic.extensions.drawAboveSystemBars
import com.exory550.exorymusic.extensions.whichFragment
import com.exory550.exorymusic.fragments.base.AbsPlayerFragment
import com.exory550.exorymusic.fragments.player.PlayerAlbumCoverFragment
import com.exory550.exorymusic.glide.BlurTransformation
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension.simpleSongCoverOptions
import com.exory550.exorymusic.glide.crossfadeListener
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.util.PreferenceUtil.blurAmount
import com.exory550.exorymusic.util.color.MediaNotificationProcessor
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder

class CardBlurFragment : AbsPlayerFragment(R.layout.fragment_card_blur_player),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor
    private lateinit var playbackControlsFragment: CardBlurPlaybackControlsFragment

    private var _binding: FragmentCardBlurPlayerBinding? = null
    private val binding get() = _binding!!
    private var lastRequest: RequestBuilder<Drawable>? = null

    override fun onShow() {
        playbackControlsFragment.show()
    }

    override fun onHide() {
        playbackControlsFragment.hide()
    }

    override fun toolbarIconColor(): Int {
        return Color.WHITE
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        playbackControlsFragment.setColor(color)
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)
        ToolbarContentTintHelper.colorizeToolbar(binding.playerToolbar, Color.WHITE, activity)

        binding.title.setTextColor(Color.WHITE)
        binding.text.setTextColor(Color.WHITE)
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
        _binding = FragmentCardBlurPlayerBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
        binding.playerToolbar.drawAboveSystemBars()
    }

    private fun setUpSubFragments() {
        playbackControlsFragment =
            whichFragment(R.id.playbackControlsFragment) as CardBlurPlaybackControlsFragment
        (whichFragment(R.id.playerAlbumCoverFragment) as? PlayerAlbumCoverFragment)?.setCallbacks(
            this
        )
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
            setTitleTextColor(Color.WHITE)
            setSubtitleTextColor(Color.WHITE)
            ToolbarContentTintHelper.colorizeToolbar(binding.playerToolbar, Color.WHITE, activity)
            setOnMenuItemClickListener(this@CardBlurFragment)
        }
    }

    override fun onServiceConnected() {
        updateIsFavorite()
        updateBlur()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
        updateBlur()
        updateSong()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.run {
            title.text = song.title
            text.text = song.artistName
        }
    }

    private fun updateBlur() {
        Glide.with(this)
            .load(ExoryMusicGlideExtension.getSongModel(MusicPlayerRemote.currentSong))
            .simpleSongCoverOptions(MusicPlayerRemote.currentSong)
            .transform(
                BlurTransformation.Builder(requireContext()).blurRadius(blurAmount.toFloat())
                    .build()
            )
            .thumbnail(lastRequest).also {
                lastRequest = it.clone()
                it.crossfadeListener()
                    .into(binding.colorBackground)
            }
    }

    override fun onPause() {
        super.onPause()
        lastRequest = null
    }

    override fun onResume() {
        super.onResume()
        lastRequest = null
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == NEW_BLUR_AMOUNT) {
            updateBlur()
        }
    }
}
