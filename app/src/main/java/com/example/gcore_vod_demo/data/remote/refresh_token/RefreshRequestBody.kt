package com.example.gcore_vod_demo.data.remote.refresh_token

import com.google.gson.annotations.SerializedName

class RefreshRequestBody(
    @SerializedName("refresh") val refreshAccessToken: String
)