package com.exory550.exorymusic.glide

import android.content.Context
import android.graphics.Bitmap
import com.exory550.exorymusic.glide.artistimage.ArtistImage
import com.exory550.exorymusic.glide.artistimage.Factory
import com.exory550.exorymusic.glide.audiocover.AudioFileCover
import com.exory550.exorymusic.glide.audiocover.AudioFileCoverLoader
import com.exory550.exorymusic.glide.palette.BitmapPaletteTranscoder
import com.exory550.exorymusic.glide.palette.BitmapPaletteWrapper
import com.exory550.exorymusic.glide.playlistPreview.PlaylistPreview
import com.exory550.exorymusic.glide.playlistPreview.PlaylistPreviewLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class ExoryMusicGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            PlaylistPreview::class.java,
            Bitmap::class.java,
            PlaylistPreviewLoader.Factory(context)
        )
        registry.prepend(
            AudioFileCover::class.java,
            InputStream::class.java,
            AudioFileCoverLoader.Factory()
        )
        registry.prepend(ArtistImage::class.java, InputStream::class.java, Factory(context))
        registry.register(
            Bitmap::class.java,
            BitmapPaletteWrapper::class.java, BitmapPaletteTranscoder()
        )
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}
