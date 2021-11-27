package ru.vladleesi.ultimatescanner.ui.analyzer

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class CameraPreviewAnalyzer(
    private val weakContext: WeakReference<Context>,
    private val analyzeProcess: AnalyzeProcess
) : ImageAnalysis.Analyzer {

    private var isBusy = AtomicBoolean(false)

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {

        if (isBusy.compareAndSet(false, true)) {
            val mediaImage = image.image
            if (mediaImage != null) {
                val visionImage = FirebaseVisionImage.fromMediaImage(
                    mediaImage,
                    getRotationDegrees(image.imageInfo.rotationDegrees)
                )
                // Pass image to an ML Kit Vision API
                runDetector(visionImage, image.width, image.height)

                image.close()
                isBusy.set(false)
            }
        } else {
            image.close()
        }
    }

    private fun getRotationDegrees(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    private fun runDetector(
        image: FirebaseVisionImage,
        capturedImageWidth: Int,
        capturedImageHeight: Int
    ) {

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
            .build()

        weakContext.get()?.let { context ->
            FirebaseApp.initializeApp(context).let {
                val mDetector: FirebaseVisionBarcodeDetector =
                    FirebaseVision.getInstance().getVisionBarcodeDetector(options)
                mDetector.detectInImage(image)
                    .addOnSuccessListener {
                        analyzeProcess.processResult(
                            it,
                            capturedImageWidth,
                            capturedImageHeight
                        )
                    }
                    .addOnFailureListener { analyzeProcess.processFailure(it) }
            }
        }
    }
}
