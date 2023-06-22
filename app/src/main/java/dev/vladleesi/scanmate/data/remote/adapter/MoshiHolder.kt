package dev.vladleesi.scanmate.data.remote.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiHolder {

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Base64AdapterFactory())
            .build()
    }

    fun <T> getAdapter(type: Class<T>): JsonAdapter<T> {
        return moshi.adapter(type)
    }

    fun <T> getListAdapter(type: Class<T>): JsonAdapter<List<T>> {
        val listMyData = Types.newParameterizedType(List::class.java, type)
        return moshi.adapter(listMyData)
    }
}

inline fun <reified T> T.toJson(): String {
    return MoshiHolder.getAdapter(T::class.java).toJson(this)
}

inline fun <reified T> List<T>.toJson(): String {
    return MoshiHolder.getListAdapter(T::class.java).toJson(this)
}
