package ru.vladleesi.ultimatescanner.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import ru.vladleesi.ultimatescanner.Constants.CAMERA_PERMISSION_REQUEST

object PermissionUtils {

    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    @Deprecated("Old request")
    /** Request camera permission if need */
    fun requestPermission(activity: Activity) {
        if (checkCameraHardware(activity.baseContext)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }
}
