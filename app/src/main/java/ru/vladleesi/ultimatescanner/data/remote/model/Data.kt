package ru.vladleesi.ultimatescanner.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Data(
    @field:Json(name = "code") val code: String?,
    @field:Json(name = "description") val description: String?
)
