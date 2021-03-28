package ru.vladleesi.ultimatescanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import ru.vladleesi.ultimatescanner.camera.CameraActivity
import ru.vladleesi.ultimatescanner.utils.PermissionUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var mDetector: FirebaseVisionBarcodeDetector? = null

    private var photoFromCameraTempFile: File? = null
    private lateinit var photoFromCameraTempUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionUtils.requestPermission(this)

        findViewById<Button>(R.id.b_open_custom_camera).setOnClickListener { openCustomCamera() }
        findViewById<Button>(R.id.b_open_device_camera).setOnClickListener { openDeviceCamera() }
    }

    private fun openCustomCamera() {
        startActivity(Intent(baseContext, CameraActivity::class.java))
    }

    private fun openDeviceCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        photoFromCameraTempFile = try {
            createTempFile(baseContext, ".jpg")
        } catch (ex: IOException) {
            Log.d(javaClass.canonicalName, ex.message ?: ex.toString())
            null
        }

        photoFromCameraTempFile?.also { file ->
            photoFromCameraTempUri = FileProvider.getUriForFile(
                baseContext,
                baseContext.packageName + ".provider",
                file
            )

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFromCameraTempUri)

            if (takePictureIntent.resolveActivity(baseContext.packageManager) != null)
                startActivityForResult(
                    takePictureIntent,
                    CameraActivity.REQUEST_CODE_FROM_CAMERA
                )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CameraActivity.REQUEST_CODE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            val file = photoFromCameraTempFile ?: File(photoFromCameraTempUri.toString())
            val bitmap = BitmapFactory.decodeFile(file.path)
            runDetector(bitmap)
        }
    }

    @Throws(IOException::class)
    fun createTempFile(context: Context?, suffix: String?): File {

        val timeStamp = SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "${timeStamp}_",
            suffix,
            storageDir
        )
    }

    private fun runDetector(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
            .build()

        FirebaseApp.initializeApp(applicationContext)
        mDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        mDetector?.detectInImage(image)
            ?.addOnSuccessListener { processResult(it) }
            ?.addOnFailureListener { processFailure(it) }
    }

    private fun processResult(it: List<FirebaseVisionBarcode>) {
        it.forEach { item ->
            when (item.valueType) {
                FirebaseVisionBarcode.TYPE_URL -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.rawValue)))
                }
                FirebaseVisionBarcode.TYPE_TEXT -> {
                    Toast.makeText(baseContext, item.rawValue, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processFailure(it: Exception) {
        Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST = 234
    }
}