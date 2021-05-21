package ru.vladleesi.ultimatescanner.ui.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import ru.vladleesi.ultimatescanner.databinding.ActivityCaptureBinding
import ru.vladleesi.ultimatescanner.ui.model.state.Result
import ru.vladleesi.ultimatescanner.utils.FileUtils
import java.lang.ref.WeakReference


class CaptureActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCaptureBinding.inflate(layoutInflater) }

    private val analyzeRepo by lazy { AnalyzeRepo(WeakReference(application)) }

    private val uri by lazy { intent.getParcelableExtra<Uri>(CAPTURED_URI) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initToolbar()

        var bitmap = BitmapFactory.decodeFile(uri?.let { FileUtils.getPathFrom(baseContext, it) })
        Matrix().apply {
            postRotate(90f)
            bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                this,
                true
            )
        }
        binding.ivCapturedImage.setImageBitmap(bitmap)
//        bitmap?.let { runDetector(FirebaseVisionImage.fromBitmap(it)) }

        val barcodeSet =
            intent.getSerializableExtra(CameraPreviewActivity.BARCODE_SET_VALUE) as? HashSet<String>
        barcodeSet?.let {
            val resultStringBuilder = StringBuilder()
            it.forEach { result ->
                if (resultStringBuilder.isNotEmpty()) {
                    resultStringBuilder.append("\n\n")
                }
                resultStringBuilder.append(result)
            }
            binding.tvValue.text = resultStringBuilder.toString()
        }

        binding.mbSendForAnalyze.setOnClickListener {
            uri?.let {
                val handler = CoroutineExceptionHandler { _, throwable ->
                    lifecycleScope.launch {
                        Toast.makeText(
                            baseContext,
                            "ERROR: ${throwable.message ?: throwable.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                GlobalScope.launch(handler) {
                    val state = analyzeRepo.analyze(it, barcodeSet?.toTypedArray())
                    lifecycleScope.launch {
                        when (state) {
                            is Result.Success -> Toast.makeText(
                                baseContext,
                                state.data,
                                Toast.LENGTH_SHORT
                            ).show()
                            is Result.Loading -> Toast.makeText(
                                baseContext,
                                "Loading",
                                Toast.LENGTH_SHORT
                            ).show()
                            is Result.Error -> showErrorToast()
                        }
                    }
                }
            }
        }
    }

    private fun initToolbar() {
        binding.tbToolbar.navigationIcon =
            ContextCompat.getDrawable(baseContext, R.drawable.ic_baseline_arrow_back_24)
        binding.tbToolbar.setNavigationOnClickListener { onBackPressed() }
        binding.tbToolbar.title = "Детальная информация"
    }

    private fun showErrorToast() {
        Toast.makeText(
            baseContext,
            "ERROR: Sending photo have not been complete",
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val TAG = "CaptureActivity"

        const val CAPTURED_URI = "capturedUri"

        fun getType(barcodeValueType: Int): String {
            return when (barcodeValueType) {
                FirebaseVisionBarcode.TYPE_URL -> {
//                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.rawValue)))
                    Log.e(TAG, "TYPE_URL")
                    "TYPE_URL"
                }
                FirebaseVisionBarcode.TYPE_TEXT -> {
//                    Toast.makeText(baseContext, item.rawValue, Toast.LENGTH_SHORT).show()
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
//                    Toast.makeText(
//                        baseContext,
//                        "Unknown value type: ${barcode.valueType}",
//                        Toast.LENGTH_LONG
//                    ).show()
                    "Unknown value type"
                }
            }
        }
    }
}