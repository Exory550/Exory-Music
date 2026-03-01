package com.exory550.exorymusic.interfaces

import android.view.View
import com.exory550.exorymusic.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}
