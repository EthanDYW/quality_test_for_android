package com.example.qualitytestforandroid

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PhotoLibrarySelectorActivity : AppCompatActivity() {
    private lateinit var toolbarTitle: TextView
    private lateinit var okLibraryButton: MaterialButton
    private lateinit var ngLibraryButton: MaterialButton
    private lateinit var productionLine: String
    private lateinit var defectType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_library_selector)

        productionLine = intent.getStringExtra("PRODUCTION_LINE") ?: return
        defectType = intent.getStringExtra("DEFECT_TYPE") ?: return

        toolbarTitle = findViewById(R.id.toolbarTitle)
        okLibraryButton = findViewById(R.id.okLibraryButton)
        ngLibraryButton = findViewById(R.id.ngLibraryButton)

        toolbarTitle.text = "$defectType - 照片库选择"

        okLibraryButton.setOnClickListener {
            openPhotoLibrary("OK")
        }

        ngLibraryButton.setOnClickListener {
            openPhotoLibrary("NG")
        }
    }

    private fun openPhotoLibrary(type: String) {
        val intent = Intent(this, PhotoLibraryActivity::class.java).apply {
            putExtra("PRODUCTION_LINE", productionLine)
            putExtra("DEFECT_TYPE", defectType)
            putExtra("LIBRARY_TYPE", type)
        }
        startActivity(intent)
    }
}
