package ru.vladleesi.ultimatescanner.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.io.File

object FileUtils {

    private fun getPathFrom(context: Context?, uri: Uri): String? {
        context?.let {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor =
                context.contentResolver?.query(uri, projection, null, null, null)
                    ?: return null
            val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val path = cursor.getString(columnIndex)
            cursor.close()
            return path
        }
        return null
    }

    fun getFile(context: Context?, uri: Uri?): File? {
        return getPathFrom(context, uri ?: return null)?.let { File(it) }
    }
}