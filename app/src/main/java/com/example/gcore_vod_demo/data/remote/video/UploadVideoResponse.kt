package com.example.gcore_vod_demo.data.remote.video

import com.google.gson.annotations.SerializedName

data class UploadVideoResponse(
    @SerializedName("servers") val servers: List<ServerResponse>,
    @SerializedName("token") val uploadToken: String,
    @SerializedName("video") val uploadVideo: VideoItemResponse
)