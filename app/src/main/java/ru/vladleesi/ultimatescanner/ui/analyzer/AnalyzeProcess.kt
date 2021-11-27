package ru.vladleesi.ultimatescanner.ui.analyzer

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

interface AnalyzeProcess {
    fun processResult(
        barcodeResults: List<FirebaseVisionBarcode>,
        capturedImageWidth: Int,
        capturedImageHeight: Int
    )

    fun processFailure(e: Exception)
}
