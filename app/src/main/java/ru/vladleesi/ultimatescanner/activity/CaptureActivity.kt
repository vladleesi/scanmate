package ru.vladleesi.ultimatescanner.activity

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.ActivityCaptureBinding
import ru.vladleesi.ultimatescanner.model.AnalyzeState
import ru.vladleesi.ultimatescanner.repository.AnalyzeRepo
import java.lang.ref.WeakReference


class CaptureActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCaptureBinding.inflate(layoutInflater) }

    private val analyzeRepo by lazy { AnalyzeRepo(WeakReference(application)) }

    private lateinit var mDetector: FirebaseVisionBarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.tbToolbar.navigationIcon =
            ContextCompat.getDrawable(baseContext, R.drawable.ic_baseline_arrow_back_24)
        binding.tbToolbar.setNavigationOnClickListener { onBackPressed() }
        binding.tbToolbar.title = "Детальная информация"

        val uri = intent.getParcelableExtra<Uri>(CAPTURED_URI)

//        val bitmap = intent.getParcelableExtra<Bitmap>(CAPTURED_BITMAP)
        var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val matrix = Matrix()
        matrix.postRotate(90f)
        bitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        binding.ivCapturedImage.setImageBitmap(bitmap)
        bitmap?.let { runDetector(FirebaseVisionImage.fromBitmap(it)) }

        binding.mbSendForAnalyze.setOnClickListener {
            uri?.let {
                analyzeRepo.analyze(uri)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { state ->
                            when (state) {
                                is AnalyzeState.Success -> Toast.makeText(
                                    baseContext,
                                    "Success",
                                    Toast.LENGTH_SHORT
                                ).show()
                                is AnalyzeState.Loading -> Toast.makeText(
                                    baseContext,
                                    "Loading",
                                    Toast.LENGTH_SHORT
                                ).show()
                                is AnalyzeState.Error -> showErrorToast()
                                else -> showErrorToast()
                            }
                        },
                        { showErrorToast() })
            }
        }
    }

    private fun runDetector(
        image: FirebaseVisionImage
    ) {

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
            .build()

        FirebaseApp.initializeApp(applicationContext)
        mDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        mDetector.detectInImage(image)
            .addOnSuccessListener { processResult(it) }
            .addOnFailureListener { processFailure(it) }
    }

    private fun processResult(
        barcodeResults: List<FirebaseVisionBarcode>
    ) {
        barcodeResults.forEach { barcode ->

            val raw = barcode.rawValue

            // TODO: Отобразить область распознавания на экране
            val rect = barcode.boundingBox
            val corner = barcode.cornerPoints

            val type = getType(barcode)
            val value = raw.toString()
            Log.e(TAG, value)

            val result = "$type: $value"
            binding.tvValue.text = result

            analyzeRepo.saveToHistory(type, value).subscribe()
        }
    }

    private fun processFailure(it: Exception) {
        Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
    }

    private fun getType(barcode: FirebaseVisionBarcode): String {
        return when (barcode.valueType) {
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
                Toast.makeText(
                    baseContext,
                    "Unknown value type: ${barcode.valueType}",
                    Toast.LENGTH_LONG
                ).show()
                "Unknown value type"
            }
        }
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

        const val CAPTURED_BITMAP = "capturedBitmap"
        const val CAPTURED_URI = "capturedUri"
    }
}