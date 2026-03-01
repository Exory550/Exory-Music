package com.exory550.exorymusic.misc

import android.content.Context
import androidx.loader.content.AsyncTaskLoader

abstract class WrappedAsyncTaskLoader<D>
    (context: Context) : AsyncTaskLoader<D>(context) {

    private var mData: D? = null

    override fun deliverResult(data: D?) {
        if (!isReset) {
            this.mData = data
            super.deliverResult(data)
        }
    }

    override fun onStartLoading() {
        super.onStartLoading()
        if (this.mData != null) {
            deliverResult(this.mData)
        } else if (takeContentChanged() || this.mData == null) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        super.onStopLoading()
        cancelLoad()
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
        this.mData = null
    }
}
