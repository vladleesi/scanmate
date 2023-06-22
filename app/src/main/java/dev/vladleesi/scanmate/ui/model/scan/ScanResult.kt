package dev.vladleesi.scanmate.ui.model.scan

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class ScanResult(
    val barcodes: List<FirebaseVisionBarcode>,
    val imageWidth: Int,
    val imageHeight: Int
)
