package dev.vladleesi.scanmate.ui.fragments.tabs

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import dev.vladleesi.scanmate.Constants
import dev.vladleesi.scanmate.R
import dev.vladleesi.scanmate.databinding.FragmentCameraBinding
import dev.vladleesi.scanmate.extensions.addOnPageSelected
import dev.vladleesi.scanmate.extensions.invisible
import dev.vladleesi.scanmate.extensions.requestPermissionIfNeed
import dev.vladleesi.scanmate.extensions.showToast
import dev.vladleesi.scanmate.extensions.visible
import dev.vladleesi.scanmate.ui.activity.CaptureActivity
import dev.vladleesi.scanmate.ui.activity.MainActivity
import dev.vladleesi.scanmate.ui.adapter.CameraTabAdapter
import dev.vladleesi.scanmate.ui.analyzer.AnalyzeProcess
import dev.vladleesi.scanmate.ui.analyzer.CameraPreviewAnalyzer
import dev.vladleesi.scanmate.ui.camera.CameraHelper
import dev.vladleesi.scanmate.ui.camera.OnDetectListener
import dev.vladleesi.scanmate.ui.fragments.TabFragment
import dev.vladleesi.scanmate.ui.model.scan.ScanResult
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraTabFragment :
    TabFragment(R.layout.fragment_camera),
    AnalyzeProcess,
    OnDetectListener {

    private lateinit var binding: FragmentCameraBinding

    private val parentActivity by lazy { parent<MainActivity>() }

    private val mOutputDirectory by lazy { context?.externalCacheDir }

    private lateinit var cameraExecutor: ExecutorService
    private val barcodeMap: HashMap<String, String> = hashMapOf()

    private val tabAdapter by lazy { CameraTabAdapter(childFragmentManager) }

    private val cameraHelper by lazy {
        CameraHelper(
            WeakReference(context),
            this,
            parentActivity?.cameraProviderFuture,
            CameraPreviewAnalyzer(WeakReference(context), this),
            this,
            mOutputDirectory
        )
    }

    private val mCameraInitLiveData = MutableLiveData<Boolean>()
    private val mStartedLiveData = MutableLiveData<Boolean>()
    private var isAutoDetectEnable = false

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

        binding.fabCapture.setOnClickListener { cameraHelper.takePhoto() }

        requireContext().requestPermissionIfNeed(permissionResult, Manifest.permission.CAMERA) {
            setCameraInit(true)
        }

        mCameraInitLiveData.observe(viewLifecycleOwner) { isCameraInitNeed ->
            if (isCameraInitNeed) {
                cameraHelper.startCamera(binding.viewFinder) {
                    mStartedLiveData.observe(viewLifecycleOwner) { onResume ->
                        if (onResume) cameraHelper.bind()
                        else cameraHelper.unbindAll()
                    }
                }
            }
        }
        binding.cameraPager.adapter = tabAdapter

        binding.cameraPager.addOnPageSelected(infinityScroll = false, ::selectMode)

        when (CameraModeHolder.cameraMode) {
            CameraMode.AUTO_MODE, CameraMode.UNDEFINED_MODE -> binding.cameraPager.currentItem = 0
            CameraMode.MANUAL_MODE -> binding.cameraPager.currentItem = 1
        }

        mStartedLiveData.observe(viewLifecycleOwner) { onResume ->
            isAutoDetectEnable = onResume && CameraModeHolder.cameraMode == CameraMode.AUTO_MODE
        }
    }

    override fun onResume() {
        super.onResume()
        mStartedLiveData.value = true
    }

    override fun onPause() {
        super.onPause()
        mStartedLiveData.value = false
    }

    private fun selectMode(position: Int) {
        when (position) {
            0 -> {
                binding.fabCapture.hide()
                isAutoDetectEnable = true
                binding.fabCaptureContainer.invisible()
                CameraModeHolder.cameraMode = CameraMode.AUTO_MODE
            }
            1 -> {
                binding.fabCapture.show()
                isAutoDetectEnable = false
                binding.fabCaptureContainer.visible()
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
        mStartedLiveData.observe(viewLifecycleOwner) { onResume ->
            if (onResume) parentActivity?.selectTab(position, fromCamera = true)
        }
    }

    private fun clearCacheDirectory() {
        mOutputDirectory?.listFiles()?.forEach { file -> file.delete() }
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

        if (barcodeResults.isNotEmpty() &&
            CameraModeHolder.cameraMode == CameraMode.AUTO_MODE &&
            isAutoDetectEnable
        ) {
            isAutoDetectEnable = false
            parentActivity?.playSound()
            cameraHelper.takePhoto()
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

    companion object {
        const val TAG = "CameraFragment"

        fun newInstance() = CameraTabFragment()
    }
}
