package ru.vladleesi.ultimatescanner.data.retrofit.services

import io.reactivex.rxjava3.core.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET

interface AnalyzeServices {

    @GET("/analyze")
    fun analyze(@Body body: RequestBody): Single<ResponseBody>
}