package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import ru.vladleesi.ultimatescanner.Constants
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.FragmentCameraBinding
import ru.vladleesi.ultimatescanner.extensions.getDrawableCompat
import ru.vladleesi.ultimatescanner.extensions.invisible
import ru.vladleesi.ultimatescanner.extensions.showToast
import ru.vladleesi.ultimatescanner.extensions.showWithAnim
import ru.vladleesi.ultimatescanner.ui.activity.CaptureActivity
import ru.vladleesi.ultimatescanner.ui.activity.MainActivity
import ru.vladleesi.ultimatescanner.ui.analyzer.AnalyzeProcess
import ru.vladleesi.ultimatescanner.ui.analyzer.CameraPreviewAnalyzer
import ru.vladleesi.ultimatescanner.ui.camera.CameraBindingHolder
import ru.vladleesi.ultimatescanner.ui.camera.CameraHelper
import ru.vladleesi.ultimatescanner.ui.fragments.TabFragment
import ru.vladleesi.ultimatescanner.ui.model.scan.ScanResult
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraTabFragment : TabFragment(), AnalyzeProcess, CameraBindingHolder {

    private lateinit var binding: FragmentCameraBinding

    private val parentActivity by lazy { parent<MainActivity>() }

    private val mOutputDirectory by lazy { context?.externalCacheDir }

    private lateinit var cameraExecutor: ExecutorService
    private var isDetectEnabled: Boolean = true
    private var isDetectRequired: Boolean = false
    private val barcodeMap: HashMap<String, String> = hashMapOf()

    private val cameraHelper by lazy {
        CameraHelper(
            WeakReference(context),
            this,
            CameraPreviewAnalyzer(WeakReference(context), this),
            this,
            mOutputDirectory
        )
    }

    private val defaultPreferences: SharedPreferences by lazy {
        getDefaultSharedPreferences(context)
    }

    override val pageTitleId: Int
        get() = R.string.page_title_camera

    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isSuccess ->
            if (isSuccess) {
                cameraHelper.startCamera()
                binding.fabCapture.showWithAnim()
            } else {
                showToast("Permissions not granted by user")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCameraBinding.bind(view)
        cameraExecutor = Executors.newSingleThreadExecutor()

        clearCacheDirectory()

        permissionResult.launch(Manifest.permission.CAMERA)

        binding.fabCapture.setOnClickListener { cameraHelper.takePhoto() }
    }

    private fun clearCacheDirectory() {
        mOutputDirectory?.listFiles()?.forEach { file -> file.delete() }
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
        binding.fabCapture.invisible()
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

        if (barcodeResults.isNotEmpty() && isDetectEnabled && isDetectRequired) {
            isDetectEnabled = false
            cameraHelper.takePhoto()
            parentActivity?.soundMaker?.playSound()
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
        binding.ivFlashlight.setImageDrawable(activity?.getDrawableCompat(R.drawable.ic_baseline_flashlight_on_24))
    }

    override fun flashlightOff() {
        binding.ivFlashlight.setImageDrawable(activity?.getDrawableCompat(R.drawable.ic_baseline_flashlight_off_24))
    }

    companion object {
        const val TAG = "CameraFragment"

        fun newInstance() = CameraTabFragment().apply { arguments = Bundle() }
    }
}