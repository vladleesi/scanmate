package ru.vladleesi.ultimatescanner.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OcrData(
    @field:Json(name = "bbox") val bbox: List<OcrBox>?,
    @field:Json(name = "text") val text: List<String>?
)
