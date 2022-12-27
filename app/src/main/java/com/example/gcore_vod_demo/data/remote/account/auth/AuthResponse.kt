package com.example.gcore_vod_demo.data.remote.account.auth

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("refresh") val refreshAccessToken: String,
    @SerializedName("access") val accessToken: String
)