package ru.vladleesi.ultimatescanner.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OcrBox(
    @field:Json(name = "p1") val p1: OcrPoint,
    @field:Json(name = "p2") val p2: OcrPoint,
    @field:Json(name = "p3") val p3: OcrPoint,
    @field:Json(name = "p4") val p4: OcrPoint
)
