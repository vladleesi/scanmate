package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.lifecycle.MutableLiveData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import ru.vladleesi.ultimatescanner.Constants
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.FragmentCameraBinding
import ru.vladleesi.ultimatescanner.extensions.addOnPageSelected
import ru.vladleesi.ultimatescanner.extensions.getDrawableCompat
import ru.vladleesi.ultimatescanner.extensions.requestPermissionIfNeed
import ru.vladleesi.ultimatescanner.extensions.showToast
import ru.vladleesi.ultimatescanner.ui.activity.CaptureActivity
import ru.vladleesi.ultimatescanner.ui.activity.MainActivity
import ru.vladleesi.ultimatescanner.ui.adapter.CameraTabAdapter
import ru.vladleesi.ultimatescanner.ui.analyzer.AnalyzeProcess
import ru.vladleesi.ultimatescanner.ui.analyzer.CameraPreviewAnalyzer
import ru.vladleesi.ultimatescanner.ui.camera.CameraBindingHolder
import ru.vladleesi.ultimatescanner.ui.camera.CameraHelper
import ru.vladleesi.ultimatescanner.ui.fragments.TabFragment
import ru.vladleesi.ultimatescanner.ui.model.scan.ScanResult
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraTabFragment :
    TabFragment(R.layout.fragment_camera),
    AnalyzeProcess,
    CameraBindingHolder {

    private lateinit var binding: FragmentCameraBinding

    private val parentActivity by lazy { parent<MainActivity>() }

    private val mOutputDirectory by lazy { context?.externalCacheDir }

    private lateinit var cameraExecutor: ExecutorService
    private var isDetectEnabled: Boolean = true
    private val barcodeMap: HashMap<String, String> = hashMapOf()

    private val tabAdapter by lazy { CameraTabAdapter(childFragmentManager) }

    private val fabCapture: FloatingActionButton by lazy { binding.fabCapture }

    private val cameraHelper by lazy {
        CameraHelper(
            WeakReference(context),
            this,
            CameraPreviewAnalyzer(WeakReference(context), this),
            this,
            mOutputDirectory
        )
    }

    private val mCameraInitLiveData = MutableLiveData<Boolean>()

    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isSuccess ->
            if (!isSuccess) {
                showToast("Permissions not granted by user")
            }
            setCameraInit(isSuccess)
        }

    private fun setCameraInit(isInitNeed: Boolean) {
        mCameraInitLiveData.value = isInitNeed
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCameraBinding.bind(view)
        cameraExecutor = Executors.newSingleThreadExecutor()

        clearCacheDirectory()

        fabCapture.setOnClickListener { cameraHelper.takePhoto() }

        requireContext().requestPermissionIfNeed(permissionResult, Manifest.permission.CAMERA) {
            setCameraInit(true)
        }

        mCameraInitLiveData.observe(viewLifecycleOwner) { isCameraInitNeed ->
            if (isCameraInitNeed) {
                cameraHelper.startCamera()
            }
        }
        binding.cameraPager.adapter = tabAdapter
        binding.cameraTabs.setupWithViewPager(binding.cameraPager)

        binding.cameraPager.addOnPageSelected(infinityScroll = false, ::selectMode)

        when (CameraModeHolder.cameraMode) {
            CameraMode.AUTO_MODE, CameraMode.UNDEFINED_MODE -> binding.cameraPager.currentItem = 0
            CameraMode.MANUAL_MODE -> binding.cameraPager.currentItem = 1
        }
    }

    private fun selectMode(position: Int) {
        when (position) {
            0 -> {
                isDetectEnabled = true
                fabCapture.hide()
                CameraModeHolder.cameraMode = CameraMode.AUTO_MODE
            }
            1 -> {
                isDetectEnabled = false
                fabCapture.show()
                CameraModeHolder.cameraMode = CameraMode.MANUAL_MODE
            }
        }
        if (CameraModeHolder.currentFragment == TabFragments.CAMERA) {
            VoiceEventBus.toVoice(tabAdapter.getPageTitle(position).toString())
            binding.root.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    private fun clearCacheDirectory() {
        mOutputDirectory?.listFiles()?.forEach { file -> file.delete() }
    }

    override fun onStart() {
        super.onStart()
        isDetectEnabled = true
    }

    override fun onStop() {
        super.onStop()
        isDetectEnabled = false
        cameraHelper
    }

    override fun processResult(
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

        if (barcodeResults.isNotEmpty() && isDetectEnabled) {
            isDetectEnabled = false
            cameraHelper.takePhoto()
            parentActivity?.playSound()
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

    override fun onDetect(savedUri: Uri) {
        startActivity(
            Intent(context, CaptureActivity::class.java).apply {
                putExtra(Constants.CAPTURED_URI, savedUri)
                putExtra(Constants.BARCODE_MAP, barcodeMap)
            }
        )
        parentActivity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun processFailure(e: Exception) {
        showToast(e.message)
    }

    override fun getCameraView(): PreviewView = binding.viewFinder

    override fun getFlashlightView(): ImageView = binding.ivFlashlight

    override fun flashlightOn() {
        binding.ivFlashlight.setImageDrawable(
            activity?.getDrawableCompat(R.drawable.ic_baseline_flashlight_on_24)
        )
    }

    override fun flashlightOff() {
        binding.ivFlashlight.setImageDrawable(
            activity?.getDrawableCompat(R.drawable.ic_baseline_flashlight_off_24)
        )
    }

    companion object {
        const val TAG = "CameraFragment"

        fun newInstance() = CameraTabFragment()
    }
}
