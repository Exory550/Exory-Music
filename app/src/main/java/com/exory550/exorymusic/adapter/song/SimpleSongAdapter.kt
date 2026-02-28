package com.exory550.exorymusic.adapter.song

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.exory550.exorymusic.model.Song
import com.exory550.exorymusic.util.MusicUtil

class SimpleSongAdapter(
    context: FragmentActivity,
    songs: ArrayList<Song>,
    layoutRes: Int
) : SongAdapter(context, songs, layoutRes) {

    override fun swapDataSet(dataSet: List<Song>) {
        this.dataSet = dataSet.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val fixedTrackNumber = MusicUtil.getFixedTrackNumber(dataSet[position].trackNumber)
        val trackAndTime = (if (fixedTrackNumber > 0) "$fixedTrackNumber | " else "") +
                MusicUtil.getReadableDurationString(dataSet[position].duration)

        holder.time?.text = trackAndTime
        holder.text2?.text = dataSet[position].artistName
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}
