package com.exory550.exorymusic.fragments.player.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.exory550.appthemehelper.util.ToolbarContentTintHelper
import com.exory550.exorymusic.R
import com.exory550.exorymusic.databinding.FragmentHomePlayerBinding
import com.exory550.exorymusic.extensions.colorControlNormal
import com.exory550.exorymusic.fragments.base.AbsPlayerFragment
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.helper.MusicProgressViewUpdateHelper
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.util.MusicUtil
import com.exory550.exorymusic.util.color.MediaNotificationProcessor

class HomePlayerFragment : AbsPlayerFragment(R.layout.fragment_home_player),
    MusicProgressViewUpdateHelper.Callback {
    private var lastColor: Int = 0
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    private var _binding: FragmentHomePlayerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomePlayerBinding.bind(view)
        setUpPlayerToolbar()
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    override fun onShow() {
    }

    override fun onHide() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = song.artistName
    }

    override fun toolbarIconColor(): Int {
        return Color.WHITE
    }

    override val paletteColor: Int
        get() = lastColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)
        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            Color.WHITE,
            requireActivity()
        )
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

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.songTotalTime.text = MusicUtil.getReadableDurationString(progress.toLong())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
