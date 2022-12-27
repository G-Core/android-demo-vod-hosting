package com.example.gcore_vod_demo.data.remote.account

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header

interface AccountApi {

    @GET("./iam/clients/me")
    fun getAccountDetails(
        @Header("Authorization") token: String
    ): Single<AccountDetailsResponse>
}