package com.example.gcore_vod_demo.data.remote

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.example.gcore_vod_demo.GCoreApp
import com.example.gcore_vod_demo.data.remote.auth.AuthRequestBody
import com.example.gcore_vod_demo.data.remote.auth.AuthResponse
import com.example.gcore_vod_demo.data.remote.refresh_token.RefreshRequestBody
import com.example.gcore_vod_demo.data.remote.video.RequestBodyForCreatingVideo
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

    private fun getAccessToken(fragmentActivity: FragmentActivity): String {
        return fragmentActivity.getSharedPreferences(
            fragmentActivity.getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).getString(ACCESS_TOKEN_KEY, "") ?: ""
    }

    fun isAuth(fragmentActivity: FragmentActivity) = getAccessToken(fragmentActivity).isNotEmpty()

    fun auth(
        fragmentActivity: FragmentActivity,
        requestBody: AuthRequestBody
    ): Single<AuthResponse> {

        return (fragmentActivity.application as GCoreApp).authApi
            .performLogin(requestBody)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun auth(fragmentActivity: FragmentActivity): Single<AuthResponse> {

        val eMail = fragmentActivity.getSharedPreferences(
            fragmentActivity.getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).getString(EMAIL_KEY, "") ?: ""

        val password = fragmentActivity.getSharedPreferences(
            fragmentActivity.getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).getString(PASSWORD_KEY, "") ?: ""

        val requestBody = AuthRequestBody(eMail = eMail, password = password)

        return (fragmentActivity.application as GCoreApp).authApi
            .performLogin(requestBody)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun refreshToken(fragmentActivity: FragmentActivity): Single<AuthResponse> {
        val refreshToken = fragmentActivity.getSharedPreferences(
            fragmentActivity.getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).getString(REFRESH_TOKEN_KEY, "") ?: ""

        val requestBody = RefreshRequestBody(refreshToken)

        return (fragmentActivity.application as GCoreApp).refreshTokenApi
            .refreshToken(requestBody)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadVideoItems(
        fragmentActivity: FragmentActivity,
        page: Int
    ): Single<List<VideoItemResponse>> {
        val accessToken = getAccessToken(fragmentActivity)

        return (fragmentActivity.application as GCoreApp).videoApi
            .getVideoItems("Bearer $accessToken", page = page)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createVideo(
        fragmentActivity: FragmentActivity,
        requestBody: RequestBodyForCreatingVideo
    ): Single<VideoItemResponse> {
        val accessToken = getAccessToken(fragmentActivity)

        return (fragmentActivity.application as GCoreApp).videoApi
            .createVideo("Bearer $accessToken", requestBody)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUrlAndTokenToUploadVideo(
        fragmentActivity: FragmentActivity,
        videoId: Int
    ): Single<UploadVideoResponse> {
        val accessToken = getAccessToken(fragmentActivity)

        return (fragmentActivity.application as GCoreApp).videoApi
            .getURLandTokenToUploadVideo("Bearer $accessToken", videoId)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }
}