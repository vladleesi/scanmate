package ru.vladleesi.ultimatescanner.data.remote.services

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import ru.vladleesi.ultimatescanner.data.remote.model.AnalyzeResultApi

interface AnalyzeServices {

    @GET
    suspend fun testConnection(@Url endpoint: String): Response<ResponseBody>

    @POST
    suspend fun analyze(@Url endpoint: String, @Body body: RequestBody): Response<AnalyzeResultApi>
}
