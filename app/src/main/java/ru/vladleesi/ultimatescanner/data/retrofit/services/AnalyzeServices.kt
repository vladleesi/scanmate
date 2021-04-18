package ru.vladleesi.ultimatescanner.data.retrofit.services

import io.reactivex.rxjava3.core.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface AnalyzeServices {

    @GET
    fun testConnection(@Url endpoint: String): Single<Response<ResponseBody>>

    @POST
    fun analyze(@Url endpoint: String, @Body body: RequestBody): Single<ResponseBody>
}