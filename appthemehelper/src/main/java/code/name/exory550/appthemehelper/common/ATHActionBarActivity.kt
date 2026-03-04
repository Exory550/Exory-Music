package code.name.exory550.appthemehelper.common

import androidx.appcompat.widget.Toolbar

import code.name.exory550.appthemehelper.util.ToolbarContentTintHelper

class ATHActionBarActivity : ATHToolbarActivity() {

    override fun getATHToolbar(): Toolbar? {
        return ToolbarContentTintHelper.getSupportActionBarView(supportActionBar)
    }
}
