package ru.vladleesi.ultimatescanner.data.remote

import android.content.Context
import androidx.preference.PreferenceManager
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.vladleesi.ultimatescanner.data.remote.adapter.MoshiHolder
import ru.vladleesi.ultimatescanner.data.remote.converter.NullOnEmptyConverterFactory
import ru.vladleesi.ultimatescanner.data.remote.services.AnalyzeServices
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class RetrofitClient {

    @Throws(IllegalArgumentException::class)
    private fun buildClient(context: WeakReference<Context>): Retrofit {
        val defaultUrl = "http://test.ru"
        var baseUrlFromPrefs = PreferenceManager.getDefaultSharedPreferences(context.get())
            .getString("url", defaultUrl)
        if (baseUrlFromPrefs?.endsWith("/") == false)
            baseUrlFromPrefs = "$baseUrlFromPrefs/"
        return Retrofit.Builder()
            .baseUrl(baseUrlFromPrefs ?: defaultUrl)
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
            .addInterceptor(HttpLoggingInterceptor().also {
                it.level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    @Throws(IllegalArgumentException::class)
    fun getAnalyzeService(context: WeakReference<Context>): AnalyzeServices {
        return buildClient(context).create(AnalyzeServices::class.java)
    }
}