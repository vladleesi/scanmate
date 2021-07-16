package ru.vladleesi.ultimatescanner.data.remote.model

import com.squareup.moshi.Json

data class Product(
    @field:Json(name = "title") val title: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "logo") val logo: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (title != other.title) return false
        if (url != other.url) return false
        if (!logo.contentEquals(other.logo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + logo.contentHashCode()
        return result
    }
}
