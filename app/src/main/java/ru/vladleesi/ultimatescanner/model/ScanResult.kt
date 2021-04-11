package ru.vladleesi.ultimatescanner.model

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class ScanResult(
    val barcodes: List<FirebaseVisionBarcode>,
    val imageWidth: Int,
    val imageHeight: Int
)