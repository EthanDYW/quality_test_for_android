package com.example.qualitytestforandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.qualitytestforandroid.network.NetworkService
import com.example.qualitytestforandroid.network.QualityTestServer
import com.example.qualitytestforandroid.ui.ChartDisplayActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {
    private lateinit var startServerButton: MaterialButton
    private lateinit var stopServerButton: MaterialButton
    private lateinit var connectServerButton: MaterialButton
    private lateinit var viewStatsButton: MaterialButton
    private var server: QualityTestServer? = null
    private var networkService: NetworkService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        startServerButton = findViewById(R.id.startServerButton)
        stopServerButton = findViewById(R.id.stopServerButton)
        connectServerButton = findViewById(R.id.connectServerButton)
        viewStatsButton = findViewById(R.id.viewStatsButton)

        // 初始状态
        stopServerButton.isEnabled = false
        connectServerButton.isEnabled = true
        viewStatsButton.isEnabled = true
    }

    private fun setupListeners() {
        startServerButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val success = startServer()
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@AdminActivity, "服务器启动成功", Toast.LENGTH_SHORT).show()
                        startServerButton.isEnabled = false
                        stopServerButton.isEnabled = true
                    } else {
                        Toast.makeText(this@AdminActivity, "服务器启动失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        stopServerButton.setOnClickListener {
            stopServer()
        }

        connectServerButton.setOnClickListener {
            connectToServer()
        }

        viewStatsButton.setOnClickListener {
            startActivity(Intent(this, ChartDisplayActivity::class.java))
        }
    }

    private fun startServer(): Boolean {
        server = QualityTestServer(this)
        return server?.startServer() ?: false
    }

    private fun stopServer() {
        server?.stopServer()
        server = null
        startServerButton.isEnabled = true
        stopServerButton.isEnabled = false
        Toast.makeText(this, "服务器已停止", Toast.LENGTH_SHORT).show()
    }

    private fun connectToServer() {
        lifecycleScope.launch {
            networkService = NetworkService(this@AdminActivity)
            networkService?.connect("localhost", QualityTestServer.SERVER_PORT)
            if (networkService?.isConnected() == true) {
                Toast.makeText(this@AdminActivity, "已连接到服务器", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AdminActivity, "连接失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stopServer()
        server = null
        networkService?.disconnect()
        networkService = null
    }
}
