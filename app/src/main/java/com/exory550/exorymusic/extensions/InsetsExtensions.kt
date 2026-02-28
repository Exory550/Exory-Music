package com.exory550.exorymusic.extensions

import androidx.core.view.WindowInsetsCompat
import com.exory550.exorymusic.util.PreferenceUtil
import com.exory550.exorymusic.util.RetroUtil

fun WindowInsetsCompat?.getBottomInsets(): Int {
    return if (PreferenceUtil.isFullScreenMode) {
        return 0
    } else {
        this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: RetroUtil.navigationBarHeight
    }
}
