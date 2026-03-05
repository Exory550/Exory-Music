package com.exory550.exorymusic.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import code.name.exory550.appthemehelper.ThemeStore
import code.name.exory550.appthemehelper.util.ATHUtil
import com.exory550.exorymusic.extensions.addAlpha
import com.exory550.exorymusic.extensions.setItemColors
import com.exory550.exorymusic.util.PreferenceUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.chrisbanes.insetter.applyInsetter

class TintedBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BottomNavigationView(context, attrs, defStyleAttr) {

    init {
        if (!isInEditMode) {
            if (PreferenceUtil.isFullScreenMode) {
                setOnApplyWindowInsetsListener { _, insets ->
                    insets
                }
            } else {
                applyInsetter {
                    type(navigationBars = true) {
                        padding(vertical = true)
                        margin(horizontal = true)
                    }
                }
            }

            labelVisibilityMode = PreferenceUtil.tabTitleMode

            if (!PreferenceUtil.materialYou) {
                val iconColor = ATHUtil.resolveColor(context, android.R.attr.colorControlNormal)
                val accentColor = ThemeStore.accentColor(context)
                setItemColors(iconColor, accentColor)
                itemRippleColor = ColorStateList.valueOf(accentColor.addAlpha(0.08F))
                itemActiveIndicatorColor = ColorStateList.valueOf(accentColor.addAlpha(0.12F))
            }
        }
    }
}
