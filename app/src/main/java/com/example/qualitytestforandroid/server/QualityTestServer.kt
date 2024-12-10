package com.example.qualitytestforandroid.server

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class QualityTestServer(private val context: Context) {
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val clients = ConcurrentHashMap<String, ClientHandler>()

    companion object {
        private const val TAG = "QualityTestServer"
        private const val DEFAULT_PORT = 8888
    }

    fun start(port: Int = DEFAULT_PORT) {
        if (isRunning.get()) return

        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning.set(true)
                Log.i(TAG, "服务器启动在端口: $port")

                while (isRunning.get()) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        handleNewClient(clientSocket)
                    } catch (e: Exception) {
                        if (isRunning.get()) {
                            Log.e(TAG, "接受客户端连接错误: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "服务器启动失败: ${e.message}")
            }
        }
    }

    private fun handleNewClient(socket: Socket) {
        val clientId = "${socket.inetAddress.hostAddress}:${socket.port}"
        val clientHandler = ClientHandler(socket, clientId)
        clients[clientId] = clientHandler
        
        scope.launch {
            try {
                clientHandler.start()
            } catch (e: Exception) {
                Log.e(TAG, "客户端处理错误: ${e.message}")
            } finally {
                clients.remove(clientId)
                clientHandler.stop()
            }
        }
    }

    fun broadcastMessage(message: JSONObject) {
        clients.values.forEach { client ->
            scope.launch {
                try {
                    client.sendMessage(message)
                } catch (e: Exception) {
                    Log.e(TAG, "广播消息失败: ${e.message}")
                }
            }
        }
    }

    fun stop() {
        isRunning.set(false)
        clients.values.forEach { it.stop() }
        clients.clear()
        serverSocket?.close()
        scope.cancel()
    }

    private inner class ClientHandler(
        private val socket: Socket,
        private val clientId: String
    ) {
        private var reader: BufferedReader? = null
        private var writer: PrintWriter? = null
        private val isRunning = AtomicBoolean(false)

        fun start() {
            try {
                reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                isRunning.set(true)
                
                while (isRunning.get()) {
                    val message = reader?.readLine() ?: break
                    handleMessage(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "客户端 $clientId 连接错误: ${e.message}")
            } finally {
                stop()
            }
        }

        private fun handleMessage(message: String) {
            try {
                val json = JSONObject(message)
                when (json.optString("type")) {
                    "test_record" -> handleTestRecord(json)
                    "chart_data" -> handleChartData(json)
                    else -> Log.w(TAG, "未知消息类型: ${json.optString("type")}")
                }
                // 广播消息给其他客户端
                broadcastToOthers(json)
            } catch (e: Exception) {
                Log.e(TAG, "处理消息错误: ${e.message}")
            }
        }

        private fun handleTestRecord(json: JSONObject) {
            // 处理测试记录
            Log.d(TAG, "收到测试记录: $json")
        }

        private fun handleChartData(json: JSONObject) {
            // 处理图表数据
            Log.d(TAG, "收到图表数据: $json")
        }

        private fun broadcastToOthers(message: JSONObject) {
            clients.forEach { (id, client) ->
                if (id != clientId) {
                    scope.launch {
                        try {
                            client.sendMessage(message)
                        } catch (e: Exception) {
                            Log.e(TAG, "广播到客户端 $id 失败: ${e.message}")
                        }
                    }
                }
            }
        }

        fun sendMessage(message: JSONObject) {
            try {
                writer?.println(message.toString())
            } catch (e: Exception) {
                Log.e(TAG, "发送消息到客户端 $clientId 失败: ${e.message}")
                stop()
            }
        }

        fun stop() {
            isRunning.set(false)
            try {
                reader?.close()
                writer?.close()
                socket.close()
            } catch (e: Exception) {
                Log.e(TAG, "关闭客户端连接错误: ${e.message}")
            }
        }
    }
}
