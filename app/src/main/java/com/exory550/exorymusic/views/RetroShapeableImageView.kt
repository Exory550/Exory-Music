package com.exory550.exorymusic.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.exory550.exorymusic.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class RetroShapeableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = -1
) : ShapeableImageView(context, attrs, defStyle) {

    init {
        context.withStyledAttributes(attrs, R.styleable.RetroShapeableImageView, defStyle, -1) {
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val radius = width / 2f
                shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(radius)
            }
        }
    }

    private fun updateCornerSize(cornerSize: Float) {
        shapeAppearanceModel = ShapeAppearanceModel.Builder()
            .setAllCorners(CornerFamily.ROUNDED, cornerSize)
            .build()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
