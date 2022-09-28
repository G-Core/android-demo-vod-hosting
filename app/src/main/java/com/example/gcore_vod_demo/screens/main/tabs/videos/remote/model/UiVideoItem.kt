package com.example.gcore_vod_demo.screens.main.tabs.videos.remote.model

import android.os.Parcelable
import com.example.gcore_vod_demo.data.remote.video.VideoItemResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class UiVideoItem(
    val id: Int,
    val name: String,
    var uri: String,
    val previewUri: String,
    val originWidth: Int,
    val originHeight: Int
) : Parcelable {

    companion object {
        fun getInstance(videoItem: VideoItemResponse): UiVideoItem {

            return UiVideoItem(
                id = videoItem.id,
                name = videoItem.name,
                uri = videoItem.hlsURL,
                previewUri = videoItem.screenshot ?: "",
                originWidth = videoItem.originWidthVideo,
                originHeight = videoItem.originHeightVideo
            )
        }
    }
}