package com.example.qualitytestforandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import java.io.File
import java.net.Socket
import android.util.Log

class AdminLoginActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var backButton: MaterialButton
    private lateinit var startServerButton: MaterialButton
    private lateinit var connectServerButton: MaterialButton
    private lateinit var viewStatsButton: MaterialButton
    
    private val job = Job()
    override val coroutineContext = job + Dispatchers.Main
    
    private var serverProcess: Process? = null
    private var isServerRunning = false
    private var isConnectedToServer = false

    companion object {
        private const val TAG = "AdminLoginActivity"
        private const val SERVER_PORT = 12345
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        initializeViews()
        setupButtonListeners()
    }

    private fun initializeViews() {
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        backButton = findViewById(R.id.backButton)
        startServerButton = findViewById(R.id.startServerButton)
        connectServerButton = findViewById(R.id.connectServerButton)
        viewStatsButton = findViewById(R.id.viewStatsButton)
        
        // 初始状态下禁用连接和统计按钮
        connectServerButton.isEnabled = false
        viewStatsButton.isEnabled = false
    }

    private fun setupButtonListeners() {
        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString() ?: ""
            val password = passwordInput.text?.toString() ?: ""

            if (validateCredentials(username, password)) {
                val intent = Intent(this, AdminMainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        startServerButton.setOnClickListener {
            if (!isServerRunning) {
                startServer()
            } else {
                stopServer()
            }
        }

        connectServerButton.setOnClickListener {
            if (!isConnectedToServer) {
                connectToServer()
            } else {
                disconnectFromServer()
            }
        }

        viewStatsButton.setOnClickListener {
            // TODO: Implement statistics view
            Toast.makeText(this, "统计功能开发中", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startServer() {
        launch(Dispatchers.IO) {
            try {
                val pythonScript = File(filesDir, "server.py")
                if (!pythonScript.exists()) {
                    try {
                        // 从 assets 复制 server.py 到 filesDir
                        assets.open("server.py").use { input ->
                            pythonScript.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.d(TAG, "Successfully copied server.py to ${pythonScript.absolutePath}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to copy server.py from assets", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AdminLoginActivity, "复制服务器脚本失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                }

                // 确保脚本有执行权限
                pythonScript.setExecutable(true)

                // 使用 Python 解释器运行脚本
                val processBuilder = ProcessBuilder("python", pythonScript.absolutePath)
                processBuilder.redirectErrorStream(true)
                serverProcess = processBuilder.start()
                
                // 启动一个协程来监控服务器输出
                launch(Dispatchers.IO) {
                    serverProcess?.inputStream?.bufferedReader()?.use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            Log.d(TAG, "Server output: $line")
                        }
                    }
                }

                isServerRunning = true

                withContext(Dispatchers.Main) {
                    startServerButton.text = "停止服务器"
                    connectServerButton.isEnabled = true
                    Toast.makeText(this@AdminLoginActivity, "服务器启动成功", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting server", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminLoginActivity, "服务器启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun stopServer() {
        launch(Dispatchers.IO) {
            try {
                serverProcess?.destroy()
                serverProcess = null
                isServerRunning = false

                withContext(Dispatchers.Main) {
                    startServerButton.text = "启动服务器"
                    connectServerButton.isEnabled = false
                    if (isConnectedToServer) {
                        disconnectFromServer()
                    }
                    Toast.makeText(this@AdminLoginActivity, "服务器已停止", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminLoginActivity, "停止服务器失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun connectToServer() {
        launch(Dispatchers.IO) {
            try {
                val socket = Socket("localhost", SERVER_PORT)
                isConnectedToServer = true

                withContext(Dispatchers.Main) {
                    connectServerButton.text = "断开连接"
                    viewStatsButton.isEnabled = true
                    Toast.makeText(this@AdminLoginActivity, "已连接到服务器", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to server", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminLoginActivity, "连接服务器失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun disconnectFromServer() {
        launch(Dispatchers.IO) {
            try {
                // TODO: Implement proper socket closing
                isConnectedToServer = false

                withContext(Dispatchers.Main) {
                    connectServerButton.text = "连接服务器"
                    viewStatsButton.isEnabled = false
                    Toast.makeText(this@AdminLoginActivity, "已断开服务器连接", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting from server", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminLoginActivity, "断开连接失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateCredentials(username: String, password: String): Boolean {
        return username == "admin" && password == "admin"
    }

    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        serverProcess?.destroy()
    }
}
