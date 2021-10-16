package ru.vladleesi.ultimatescanner.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OcrBox(
    @field:Json(name = "p1") val p1: IntArray,
    @field:Json(name = "p2") val p2: IntArray,
    @field:Json(name = "p3") val p3: IntArray,
    @field:Json(name = "p4") val p4: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OcrBox

        if (!p1.contentEquals(other.p1)) return false
        if (!p2.contentEquals(other.p2)) return false
        if (!p3.contentEquals(other.p3)) return false
        if (!p4.contentEquals(other.p4)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = p1.contentHashCode()
        result = 31 * result + p2.contentHashCode()
        result = 31 * result + p3.contentHashCode()
        result = 31 * result + p4.contentHashCode()
        return result
    }
}
