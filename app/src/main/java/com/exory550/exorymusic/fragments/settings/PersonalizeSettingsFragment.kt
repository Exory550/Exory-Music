package com.exory550.exorymusic.fragments.settings

import android.os.Bundle
import android.view.View
import com.exory550.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.exory550.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import com.exory550.appthemehelper.util.VersionUtils
import com.exory550.exorymusic.*

class PersonalizeSettingsFragment : AbsSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_ui)
        val blurredAlbumArt: ATESwitchPreference? = findPreference(BLURRED_ALBUM_ART)
        blurredAlbumArt?.isVisible = !VersionUtils.hasR()
    }

    override fun invalidateSettings() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val albumArtOnLockscreen: ATESwitchPreference? = findPreference(ALBUM_ART_ON_LOCK_SCREEN)
        albumArtOnLockscreen?.isVisible = !VersionUtils.hasT()

        val homeArtistStyle: ATEListPreference? = findPreference(HOME_ARTIST_GRID_STYLE)
        homeArtistStyle?.setOnPreferenceChangeListener { preference, newValue ->
            setSummary(preference, newValue)
            true
        }
        val homeAlbumStyle: ATEListPreference? = findPreference(HOME_ALBUM_GRID_STYLE)
        homeAlbumStyle?.setOnPreferenceChangeListener { preference, newValue ->
            setSummary(preference, newValue)
            true
        }
        val tabTextMode: ATEListPreference? = findPreference(TAB_TEXT_MODE)
        tabTextMode?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            true
        }
        val appBarMode: ATEListPreference? = findPreference(APPBAR_MODE)
        appBarMode?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }
    }
}
