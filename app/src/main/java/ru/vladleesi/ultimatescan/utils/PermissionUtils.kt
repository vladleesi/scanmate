package ru.vladleesi.ultimatescan.utils

import android.content.Context
import android.content.pm.PackageManager

class PermissionUtils {

    companion object {

        /** Check if this device has a camera */
        fun checkCameraHardware(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }
    }
}