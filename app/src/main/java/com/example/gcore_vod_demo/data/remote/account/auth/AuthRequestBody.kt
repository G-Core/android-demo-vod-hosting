package com.example.gcore_vod_demo.data.remote.account.auth

import com.google.gson.annotations.SerializedName

class AuthRequestBody(
    @SerializedName("username") val eMail: String,
    @SerializedName("password") val password: String,
    @SerializedName("one_time_password") val oneTimePassword: String = "authenticator passcode"
)