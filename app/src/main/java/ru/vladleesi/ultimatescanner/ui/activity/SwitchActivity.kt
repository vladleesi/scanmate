package ru.vladleesi.ultimatescanner.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ru.vladleesi.ultimatescanner.R

class SwitchActivity : AppCompatActivity(R.layout.activity_switch) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.old_nav).setOnClickListener {
            finish()
            startActivity(Intent(baseContext, CameraPreviewActivity::class.java))
        }

        findViewById<Button>(R.id.new_nav).setOnClickListener {
            finish()
            startActivity(Intent(baseContext, MainActivity::class.java))
        }
    }

}