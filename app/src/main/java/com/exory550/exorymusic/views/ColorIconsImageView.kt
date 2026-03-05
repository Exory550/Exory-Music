package com.exory550.exorymusic.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import code.name.exory550.appthemehelper.ThemeStore
import code.name.exory550.appthemehelper.util.ATHUtil
import code.name.exory550.appthemehelper.util.ColorUtil
import com.exory550.exorymusic.R
import com.exory550.exorymusic.util.PreferenceUtil
import com.exory550.exorymusic.util.RetroColorUtil
import com.google.android.material.color.MaterialColors

class ColorIconsImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        context.withStyledAttributes(attrs, R.styleable.ColorIconsImageView, 0, 0) {
            val color = getColor(R.styleable.ColorIconsImageView_iconBackgroundColor, Color.RED)
            setIconBackgroundColor(color)
        }
    }

    fun setIconBackgroundColor(color: Int) {
        background = ContextCompat.getDrawable(context, R.drawable.color_circle_gradient)
        if (ATHUtil.isWindowBackgroundDark(context) && PreferenceUtil.isDesaturatedColor) {
            val desaturatedColor = RetroColorUtil.desaturateColor(color, 0.4f)
            backgroundTintList = ColorStateList.valueOf(desaturatedColor)
            imageTintList =
                ColorStateList.valueOf(ATHUtil.resolveColor(context, com.google.android.material.R.attr.colorSurface))
        } else {
            val finalColor = MaterialColors.harmonize(
                color,
                ThemeStore.accentColor(context)
            )
            backgroundTintList = ColorStateList.valueOf(ColorUtil.adjustAlpha(finalColor, 0.22f))
            imageTintList = ColorStateList.valueOf(ColorUtil.withAlpha(finalColor, 0.75f))
        }
        requestLayout()
        invalidate()
    }
}
