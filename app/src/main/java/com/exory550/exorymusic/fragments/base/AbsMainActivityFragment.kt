package com.exory550.exorymusic.fragments.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.exory550.exorymusic.activities.MainActivity
import com.exory550.exorymusic.fragments.LibraryViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

abstract class AbsMainActivityFragment(@LayoutRes layout: Int) : AbsMusicServiceFragment(layout),
    MenuProvider {
    val libraryViewModel: LibraryViewModel by activityViewModel()

    val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
    }
}
