package com.exory550.exorymusic.fragments.settings

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.exory550.appthemehelper.ACCENT_COLORS
import com.exory550.appthemehelper.ACCENT_COLORS_SUB
import com.exory550.appthemehelper.ThemeStore
import com.exory550.appthemehelper.common.prefs.supportv7.ATEColorPreference
import com.exory550.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import com.exory550.appthemehelper.util.ColorUtil
import com.exory550.appthemehelper.util.VersionUtils
import com.exory550.exorymusic.*
import com.exory550.exorymusic.appshortcuts.DynamicShortcutManager
import com.exory550.exorymusic.extensions.materialDialog
import com.exory550.exorymusic.fragments.NowPlayingScreen.*
import com.exory550.exorymusic.util.PreferenceUtil
import com.afollestad.materialdialogs.color.colorChooser
import com.google.android.material.color.DynamicColors

class ThemeSettingsFragment : AbsSettingsFragment() {
    @SuppressLint("CheckResult")
    override fun invalidateSettings() {
        val generalTheme: Preference? = findPreference(GENERAL_THEME)
        generalTheme?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                ThemeStore.markChanged(requireContext())

                if (VersionUtils.hasNougatMR()) {
                    DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                }
                restartActivity()
                true
            }
        }

        val accentColorPref: ATEColorPreference? = findPreference(ACCENT_COLOR)
        val accentColor = ThemeStore.accentColor(requireContext())
        accentColorPref?.setColor(accentColor, ColorUtil.darkenColor(accentColor))
        accentColorPref?.setOnPreferenceClickListener {
            materialDialog().show {
                colorChooser(
                    initialSelection = accentColor,
                    showAlphaSelector = false,
                    colors = ACCENT_COLORS,
                    subColors = ACCENT_COLORS_SUB, allowCustomArgb = true
                ) { _, color ->
                    ThemeStore.editTheme(requireContext()).accentColor(color).commit()
                    if (VersionUtils.hasNougatMR())
                        DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                    restartActivity()
                }
            }
            return@setOnPreferenceClickListener true
        }
        val blackTheme: ATESwitchPreference? = findPreference(BLACK_THEME)
        blackTheme?.setOnPreferenceChangeListener { _, _ ->
            ThemeStore.markChanged(requireContext())
            if (VersionUtils.hasNougatMR()) {
                requireActivity().setTheme(PreferenceUtil.themeResFromPrefValue("black"))
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
            }
            restartActivity()
            true
        }

        val desaturatedColor: ATESwitchPreference? = findPreference(DESATURATED_COLOR)
        desaturatedColor?.setOnPreferenceChangeListener { _, value ->
            val desaturated = value as Boolean
            ThemeStore.prefs(requireContext()).edit {
                putBoolean("desaturated_color", desaturated)
            }
            PreferenceUtil.isDesaturatedColor = desaturated
            restartActivity()
            true
        }

        val colorAppShortcuts: TwoStatePreference? = findPreference(SHOULD_COLOR_APP_SHORTCUTS)
        if (!VersionUtils.hasNougatMR()) {
            colorAppShortcuts?.isVisible = false
        } else {
            colorAppShortcuts?.isChecked = PreferenceUtil.isColoredAppShortcuts
            colorAppShortcuts?.setOnPreferenceChangeListener { _, newValue ->
                PreferenceUtil.isColoredAppShortcuts = newValue as Boolean
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                true
            }
        }

        val materialYou: ATESwitchPreference? = findPreference(MATERIAL_YOU)
        materialYou?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                DynamicColors.applyToActivitiesIfAvailable(App.getContext())
            }
            restartActivity()
            true
        }
        val wallpaperAccent: ATESwitchPreference? = findPreference(WALLPAPER_ACCENT)
        wallpaperAccent?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }
        val customFont: ATESwitchPreference? = findPreference(CUSTOM_FONT)
        customFont?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }

        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        adaptiveColor?.isEnabled =
            PreferenceUtil.nowPlayingScreen in listOf(Normal, Material, Flat)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        val wallpaperAccent: ATESwitchPreference? = findPreference(WALLPAPER_ACCENT)
        wallpaperAccent?.isVisible = VersionUtils.hasOreoMR1() && !VersionUtils.hasS()
    }
}
