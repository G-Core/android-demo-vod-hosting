package com.example.gcore_vod_demo.screens.videos

import androidx.recyclerview.widget.DiffUtil
import com.example.gcore_vod_demo.data.VideoItemModel

class VideosDiffCallback(
    private val oldList: List<VideoItemModel>,
    private val newList: List<VideoItemModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldVideoItem = oldList[oldItemPosition]
        val newVideoItem = newList[newItemPosition]

        return oldVideoItem.videoId == newVideoItem.videoId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldVideoItem = oldList[oldItemPosition]
        val newVideoItem = newList[newItemPosition]

        return oldVideoItem == newVideoItem
    }
}