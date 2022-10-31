package com.example.gcore_vod_demo.data.remote.video

import com.google.gson.annotations.SerializedName

data class VideoItemResponse(
    @SerializedName("id") val videoId: Int,
    @SerializedName("name") val videoName: String,
    @SerializedName("description") val videoDescription: String?,
    @SerializedName("client_id") val clientId: Int,
    @SerializedName("duration") val videoDuration: Long?,
    @SerializedName("slug") val endPartURL: String,
    @SerializedName("status") val videoStatus: String,
    @SerializedName("share_url") val videoShareURL: String?,
    @SerializedName("custom_iframe_url") val videoCustomIframeURL: String?,
    @SerializedName("origin_filename") val originVideoName: String?,
    @SerializedName("origin_size") val originVideoSize: Long?,
    @SerializedName("origin_storage") val originVideoStorage: Any?,
    @SerializedName("origin_host") val originVideoHost: String?,
    @SerializedName("origin_resource") val originVideoResource: String?,
    @SerializedName("origin_audio_channels") val originAudioChannels: Int?,
    @SerializedName("origin_height") val originHeightVideo: Int?,
    @SerializedName("origin_width") val originWidthVideo: Int?,
    @SerializedName("screenshots") val videoScreenshots: List<String?>?,
    @SerializedName("screenshot_id") val videoScreenshotId: Int,
    @SerializedName("ad_id") val videoAdId: Int?,
    @SerializedName("stream_id") val videoStreamId: Int?,
    @SerializedName("client_user_id") val clientUserId: Int?,
    @SerializedName("recording_started_at") val recordingStartedAt: Any?,
    @SerializedName("projection") val videoProjection: String,
    @SerializedName("player_id") val videoPlayerId: Int?,
    @SerializedName("error") val videoError: String?,
    @SerializedName("encryption") val videoEncryption: String?,
    @SerializedName("hls_url") val videoHlsURL: String,
    @SerializedName("poster_thumb") val videoPosterThumb: String?,
    @SerializedName("poster") val videoPoster: String?,
    @SerializedName("screenshot") val videoScreenshot: String?,
    @SerializedName("views") val videoViews: Int,
    @SerializedName("folders") val videoFolders: List<Any>
)