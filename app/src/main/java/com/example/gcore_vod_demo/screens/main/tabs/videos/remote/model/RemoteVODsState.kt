package com.example.gcore_vod_demo.screens.main.tabs.videos.remote.model

sealed class RemoteVODsState {
    object Loading : RemoteVODsState()
    class Success(val videos: List<UiVideoItem>) : RemoteVODsState()
    object AccessDenied : RemoteVODsState()
    object Error : RemoteVODsState()
    object Empty : RemoteVODsState()
}