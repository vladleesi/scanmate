package dev.vladleesi.scanmate.data.interceptor

import dev.vladleesi.scanmate.utils.PreferencesHelper
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

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
