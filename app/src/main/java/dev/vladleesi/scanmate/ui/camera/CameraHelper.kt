package dev.vladleesi.scanmate.ui.camera

import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import dev.vladleesi.scanmate.Constants
import dev.vladleesi.scanmate.extensions.showToast
import dev.vladleesi.scanmate.ui.analyzer.CameraPreviewAnalyzer
import dev.vladleesi.scanmate.ui.fragments.tabs.CameraTabFragment
import dev.vladleesi.scanmate.utils.ImageCompressUtils
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

class CameraHelper(
    private val weakContext: WeakReference<Context>,
    private val lifecycleOwner: LifecycleOwner,
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>?,
    private val cameraPreviewAnalyzer: CameraPreviewAnalyzer,
    private val onDetectListener: OnDetectListener,
    private val outputDirectory: File?
) {

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val mainExecutor = ContextCompat.getMainExecutor(weakContext.get()!!)

    // Used to bind the lifecycle of cameras to the lifecycle owner
    private val cameraProvider: ProcessCameraProvider? by lazy { cameraProviderFuture?.get() }
    private var camera: Camera? = null
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var preview: Preview

    // Select back camera as a default
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private lateinit var imageCapture: ImageCapture
    private lateinit var screenResolution: Size

    fun startCamera(previewView: PreviewView, callback: () -> Unit) {
        cameraProviderFuture?.addListener({

            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also {
                previewView.display.getRealMetrics(it)
            }
            screenResolution = Size(metrics.widthPixels, metrics.heightPixels)
//            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            val targetRotation = previewView.display.rotation

            // Preview
            preview = Preview.Builder()
//                .setTargetAspectRatio(screenAspectRatio)
                .setTargetResolution(screenResolution)
                .setTargetRotation(targetRotation)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
//                .setTargetAspectRatio(screenAspectRatio)
                .setTargetResolution(screenResolution)
                .setTargetRotation(targetRotation)
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
//                .setTargetAspectRatio(screenAspectRatio)
                .setTargetResolution(screenResolution)
                .setTargetRotation(targetRotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, cameraPreviewAnalyzer)
                }

            try {
                unbindAll()
                callback()
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

                    onDetectListener.onDetect(Uri.fromFile(compressed))
                }
            }
        )
    }

    /** Unbind use cases before rebinding */
    fun unbindAll() {
        cameraProvider?.unbindAll()
    }

    /** Bind use cases to camera */
    fun bind() {
        camera = cameraProvider?.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
        )
    }
}
