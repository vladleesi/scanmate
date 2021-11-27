package ru.vladleesi.ultimatescanner.ui.camera

import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import ru.vladleesi.ultimatescanner.Constants
import ru.vladleesi.ultimatescanner.extensions.showToast
import ru.vladleesi.ultimatescanner.ui.analyzer.CameraPreviewAnalyzer
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.CameraTabFragment
import ru.vladleesi.ultimatescanner.utils.ImageCompressUtils
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class CameraHelper(
    private val weakContext: WeakReference<Context>,
    private val lifecycleOwner: LifecycleOwner,
    private val cameraPreviewAnalyzer: CameraPreviewAnalyzer,
    private val cameraBindingHolder: CameraBindingHolder,
    private val outputDirectory: File?
) {

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val mainExecutor = ContextCompat.getMainExecutor(weakContext.get())
    private val cameraProviderFuture =
        weakContext.get()?.let { ProcessCameraProvider.getInstance(it) }

    private lateinit var imageCapture: ImageCapture
    private lateinit var screenResolution: Size

    fun startCamera() {
        cameraProviderFuture?.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also {
                cameraBindingHolder.getCameraView().display.getRealMetrics(it)
            }
            screenResolution = Size(metrics.widthPixels, metrics.heightPixels)
//            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            val targetRotation = cameraBindingHolder.getCameraView().display.rotation

            // Preview
            val preview = Preview.Builder()
//                .setTargetAspectRatio(screenAspectRatio)
                .setTargetResolution(screenResolution)
                .setTargetRotation(targetRotation)
                .build()
                .also {
                    it.setSurfaceProvider(cameraBindingHolder.getCameraView().surfaceProvider)
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
                    it.setAnalyzer(cameraExecutor, cameraPreviewAnalyzer)
                }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
                )

                if (camera.cameraInfo.hasFlashUnit()) {
                    var isFlashlightOn = false
                    cameraBindingHolder.getFlashlightView().setOnClickListener {
                        isFlashlightOn = !isFlashlightOn
                        camera.cameraControl.enableTorch(isFlashlightOn)
                        if (isFlashlightOn) {
                            cameraBindingHolder.flashlightOff()
                        } else {
                            cameraBindingHolder.flashlightOn()
                        }
                    }
                }
                val sensorRotationDegrees = camera.cameraInfo.sensorRotationDegrees
            } catch (exc: Exception) {
                Log.e(CameraTabFragment.TAG, "Use case binding failed", exc)
            }
        }, mainExecutor)
    }

    fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        if (!::imageCapture.isInitialized) {
            return
        }

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(Constants.FILENAME_FORMAT, Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(CameraTabFragment.TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: ${photoFile.absoluteFile}"
                    weakContext.get()?.showToast(msg)
                    Log.d(CameraTabFragment.TAG, msg)

                    val compressed =
                        ImageCompressUtils.getCompressed(
                            weakContext.get(),
                            photoFile.absolutePath,
                            screenResolution.width,
                            screenResolution.height
                        )

                    cameraBindingHolder.onDetect(Uri.fromFile(compressed))
                }
            }
        )
    }
}
