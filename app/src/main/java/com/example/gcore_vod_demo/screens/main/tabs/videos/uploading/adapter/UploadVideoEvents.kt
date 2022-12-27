package com.example.gcore_vod_demo.screens.main.tabs.videos.uploading.adapter

import com.example.gcore_vod_demo.screens.main.tabs.videos.uploading.model.UiUploadingVideo

interface UploadVideoEvents {
    fun onWatchVideo(video: UiUploadingVideo)
    fun onCancelUpload(video: UiUploadingVideo)
    fun onResumeUpload(video: UiUploadingVideo)
}