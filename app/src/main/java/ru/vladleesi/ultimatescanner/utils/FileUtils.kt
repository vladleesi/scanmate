package ru.vladleesi.ultimatescanner.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun getFile(context: Context?, uri: Uri?): File? {
        return context?.let {
            getPathFrom(context, uri ?: return null)?.let { filePath -> File(filePath) }
        }
    }

    @Throws(URISyntaxException::class)
    fun getPathFrom(context: Context?, uri: Uri): String? {
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

                if (id.startsWith("raw:/")) {
                    return id.replace("raw:/", "file:///")
                } else {
                    if (id.startsWith("msf:")) {
                        context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
                            val file =
                                File(
                                    context.externalCacheDir,
                                    uri.lastPathSegment ?: return null
                                )
                            FileOutputStream(file).use { output ->
                                val maxMemory =
                                    (Runtime.getRuntime().maxMemory() / 1024).toInt()
                                val cacheSize = maxMemory / 8
                                val buffer = ByteArray(cacheSize)
                                var read: Int
                                while (inputStream.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                }
                                output.flush()
                                return file.path
                            }
                        }
                    }

                    uriInner = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), id.toLong()
                    )
                }
            }
            isMediaDocument(uriInner) -> {
                val docId = DocumentsContract.getDocumentId(uriInner)
                val split = docId.split(":".toRegex()).toTypedArray()
                when (split[0]) {
                    "image" -> {
                        uriInner = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        uriInner = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        uriInner = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
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

            context?.contentResolver?.query(
                uriInner,
                projection,
                selection,
                selectionArgs,
                null
            )
                .use { cursor ->
                    val columnIndex =
                        cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
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

    fun getFileInfo(context: Context?, fileUri: Uri, file: File? = null): Pair<String, Long> {
        var displayName = "indefinitely"
        var fileSize: Long = 0

        if (fileUri.toString().startsWith("content://")) {
            var cursor: Cursor? = null
            try {
                cursor = context?.contentResolver
                    ?.query(fileUri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex =
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    displayName = cursor.getString(columnIndex)

                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    fileSize = cursor.getLong(sizeIndex)
                }
            } finally {
                cursor?.close()
            }
        } else if (fileUri.toString().startsWith("file://")) {
            val fileInner = file ?: File(fileUri.toString())
            displayName = fileInner.name
            fileSize = fileInner.length()
        }

        return Pair(displayName, fileSize)
    }

    fun createFile(fileName: String, fileDir: String): File {
        val file =
            File("$fileDir/$fileName")
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    fun writeBytesToFile(file: File, bytes: ByteArray?) {
        FileOutputStream(file).use { fos ->
            fos.write(bytes)
        }
    }

    @Throws(IOException::class)
    fun createTempFile(context: Context?, suffix: String?, fileName: String? = null): File {

        val timeStamp = SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val storageDir = context?.externalCacheDir

        return File.createTempFile(
            "${fileName ?: timeStamp}_",
            suffix,
            storageDir
        )
    }

    @Throws(IOException::class)
    fun copyBytesToFile(byteArray: ByteArray, file: File?) {
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.use {
            it.write(byteArray)
            it.close()
        }
    }

    fun readFileToBytes(filePath: String?): ByteArray? {
        return filePath?.let {
            val file = File(filePath)
            return@let ByteArray(file.length().toInt()).also { bytes ->
                FileInputStream(file).use { fis ->
                    fis.read(bytes, 0, bytes.size)
                }
            }
        }
    }

    fun getFileExtensionFromMimeType(mimeType: String?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    fun getMimeType(contextWeakReference: WeakReference<Context>, uri: Uri): String? {
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.let { extension ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } ?: contextWeakReference.get()?.contentResolver?.getType(uri)
    }

    fun isImage(contextWeakReference: WeakReference<Context>, uri: Uri): Boolean {
        val mimeType = getMimeType(contextWeakReference, uri)
        return mimeType?.contains("image/") == true
    }

    fun isImage(mimeType: String?): Boolean {
        return mimeType?.contains("image/") == true
    }

    fun getImageCompressorCacheDir(context: Context): File {
        val path = context.externalCacheDir?.absolutePath + "/compressed"
        return File(path).apply {
            // Create ImageCompressor folder if it doesnt already exists.
            if (!exists()) mkdirs()
        }
    }

    fun clearImageCompressorCache(context: Context) {
        getImageCompressorCacheDir(context)
            .takeIf { it.exists() }
            ?.deleteRecursively()
    }
}
