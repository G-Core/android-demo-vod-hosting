package com.example.gcore_vod_demo.data.remote

import android.app.Application
import android.content.Context
import com.example.gcore_vod_demo.GCoreApp
import com.example.gcore_vod_demo.data.remote.account.AccountDetailsResponse
import com.example.gcore_vod_demo.data.remote.account.auth.AuthRequestBody
import com.example.gcore_vod_demo.data.remote.account.auth.AuthResponse
import com.example.gcore_vod_demo.data.remote.account.refresh_token.RefreshRequestBody
import com.example.gcore_vod_demo.data.remote.video.PostVideoRequestBody
import com.example.gcore_vod_demo.data.remote.video.UploadVideoResponse
import com.example.gcore_vod_demo.data.remote.video.VideoItemResponse
import gcore_vod_demo.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object RemoteAccessManager {

    const val EMAIL_KEY = "email"
    const val PASSWORD_KEY = "password"
    const val ACCESS_TOKEN_KEY = "accessToken"
    const val REFRESH_TOKEN_KEY = "refreshToken"

    private fun getAccessToken(app: Application): String {
        return app.getSharedPreferences(
            app.getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).getString(ACCESS_TOKEN_KEY, "") ?: ""
    }

    fun updateTokens(app: Application, authResponse: AuthResponse) {
        app.getSharedPreferences(
            app.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(ACCESS_TOKEN_KEY, authResponse.accessToken)
            .putString(REFRESH_TOKEN_KEY, authResponse.refreshAccessToken)
            .apply()
    }

    fun isAuth(app: Application) = getAccessToken(app).isNotEmpty()

    fun auth(
        app: Application,
        requestBody: AuthRequestBody
    ): Single<AuthResponse> {

        return (app as GCoreApp).authApi
            .performLogin(requestBody)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun auth(app: Application): Single<AuthResponse> {

        val eMail = app.getSharedPreferences(
            app.getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).getString(EMAIL_KEY, "") ?: ""

        val password = app.getSharedPreferences(
            app.getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).getString(PASSWORD_KEY, "") ?: ""

        val requestBody = AuthRequestBody(eMail = eMail, password = password)

        return (app as GCoreApp).authApi
            .performLogin(requestBody)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun signOut(app: Application) {
        app.getSharedPreferences(
            app.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )
            .edit()
            .clear()
            .apply()
    }

    fun refreshToken(app: Application): Single<AuthResponse> {
        val refreshToken = app.getSharedPreferences(
            app.getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).getString(REFRESH_TOKEN_KEY, "") ?: ""

        val requestBody = RefreshRequestBody(refreshToken)

        return (app as GCoreApp).refreshTokenApi
            .refreshToken(requestBody)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getAccountDetails(app: Application): Single<AccountDetailsResponse> {
        val accessToken = getAccessToken(app)

        return (app as GCoreApp).accountApi
            .getAccountDetails("Bearer $accessToken")
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadVideoItems(
        app: Application,
    ): Single<List<VideoItemResponse>> {
        val accessToken = getAccessToken(app)

        return (app as GCoreApp).videoApi
            .getVideoItems("Bearer $accessToken")
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun postVideo(
        app: Application,
        requestBody: PostVideoRequestBody
    ): Single<VideoItemResponse> {
        val accessToken = getAccessToken(app)

        return (app as GCoreApp).videoApi
            .postVideo("Bearer $accessToken", requestBody)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUrlAndTokenToUploadVideo(
        app: Application,
        videoId: Int
    ): Single<UploadVideoResponse> {
        val accessToken = getAccessToken(app)

        return (app as GCoreApp).videoApi
            .getURLandTokenToUploadVideo("Bearer $accessToken", videoId)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }
}