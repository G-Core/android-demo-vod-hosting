package com.example.gcore_vod_demo.data.remote.account.refresh_token

import com.example.gcore_vod_demo.data.remote.account.auth.AuthResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshTokenApi {
    @POST("./iam/auth/jwt/refresh")
    fun refreshToken(@Body body: RefreshRequestBody): Single<AuthResponse>
}