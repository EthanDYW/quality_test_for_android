package com.example.qualitytestforandroid.network

import android.content.Context
import android.util.Log
import com.example.qualitytestforandroid.data.QualityTestRecord
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class QualityTestServer(private val context: Context) {
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val clients = mutableListOf<ClientHandler>()
    
    companion object {
        private const val TAG = "QualityTestServer"
        const val SERVER_PORT = 12345
    }

    fun startServer(): Boolean {
        return try {
            serverSocket = ServerSocket(SERVER_PORT)
            isRunning.set(true)
            Log.d(TAG, "Server started on port $SERVER_PORT")
            
            // 在协程中启动服务器监听
            scope.launch {
                while (isRunning.get()) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        if (clientSocket != null) {
                            Log.d(TAG, "New client connected: ${clientSocket.inetAddress.hostAddress}")
                            val clientHandler = ClientHandler(clientSocket)
                            clients.add(clientHandler)
                            launch { clientHandler.handleClient() }
                        }
                    } catch (e: Exception) {
                        if (isRunning.get()) {
                            Log.e(TAG, "Error accepting client connection", e)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting server", e)
            false
        }
    }

    fun stopServer() {
        isRunning.set(false)
        clients.forEach { it.close() }
        clients.clear()
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket", e)
        }
        serverSocket = null
        scope.cancel()
    }

    inner class ClientHandler(private val clientSocket: Socket) {
        private var reader: BufferedReader? = null
        private var writer: PrintWriter? = null

        init {
            try {
                reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                writer = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket.outputStream)), true)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing client handler", e)
                close()
            }
        }

        suspend fun handleClient() {
            try {
                while (isRunning.get()) {
                    val message = withContext(Dispatchers.IO) {
                        reader?.readLine()
                    } ?: break

                    try {
                        val json = JSONObject(message)
                        when (json.getString("type")) {
                            "test_record" -> handleTestRecord(json)
                            "chart_data" -> handleChartData(json)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling message: $message", e)
                    }
                }
            } catch (e: Exception) {
                if (isRunning.get()) {
                    Log.e(TAG, "Error in client handler", e)
                }
            } finally {
                close()
            }
        }

        private fun handleTestRecord(json: JSONObject) {
            // 处理测试记录数据
            val response = JSONObject().apply {
                put("type", "response")
                put("status", "success")
                put("message", "Test record received")
            }
            sendMessage(response.toString())
        }

        private fun handleChartData(json: JSONObject) {
            // 处理图表数据
            val response = JSONObject().apply {
                put("type", "response")
                put("status", "success")
                put("message", "Chart data received")
            }
            sendMessage(response.toString())
        }

        fun sendMessage(message: String) {
            try {
                writer?.println(message)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }

        fun close() {
            try {
                reader?.close()
                writer?.close()
                clientSocket.close()
                clients.remove(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error closing client handler", e)
            }
        }
    }
}
