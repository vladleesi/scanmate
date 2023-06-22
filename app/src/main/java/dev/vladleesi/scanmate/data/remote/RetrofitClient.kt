package dev.vladleesi.scanmate.data.remote

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dev.vladleesi.scanmate.data.interceptor.BaseUrlInterceptor
import dev.vladleesi.scanmate.data.remote.adapter.MoshiHolder
import dev.vladleesi.scanmate.data.remote.converter.NullOnEmptyConverterFactory
import dev.vladleesi.scanmate.data.remote.services.AnalyzeServices
import dev.vladleesi.scanmate.utils.PreferencesHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(private val preferencesHelper: PreferencesHelper) {

    @Throws(IllegalArgumentException::class)
    private fun buildClient(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(MoshiConverterFactory.create(MoshiHolder.moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient())
            .build()
    }

    // OkHttpClient with interceptors
    private fun okHttpClient(): OkHttpClient {
        val timeout = 30L
        return OkHttpClient.Builder()
            .readTimeout(timeout, TimeUnit.SECONDS)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .callTimeout(timeout, TimeUnit.SECONDS)
            .addInterceptor(BaseUrlInterceptor(preferencesHelper))
            .addInterceptor(
                HttpLoggingInterceptor().also {
                    it.level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    @Throws(IllegalArgumentException::class)
    fun getAnalyzeService(baseUrl: String): AnalyzeServices {
        return buildClient(baseUrl).create(AnalyzeServices::class.java)
    }
}
