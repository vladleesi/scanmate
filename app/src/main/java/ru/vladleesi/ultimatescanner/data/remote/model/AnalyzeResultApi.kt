package ru.vladleesi.ultimatescanner.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnalyzeResultApi(
    @field:Json(name = "data") val data: String?,
    @field:Json(name = "errors") val errors: List<String>?,
    @field:Json(name = "img_data") val imgData: String?,
    @field:Json(name = "ocr_data") val ocr_data: OcrData?,
    @field:Json(name = "products") val products: List<Product>?
)
