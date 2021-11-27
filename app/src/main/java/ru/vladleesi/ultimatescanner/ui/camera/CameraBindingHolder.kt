package ru.vladleesi.ultimatescanner.ui.camera

import android.net.Uri
import android.widget.ImageView
import androidx.camera.view.PreviewView

interface CameraBindingHolder {
    fun getCameraView(): PreviewView
    fun getFlashlightView(): ImageView
    fun flashlightOn()
    fun flashlightOff()
    fun onDetect(savedUri: Uri)
}
