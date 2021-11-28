package ru.vladleesi.ultimatescanner.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import dagger.hilt.android.AndroidEntryPoint
import ru.vladleesi.ultimatescanner.Constants.BARCODE_MAP
import ru.vladleesi.ultimatescanner.Constants.CAMERA_PERMISSION_REQUEST
import ru.vladleesi.ultimatescanner.Constants.CAPTURED_URI
import ru.vladleesi.ultimatescanner.Constants.FILENAME_FORMAT
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.ActivityCameraPreviewBinding
import ru.vladleesi.ultimatescanner.extensions.allPermissionsGranted
import ru.vladleesi.ultimatescanner.extensions.getDrawableCompat
import ru.vladleesi.ultimatescanner.extensions.makeStatusBarTransparent
import ru.vladleesi.ultimatescanner.extensions.showToast
import ru.vladleesi.ultimatescanner.ui.accessibility.SoundMaker
import ru.vladleesi.ultimatescanner.ui.model.scan.ScanResult
import ru.vladleesi.ultimatescanner.utils.FileUtils
import ru.vladleesi.ultimatescanner.utils.ImageCompressUtils
import ru.vladleesi.ultimatescanner.utils.PermissionUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class CameraPreviewActivity : AppCompatActivity() {

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val binding by lazy { ActivityCameraPreviewBinding.inflate(layoutInflater) }

    private lateinit var imageCapture: ImageCapture

    private lateinit var screenResolution: Size

    private val mOutputDirectory by lazy { getOutputDirectory() }
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var mDetector: FirebaseVisionBarcodeDetector

    private var isDetectEnabled = true
    private val barcodeMap: HashMap<String, String> = hashMapOf()

    private val defaultPreferences: SharedPreferences by lazy {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(
            baseContext
        )
    }
    private var isDetectRequired: Boolean = false

    @Inject
    lateinit var soundMaker: SoundMaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())

        clearCacheDirectory()

        makeStatusBarTransparent()

        PermissionUtils.requestPermission(this)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.ivCloseApp.setOnClickListener { finish() }

        initBottomNavigationView()

        binding.fabCapture.setOnClickListener { takePhoto() }
    }

    override fun onStart() {
        super.onStart()
        isDetectRequired = defaultPreferences.getBoolean(
            getString(R.string.settings_auto_detect),
            false
        )
        isDetectEnabled = true
    }

    override fun onStop() {
        super.onStop()
        isDetectEnabled = false
    }

    private fun initBottomNavigationView() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_item_bottom_settings -> startActivity(
                    Intent(
                        baseContext,
                        SettingsActivity::class.java
                    )
                )
                R.id.menu_item_bottom_history -> startActivity(
                    Intent(
                        baseContext,
                        HistoryActivity::class.java
                    )
                )
            }
            false
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
//                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: ${photoFile.absoluteFile}"
                    showToast(msg)
                    Log.d(TAG, msg)

                    val compressed =
                        ImageCompressUtils.getCompressed(
                            baseContext,
                            photoFile.absolutePath,
                            screenResolution.width,
                            screenResolution.height
                        )

                    openDetails(Uri.fromFile(compressed))
                }
            }
        )
    }

    private fun openDetails(savedUri: Uri) {
        startActivity(
            Intent(baseContext, CaptureActivity::class.java).apply {
                putExtra(CAPTURED_URI, savedUri)
                putExtra(BARCODE_MAP, barcodeMap)
            }
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(baseContext)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
            screenResolution = Size(metrics.widthPixels, metrics.heightPixels)
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            val targetRotation = binding.viewFinder.display.rotation

            // Preview
            val preview = Preview.Builder()
//                .setTargetAspectRatio(screenAspectRatio)
                .setTargetResolution(screenResolution)
                .setTargetRotation(targetRotation)
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
//                .setTargetAspectRatio(screenAspectRatio)
                .setTargetResolution(screenResolution)
                .setTargetRotation(targetRotation)
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
//                .setTargetAspectRatio(screenAspectRatio)
                .setTargetResolution(screenResolution)
                .setTargetRotation(targetRotation)
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

                if (camera.cameraInfo.hasFlashUnit()) {
                    var isFlashlightOn = false
                    binding.ivFlashlight.setOnClickListener {
                        isFlashlightOn = !isFlashlightOn
                        camera.cameraControl.enableTorch(isFlashlightOn)
                        if (isFlashlightOn) {
                            binding.ivFlashlight.setImageDrawable(getDrawableCompat(R.drawable.ic_baseline_flashlight_off_24))
                        } else {
                            binding.ivFlashlight.setImageDrawable(getDrawableCompat(R.drawable.ic_baseline_flashlight_on_24))
                        }
                    }
                }

                val sensorRotationDegrees = camera.cameraInfo.sensorRotationDegrees
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
        }

        private fun getOutputDirectory(): File? = baseContext.externalCacheDir

        override fun onDestroy() {
            super.onDestroy()
            cameraExecutor.shutdown()
            FileUtils.clearImageCompressorCache(applicationContext)
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == CAMERA_PERMISSION_REQUEST) {
                if (allPermissionsGranted()) {
                    startCamera()
                } else {
                    showToast("Permissions not granted by the user.")
                    finish()
                }
            }
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
        }

        private fun runDetector(
            image: FirebaseVisionImage,
            capturedImageWidth: Int,
            capturedImageHeight: Int
        ) {

            val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                .build()

            FirebaseApp.initializeApp(applicationContext).let {
                mDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
                mDetector.detectInImage(image)
                    .addOnSuccessListener { processResult(it, capturedImageWidth, capturedImageHeight) }
                    .addOnFailureListener { processFailure(it) }
            }
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
                barcodeMap[getType(it.valueType)] = it.rawValue ?: ""
            }

            if (barcodeResults.isNotEmpty() && isDetectEnabled && isDetectRequired) {
                isDetectEnabled = false
                takePhoto()
                soundMaker.playSound()
            }
        }

        private fun getType(barcodeValueType: Int): String {
            return when (barcodeValueType) {
                FirebaseVisionBarcode.TYPE_URL -> {
                    Log.e(TAG, "TYPE_URL")
                    "TYPE_URL"
                }
                FirebaseVisionBarcode.TYPE_TEXT -> {
                    Log.e(TAG, "TYPE_TEXT")
                    "TYPE_TEXT"
                }
                FirebaseVisionBarcode.TYPE_CALENDAR_EVENT -> {
                    Log.e(TAG, "TYPE_CALENDAR_EVENT")
                    "TYPE_CALENDAR_EVENT"
                }
                FirebaseVisionBarcode.TYPE_CONTACT_INFO -> {
                    Log.e(TAG, "TYPE_CONTACT_INFO")
                    "TYPE_CONTACT_INFO"
                }
                FirebaseVisionBarcode.TYPE_EMAIL -> {
                    Log.e(TAG, "TYPE_EMAIL")
                    "TYPE_EMAIL"
                }
                FirebaseVisionBarcode.TYPE_PHONE -> {
                    Log.e(TAG, "TYPE_PHONE")
                    "TYPE_PHONE"
                }
                FirebaseVisionBarcode.TYPE_WIFI -> {
                    Log.e(TAG, "TYPE_WIFI")
                    "TYPE_WIFI"
                }
                FirebaseVisionBarcode.TYPE_GEO -> {
                    Log.e(TAG, "TYPE_GEO")
                    "TYPE_GEO"
                }
                FirebaseVisionBarcode.TYPE_UNKNOWN -> {
                    Log.e(TAG, "TYPE_UNKNOWN")
                    "TYPE_UNKNOWN"
                }
                FirebaseVisionBarcode.TYPE_DRIVER_LICENSE -> {
                    Log.e(TAG, "TYPE_DRIVER_LICENSE")
                    "TYPE_DRIVER_LICENSE"
                }
                FirebaseVisionBarcode.TYPE_PRODUCT -> {
                    Log.e(TAG, "TYPE_PRODUCT")
                    "TYPE_PRODUCT"
                }
                FirebaseVisionBarcode.TYPE_SMS -> {
                    Log.e(TAG, "TYPE_SMS")
                    "TYPE_SMS"
                }
                FirebaseVisionBarcode.TYPE_ISBN -> {
                    Log.e(TAG, "TYPE_ISBN")
                    "TYPE_ISBN"
                }
                else -> {
                    "Unknown value type"
                }
            }
        }

        private fun processFailure(it: Exception) {
            showToast(it.message)
        }

        private companion object {
            private const val TAG = "CameraXBasic"

            private const val RATIO_4_3_VALUE = 4.0 / 3.0
            private const val RATIO_16_9_VALUE = 16.0 / 9.0
        }
    }
    