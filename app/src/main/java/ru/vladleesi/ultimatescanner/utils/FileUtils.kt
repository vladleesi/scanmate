package ru.vladleesi.ultimatescanner.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.net.URISyntaxException


object FileUtils {

    fun getFile(context: Context?, uri: Uri?): File? {
        return context?.let {
            getFilePath(context, uri ?: return null)?.let { filePath -> File(filePath) }
        }
    }

    @Throws(URISyntaxException::class)
    fun getFilePath(context: Context, uri: Uri): String? {
        var uriInner = uri
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        when {
            isExternalStorageDocument(uriInner) -> {
                val docId = DocumentsContract.getDocumentId(uriInner)
                val split = docId.split(":".toRegex()).toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
            isDownloadsDocument(uriInner) -> {
                val id = DocumentsContract.getDocumentId(uriInner)
                uriInner = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
            }
            isMediaDocument(uriInner) -> {
                val docId = DocumentsContract.getDocumentId(uriInner)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uriInner = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uriInner = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uriInner = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(
                    split[1]
                )
            }
        }
        if ("content".equals(uriInner.scheme, ignoreCase = true)) {
            if (isGooglePhotosUri(uriInner)) {
                return uriInner.lastPathSegment
            }
            val projection = arrayOf(
                MediaStore.Images.Media.DATA
            )

            context.contentResolver.query(uriInner, projection, selection, selectionArgs, null)
                .use { cursor ->
                    val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    if (cursor?.moveToFirst() == true) {
                        return cursor.getString(columnIndex ?: return null)
                    }
                }
        } else if ("file".equals(uriInner.scheme, ignoreCase = true)) {
            return uriInner.path
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}