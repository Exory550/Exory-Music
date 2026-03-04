package code.name.exory550.exorymusic.atheme.common

import androidx.appcompat.widget.Toolbar

import code.name.exory550.exorymusic.atheme.util.ToolbarContentTintHelper

class ATHActionBarActivity : ATHToolbarActivity() {

    override fun getATHToolbar(): Toolbar? {
        return ToolbarContentTintHelper.getSupportActionBarView(supportActionBar)
    }
}
