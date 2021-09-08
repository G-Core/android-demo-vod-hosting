package com.example.gcore_vod_demo.data

import com.example.gcore_vod_demo.data.remote.video.VideoItemResponse
import com.example.gcore_vod_demo.extensions.toTimeFormat

data class VideoItemModel(
    val videoId: Int,
    val videoTitle: String,
    val videoDescription: String,
    var videoUri: String,
    val videoPreviewUri: String,
    var videoSizeInMB: Double,
    var videoDuration: String
) {

    companion object {
        fun getInstance(videoItem: VideoItemResponse): VideoItemModel {

            return VideoItemModel(
                videoId = videoItem.videoId,
                videoTitle = videoItem.videoName,
                videoDescription = videoItem.videoDescription ?: "",
                videoUri = videoItem.videoHlsURL,
                videoPreviewUri = videoItem.videoScreenshot ?: "",
                videoSizeInMB = getVideoSizeInMB(videoItem.originVideoSize ?: 0),
                videoDuration = (videoItem.videoDuration ?: 0).toTimeFormat()
            )
        }

        private const val oneMB = 1024 * 1024
        fun getVideoSizeInMB(videoSizeInByte: Long) = videoSizeInByte / oneMB.toDouble()
    }
}