package ru.vladleesi.ultimatescanner.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.ActivityCameraPreviewBinding
import ru.vladleesi.ultimatescanner.extensions.makeStatusBarTransparent
import ru.vladleesi.ultimatescanner.ui.model.scan.ScanResult
import ru.vladleesi.ultimatescanner.utils.FileUtils
import ru.vladleesi.ultimatescanner.utils.PermissionUtils
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class CameraPreviewActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCameraPreviewBinding.inflate(layoutInflater) }

    private lateinit var imageCapture: ImageCapture

    private val mOutputDirectory by lazy { getOutputDirectory() }
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var mDetector: FirebaseVisionBarcodeDetector

    private val barcodeMap: HashMap<String, String> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        clearCacheDirectory()

        makeStatusBarTransparent()

        PermissionUtils.requestPermission(this)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.ivCloseApp.setOnClickListener { finish() }

        binding.fabOpenSettings.setOnClickListener {
            startActivity(Intent(baseContext, SettingsActivity::class.java))
        }

        binding.fabCapture.setOnClickListener { takePhoto() }

        binding.fabOpenHistory.setOnClickListener {
            startActivity(Intent(baseContext, HistoryActivity::class.java))
        }
    }

    private fun clearCacheDirectory() {
        mOutputDirectory?.listFiles()?.forEach {
            it.delete()
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        if (!::imageCapture.isInitialized) {
            return
        }

        // Create time-stamped output file to hold the image
        val photoFile = File(
            mOutputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    startActivity(Intent(baseContext, CaptureActivity::class.java).apply {
                        putExtra(CaptureActivity.CAPTURED_URI, savedUri)
                        putExtra(BARCODE_MAP, barcodeMap)
                    })
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(baseContext)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, CameraPreviewAnalyzer())
                }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

                // TODO: How to delivery degrees to CameraPreviewAnalyzer?
                val degrees = camera.cameraInfo.sensorRotationDegrees
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File? {
//        val mediaDir = externalMediaDirs.firstOrNull()?.let {
//            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
//        }
//        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
        return baseContext.externalCacheDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        FileUtils.clearImageCompressorCache(WeakReference(applicationContext))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    baseContext,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun runDetector(
        image: FirebaseVisionImage,
        capturedImageWidth: Int,
        capturedImageHeight: Int
    ) {

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
            .build()

        FirebaseApp.initializeApp(applicationContext)
        mDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        mDetector.detectInImage(image)
            .addOnSuccessListener { processResult(it, capturedImageWidth, capturedImageHeight) }
            .addOnFailureListener { processFailure(it) }
    }

    private fun processResult(
        barcodeResults: List<FirebaseVisionBarcode>,
        capturedImageWidth: Int,
        capturedImageHeight: Int
    ) {
        binding.boBarcodeOverlay.overlay(
            ScanResult(
                barcodeResults,
                capturedImageWidth,
                capturedImageHeight
            )
        )

        if (barcodeMap.isNotEmpty()) {
            barcodeMap.clear()
        }
        barcodeResults.forEach {
            barcodeMap[CaptureActivity.getType(it.valueType)] = it.rawValue ?: ""
        }
    }

    private fun processFailure(it: Exception) {
        Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = width.coerceAtLeast(height).toDouble() / width.coerceAtMost(height)
        if (kotlin.math.abs(previewRatio - RATIO_4_3_VALUE) <= kotlin.math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    inner class CameraPreviewAnalyzer : ImageAnalysis.Analyzer {

        private var isBusy = AtomicBoolean(false)

        private fun getRotationDegrees(degrees: Int): Int = when (degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        override fun analyze(image: ImageProxy) {

            if (isBusy.compareAndSet(false, true)) {
                val mediaImage = image.image
                if (mediaImage != null) {
                    val visionImage = FirebaseVisionImage.fromMediaImage(
                        mediaImage,
                        getRotationDegrees(90)
//                    image.imageInfo.rotationDegrees
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
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyyMMddHHmmssSSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        const val REQUEST_CODE_FROM_CAMERA = 96
        const val CAMERA_PERMISSION_REQUEST = 234

        const val BARCODE_MAP = "barcodeSetValue"

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}
