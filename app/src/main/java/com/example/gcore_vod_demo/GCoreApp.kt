package com.example.gcore_vod_demo

import android.app.Application
import androidx.work.Configuration
import com.example.gcore_vod_demo.data.remote.account.AccountApi
import com.example.gcore_vod_demo.data.remote.account.auth.AuthApi
import com.example.gcore_vod_demo.data.remote.account.refresh_token.RefreshTokenApi
import com.example.gcore_vod_demo.data.remote.video.VideoApi
import com.example.gcore_vod_demo.utils.ExoPlayerUtils
import gcore_vod_demo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class GCoreApp : Application(), Configuration.Provider {
    lateinit var authApi: AuthApi
    lateinit var videoApi: VideoApi
    lateinit var refreshTokenApi: RefreshTokenApi
    lateinit var accountApi: AccountApi

    lateinit var exoPlayerUtils: ExoPlayerUtils

    override fun onCreate() {
        super.onCreate()

        configureNetwork()
        exoPlayerUtils = ExoPlayerUtils(this)
    }

    private fun configureNetwork() {
        // For logging requests
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())     // To convert Json to Kotlin objects
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())      // To convert retrofit responses to rxjava objects
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        videoApi = retrofit.create(VideoApi::class.java)
        refreshTokenApi = retrofit.create(RefreshTokenApi::class.java)
        accountApi = retrofit.create(AccountApi::class.java)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return if (BuildConfig.DEBUG) {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
        } else {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.ERROR)
                .build()
        }
    }

    companion object {
        private const val BASE_URL = "https://api.gcorelabs.com"
    }
}