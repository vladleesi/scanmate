package ru.vladleesi.ultimatescanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageCompressUtils {

    /**
     * compress the file/photo from @param **path** to a private location on the current device and return the compressed file.
     *
     * @param path    = The original image path
     * @param context = Current android Context
     */

    fun getCompressed(context: Context?, path: String?): File? {
        return try {
            // dateFormat to generate a unique name for the compressed file.
            val dateFormat = SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault())
            if (context == null) throw NullPointerException("Context must not be null.")
            // getting device external cache directory, might not be available on some devices,
            // so our code fall back to internal storage cache directory, which is always available but in smaller quantity
            val imageCompressedCacheDir = FileUtils.getImageCompressorCacheDir(context)
            // decode and resize the original bitmap from @param path.
            val bitmap =
                decodeImageFromFiles(
                    path,  /* your desired width */
                    300,  /* your desired height */
                    300
                )
            // create placeholder for the compressed image file
            val compressed =
                File(
                    imageCompressedCacheDir,
                    dateFormat.format(Date()) + ".jpg" /* Your desired format */
                )
            // convert the decoded bitmap to stream
            val byteArrayOutputStream = ByteArrayOutputStream()
            /*
                    compress bitmap into byteArrayOutputStream
                    Bitmap.compress(Format, Quality, OutputStream)
                    Where Quality ranges from 1–100.
                */
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            /*
                    Right now, we have our bitmap inside byteArrayOutputStream Object, all we need next is to write it to the compressed file we created earlier,
                    java.io.FileOutputStream can help us do just That!
                */
            val fileOutputStream = FileOutputStream(compressed)
            fileOutputStream.write(byteArrayOutputStream.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
            // File written, return to the caller. Done!
            compressed
        } catch (ex: IOException) {
            null
        }
    }

    private fun decodeImageFromFiles(path: String?, width: Int, height: Int): Bitmap {
        val scaleOptions = BitmapFactory.Options()
        scaleOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, scaleOptions)
        var scale = 1
        while (scaleOptions.outWidth / scale / 2 >= width && scaleOptions.outHeight / scale / 2 >= height) {
            scale *= 2
        }
        // decode with the sample size
        val outOptions = BitmapFactory.Options()
        outOptions.inSampleSize = scale
        return BitmapFactory.decodeFile(path, outOptions)
    }
}