package com.exory550.exorymusic.model

import androidx.annotation.StringRes
import com.exory550.exorymusic.HomeSection

data class Home(
    val arrayList: List<Any>,
    @HomeSection
    val homeSection: Int,
    @StringRes
    val titleRes: Int
)
