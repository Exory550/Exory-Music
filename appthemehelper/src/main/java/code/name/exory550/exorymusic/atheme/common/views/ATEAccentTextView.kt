package code.name.exory550.exorymusic.atheme.common.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import code.name.exory550.exorymusic.atheme.ThemeStore

class ATEAccentTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        setTextColor(ThemeStore.accentColor(context))
    }
}
