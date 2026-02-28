package com.exory550.exorymusic.activities

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import android.view.MenuItem
import androidx.core.net.toUri
import androidx.core.os.BundleCompat
import androidx.core.view.drawToBitmap
import com.exory550.appthemehelper.util.ColorUtil
import com.exory550.appthemehelper.util.MaterialValueHelper
import com.exory550.exorymusic.activities.base.AbsThemeActivity
import com.exory550.exorymusic.databinding.ActivityShareInstagramBinding
import com.exory550.exorymusic.extensions.accentColor
import com.exory550.exorymusic.extensions.setStatusBarColor
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension.asBitmapPalette
import com.exory550.exorymusic.glide.ExoryMusicGlideExtension.songCoverOptions
import com.exory550.exorymusic.glide.ExoryMusicColoredTarget
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.util.Share
import com.exory550.exorymusic.util.color.MediaNotificationProcessor
import com.bumptech.glide.Glide

class ShareInstagramStory : AbsThemeActivity() {

    private lateinit var binding: ActivityShareInstagramBinding

    companion object {
        const val EXTRA_SONG = "extra_song"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareInstagramBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(Color.TRANSPARENT)

        binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
        setSupportActionBar(binding.toolbar)

        val song = intent.extras?.let { BundleCompat.getParcelable(it, EXTRA_SONG, Song::class.java) }
        song?.let { songFinal ->
            Glide.with(this)
                .asBitmapPalette()
                .songCoverOptions(songFinal)
                .load(ExoryMusicGlideExtension.getSongModel(songFinal))
                .into(object : ExoryMusicColoredTarget(binding.image) {
                    override fun onColorReady(colors: MediaNotificationProcessor) {
                        setColors(colors.backgroundColor)
                    }
                })

            binding.shareTitle.text = songFinal.title
            binding.shareText.text = songFinal.artistName
            binding.shareButton.setOnClickListener {
                val path: String = Media.insertImage(
                    contentResolver,
                    binding.mainContent.drawToBitmap(Bitmap.Config.ARGB_8888),
                    "Design", null
                )
                Share.shareStoryToSocial(
                    this@ShareInstagramStory,
                    path.toUri()
                )
            }
        }
        binding.shareButton.setTextColor(
            MaterialValueHelper.getPrimaryTextColor(
                this,
                ColorUtil.isColorLight(accentColor())
            )
        )
        binding.shareButton.backgroundTintList =
            ColorStateList.valueOf(accentColor())
    }

    private fun setColors(color: Int) {
        binding.mainContent.background =
            GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(color, Color.BLACK)
            )
    }
}
