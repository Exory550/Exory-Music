package com.exory550.exorymusic.fragments.settings

import android.os.Bundle
import com.exory550.exorymusic.R

class ImageSettingsFragment : AbsSettingsFragment() {
    override fun invalidateSettings() {}

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_images)
    }
}
