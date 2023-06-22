package dev.vladleesi.scanmate.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

fun Context.requestPermissionIfNeed(
    activityResultLauncher: ActivityResultLauncher<String>,
    permission: String,
    callback: () -> Unit
) {
    if (isPermissionGranted(permission))
        callback()
    else
        activityResultLauncher.launch(permission)
}

private fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED

fun Context.allPermissionsGranted() = REQUIRED_PERMISSIONS.all { isPermissionGranted(it) }
