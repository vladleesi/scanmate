package ru.vladleesi.ultimatescanner.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnalyzeResultApi(@field:Json(name = "data") val data: String)
