package ru.vladleesi.ultimatescanner.data.remote.adapter

import android.util.Base64
import com.squareup.moshi.*
import java.lang.reflect.Type


class Base64AdapterFactory : JsonAdapter.Factory {

    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        val raw: Class<*> = Types.getRawType(type)
        if (raw == ByteArray::class.java) {
            return Base64Adapter().nullSafe()
        }

        return null
    }


    private class Base64Adapter : JsonAdapter<ByteArray?>() {

        override fun fromJson(reader: JsonReader): ByteArray? {
            val string = reader.nextString()
            return Base64.decode(string, Base64.NO_WRAP)
        }

        override fun toJson(writer: JsonWriter, value: ByteArray?) {
            val string = Base64.encodeToString(value, Base64.NO_WRAP)
            writer.value(string)
        }

    }
}