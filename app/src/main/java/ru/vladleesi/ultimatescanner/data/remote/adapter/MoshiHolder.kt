package ru.vladleesi.ultimatescanner.data.remote.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiHolder {

    val moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    fun <T> getAdapter(type: Class<T>): JsonAdapter<T> {
        return moshi.adapter(type)
    }
}

inline fun <reified T> T.toJson(): String {
    return MoshiHolder.getAdapter(T::class.java).toJson(this)
}