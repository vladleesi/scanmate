package ru.vladleesi.ultimatescanner.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import ru.vladleesi.ultimatescanner.MainActivity

class PermissionUtils {

    companion object {

        /** Check if this device has a camera */
        private fun checkCameraHardware(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }

        /** Request camera permission if need */
        fun requestPermission(activity: Activity) {
            if (checkCameraHardware(activity.baseContext)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    MainActivity.CAMERA_PERMISSION_REQUEST
                )
            }
        }
    }
}