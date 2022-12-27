package com.example.gcore_vod_demo.screens.main.tabs.videos.uploading.model

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.example.gcore_vod_demo.data.remote.video.UploadVideoResponse

data class UiUploadingVideo(
    val videoName: String,
    val remoteData: UploadVideoResponse,
    val localUri: String,
    var state: LiveData<WorkInfo>? = null
)