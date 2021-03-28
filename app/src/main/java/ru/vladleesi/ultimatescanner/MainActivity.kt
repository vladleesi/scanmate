package ru.vladleesi.ultimatescanner

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ru.vladleesi.ultimatescanner.camera.CameraActivity
import ru.vladleesi.ultimatescanner.utils.PermissionUtils


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.b_open_camera).setOnClickListener {
            if (PermissionUtils.checkCameraHardware(baseContext)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
            }
            startActivity(Intent(baseContext, CameraActivity::class.java))
        }
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST = 11
    }

}