package ru.vladleesi.ultimatescanner.extensions

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(
        this, 0, 0, width, height, matrix, true
    )
}

fun Bitmap.resize(newWidth: Int, newHeight: Int): Bitmap? {
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    // CREATE A MATRIX FOR THE MANIPULATION & RESIZE THE BIT MAP
    val matrix = Matrix().apply { postScale(scaleWidth, scaleHeight) }
    // "RECREATE" THE NEW BITMAP
    return Bitmap.createBitmap(
        this, 0, 0, width, height, matrix, false
    )
}
