package ru.vladleesi.ultimatescanner.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.utils.PermissionUtils

class CameraActivity : AppCompatActivity() {

    private var mCameraManager: CameraManager? = null
    private var mCameraService: CameraService? = null

    private lateinit var mTextureView: TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initCamera()
    }

    private fun initCamera() {

        mTextureView = findViewById(R.id.camera_preview)
        mCameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager?

        mCameraManager?.cameraIdList?.forEach { cameraId ->
            val id = cameraId.toIntOrNull()
            val cameraCharacteristics = mCameraManager?.getCameraCharacteristics(cameraId)

            when (cameraCharacteristics?.get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_FRONT -> {
                }
                CameraCharacteristics.LENS_FACING_BACK -> {
                }
            }

            val cameraOutputStreamConfigurations =
                cameraCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val jpegSupportedResolutions =
                cameraOutputStreamConfigurations?.getOutputSizes(ImageFormat.JPEG)
            jpegSupportedResolutions?.forEach { resolution ->

            }

            mCameraService = CameraService(cameraId)
            if (mCameraService?.isOpen == false) {
                mCameraService?.openCamera(this)
            }
        }
    }

    inner class CameraService(
        private val mCameraID: String
    ) {
        private var mCameraDevice: CameraDevice? = null
        private lateinit var mCaptureSession: CameraCaptureSession

        val isOpen: Boolean
            get() = mCameraDevice != null

        private val mCameraCallback: CameraDevice.StateCallback =
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    mCameraDevice = camera
                    Log.i(TAG, "Open camera  with id:" + mCameraDevice!!.id)
                    createCameraPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    mCameraDevice!!.close()
                    Log.i(TAG, "disconnect camera  with id:" + mCameraDevice!!.id)
                    mCameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.i(TAG, "error! camera id:" + camera.id + " error:" + error)
                }
            }

        private fun createCameraPreviewSession() {
            val texture: SurfaceTexture? = mTextureView.surfaceTexture
            val surface = Surface(texture)
            try {
                val builder: CaptureRequest.Builder =
                    mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) ?: return
                builder.addTarget(surface)
                mCameraDevice?.createCaptureSession(
                    listOf(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            mCaptureSession = session
                            try {
                                mCaptureSession.setRepeatingRequest(builder.build(), null, null)
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }, null
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        fun openCamera(activity: Activity) {
            if (ActivityCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mCameraManager?.openCamera(mCameraID, mCameraCallback, null)
            } else {
                PermissionUtils.requestPermission(activity)
            }
        }

        fun closeCamera() {
            if (mCameraDevice != null) {
                mCameraDevice?.close()
                mCameraDevice = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCameraService != null) {
            mCameraService?.closeCamera()
            mCameraService = null
        }
    }

    companion object {
        const val TAG = "CameraActivity"

        const val REQUEST_CODE_FROM_CAMERA = 96
    }
}