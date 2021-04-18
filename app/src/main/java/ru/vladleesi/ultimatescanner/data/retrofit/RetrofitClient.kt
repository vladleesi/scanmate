package ru.vladleesi.ultimatescanner.data.retrofit

import android.content.Context
import androidx.preference.PreferenceManager
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.vladleesi.ultimatescanner.data.retrofit.converter.NullOnEmptyConverterFactory
import ru.vladleesi.ultimatescanner.data.retrofit.services.AnalyzeServices
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
            .addConverterFactory(GsonConverterFactory.create())
            // TODO: All retrofit requests with Schedulers.io is ok?
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
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