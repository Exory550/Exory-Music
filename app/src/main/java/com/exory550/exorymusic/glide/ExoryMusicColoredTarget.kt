package com.exory550.exorymusic.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.exory550.exorymusic.App
import com.exory550.exorymusic.extensions.colorControlNormal
import com.exory550.exorymusic.glide.palette.BitmapPaletteTarget
import com.exory550.exorymusic.glide.palette.BitmapPaletteWrapper
import com.exory550.exorymusic.util.color.MediaNotificationProcessor
import com.bumptech.glide.request.transition.Transition

abstract class ExoryMusicColoredTarget(view: ImageView) : BitmapPaletteTarget(view) {

    protected val defaultFooterColor: Int
        get() = getView().context.colorControlNormal()

    abstract fun onColorReady(colors: MediaNotificationProcessor)

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        onColorReady(MediaNotificationProcessor.errorColor(App.getContext()))
    }

    override fun onResourceReady(
        resource: BitmapPaletteWrapper,
        transition: Transition<in BitmapPaletteWrapper>?
    ) {
        super.onResourceReady(resource, transition)
        MediaNotificationProcessor(App.getContext()).getPaletteAsync({
            onColorReady(it)
        }, resource.bitmap)
    }
}
