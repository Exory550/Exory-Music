package com.exory550.exorymusic.fragments.player.fit

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.exory550.appthemehelper.util.ToolbarContentTintHelper
import com.exory550.exorymusic.R
import com.exory550.exorymusic.databinding.FragmentFitBinding
import com.exory550.exorymusic.extensions.colorControlNormal
import com.exory550.exorymusic.extensions.drawAboveSystemBars
import com.exory550.exorymusic.extensions.whichFragment
import com.exory550.exorymusic.fragments.base.AbsPlayerFragment
import com.exory550.exorymusic.fragments.player.PlayerAlbumCoverFragment
import com.exory550.exorymusic.helper.MusicPlayerRemote
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.util.color.MediaNotificationProcessor

class FitFragment : AbsPlayerFragment(R.layout.fragment_fit) {
    private var _binding: FragmentFitBinding? = null
    private val binding get() = _binding!!

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor

    private lateinit var playbackControlsFragment: FitPlaybackControlsFragment

    override fun onShow() {
        playbackControlsFragment.show()
    }

    override fun onHide() {
        playbackControlsFragment.hide()
    }

    override fun toolbarIconColor(): Int {
        return colorControlNormal()
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        playbackControlsFragment.setColor(color)
        lastColor = color.primaryTextColor
        libraryViewModel.updateColor(color.primaryTextColor)
        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            colorControlNormal(),
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFitBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
        playerToolbar().drawAboveSystemBars()
    }

    private fun setUpSubFragments() {
        playbackControlsFragment = whichFragment(R.id.playbackControlsFragment)
        val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment.setCallbacks(this)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(this@FitFragment)
            ToolbarContentTintHelper.colorizeToolbar(
                this,
                colorControlNormal(),
                requireActivity()
            )
        }
    }

    override fun onServiceConnected() {
        updateIsFavorite()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): FitFragment {
            return FitFragment()
        }
    }
}
