package com.exory550.exorymusic.util.theme

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import com.exory550.exorymusic.R
import com.exory550.exorymusic.extensions.generalThemeValue
import com.exory550.exorymusic.util.PreferenceUtil
import com.exory550.exorymusic.util.theme.ThemeMode.*

@StyleRes
fun Context.getThemeResValue(): Int =
    if (PreferenceUtil.materialYou) {
        if (generalThemeValue == BLACK) R.style.Theme_ExoryMusic_MD3_Black
        else R.style.Theme_ExoryMusic_MD3
    } else {
        when (generalThemeValue) {
            LIGHT -> R.style.Theme_ExoryMusic_Light
            DARK -> R.style.Theme_ExoryMusic_Base
            BLACK -> R.style.Theme_ExoryMusic_Black
            AUTO -> R.style.Theme_ExoryMusic_FollowSystem
        }
    }

fun Context.getNightMode(): Int = when (generalThemeValue) {
    LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    DARK -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}
