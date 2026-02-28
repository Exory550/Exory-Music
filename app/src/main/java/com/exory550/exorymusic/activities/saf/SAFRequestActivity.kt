package com.exory550.exorymusic.activities.saf

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.exory550.exorymusic.activities.saf.SAFGuideActivity.REQUEST_CODE_SAF_GUIDE
import com.exory550.exorymusic.util.SAFUtil

class SAFRequestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, com.exory550.exorymusic.activities.saf.SAFGuideActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SAF_GUIDE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_CODE_SAF_GUIDE -> {
                SAFUtil.openTreePicker(this)
            }
            SAFUtil.REQUEST_SAF_PICK_TREE -> {
                if (resultCode == RESULT_OK) {
                    SAFUtil.saveTreeUri(this, intent)
                }
                finish()
            }
        }
    }
}
