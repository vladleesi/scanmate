package ru.vladleesi.ultimatescanner.data.interceptor

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import ru.vladleesi.ultimatescanner.utils.PreferencesHelper

class BaseUrlInterceptor(private val preferencesHelper: PreferencesHelper) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val baseUrl = preferencesHelper.baseUrl()

        var request = chain.request()

        baseUrl?.let {
            val host = baseUrl.toHttpUrlOrNull() ?: return chain.proceed(request)

            val url = request.url.newBuilder()
                .scheme(host.scheme)
                .host(host.host)
                .build()

            request = request.newBuilder()
                .url(url)
                .build()
        }

        return chain.proceed(request)
    }
}
