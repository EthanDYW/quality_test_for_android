package com.example.qualitytestforandroid.network

import android.content.Context
import android.util.Log
import com.example.qualitytestforandroid.data.ChartDataManager
import com.example.qualitytestforandroid.data.ChartDataPoint
import com.example.qualitytestforandroid.data.DataManager
import com.example.qualitytestforandroid.data.QualityTestRecord
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.text.SimpleDateFormat
import java.util.*

class NetworkService(private val context: Context) {
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private val isConnected = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var reconnectJob: Job? = null
    private val TAG = "NetworkService"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    companion object {
        private const val RECONNECT_DELAY = 5000L // 5秒重连间隔
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }

    fun connect(host: String, port: Int) {
        try {
            socket = Socket(host, port)
            reader = BufferedReader(InputStreamReader(socket?.inputStream))
            writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket?.outputStream)), true)
            isConnected.set(true)
            startListening()
            Log.d(TAG, "Connected to server: $host:$port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect: ${e.message}")
            handleDisconnection()
        }
    }

    private fun startListening() {
        scope.launch {
            while (isConnected.get()) {
                try {
                    val message = receiveData()
                    if (message != null) {
                        handleMessage(message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error while listening: ${e.message}")
                    handleDisconnection()
                    break
                }
            }
        }
    }

    private fun handleMessage(message: String) {
        try {
            val json = JSONObject(message)
            when (json.optString("type")) {
                "test_record" -> handleTestRecord(json)
                "chart_data" -> handleChartData(json)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message: ${e.message}")
        }
    }

    private fun handleTestRecord(json: JSONObject) {
        val data = json.getJSONObject("data")
        val timestamp = data.getLong("timestamp")
        val errors = mutableMapOf<String, Int>()
        
        // 处理错误记录
        val errorsJson = data.optJSONObject("defectTypeErrors")
        if (errorsJson != null) {
            val iterator = errorsJson.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                errors[key] = errorsJson.getInt(key)
            }
        }
        
        val record = QualityTestRecord(
            employeeId = data.getString("employeeId"),
            productionLine = data.getString("productionLine"),
            date = Date(timestamp),
            defectType = data.optString("defectType", ""),
            defectLocation = data.optString("defectLocation", ""),
            defectSeverity = data.optString("defectSeverity", ""),
            startTime = data.getLong("startTime"),
            endTime = data.getLong("endTime"),
            defectTypeErrors = errors,
            isPassed = data.getBoolean("isPassed"),
            testDuration = data.getInt("testDuration")
        )

        DataManager.getInstance(context).addTestRecord(record)
    }

    private fun handleChartData(json: JSONObject) {
        val data = json.getJSONObject("data")
        val timestamp = data.getLong("timestamp")
        
        val dataPoint = ChartDataPoint(
            date = dateFormat.format(Date(timestamp)),
            value = data.getInt("value"),
            timestamp = timestamp
        )
        
        ChartDataManager.getInstance(context).addDataPoint(dataPoint)
    }

    private fun handleDisconnection() {
        isConnected.set(false)
        closeConnection()
        startReconnection()
    }

    private fun startReconnection() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            var attempts = 0
            while (!isConnected.get() && attempts < MAX_RECONNECT_ATTEMPTS) {
                delay(RECONNECT_DELAY)
                try {
                    attempts++
                    Log.d(TAG, "Attempting to reconnect... (Attempt $attempts)")
                    connect(socket?.inetAddress?.hostAddress ?: return@launch, socket?.port ?: return@launch)
                } catch (e: Exception) {
                    Log.e(TAG, "Reconnection attempt failed: ${e.message}")
                }
            }
        }
    }

    fun sendData(data: JSONObject) {
        if (!isConnected.get()) {
            Log.e(TAG, "Cannot send data: Not connected")
            return
        }
        scope.launch {
            try {
                writer?.println(data.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Error sending data: ${e.message}")
                handleDisconnection()
            }
        }
    }

    fun syncTestRecord(record: QualityTestRecord) {
        val json = JSONObject().apply {
            put("type", "test_record")
            put("data", JSONObject().apply {
                put("employeeId", record.employeeId)
                put("productionLine", record.productionLine)
                put("timestamp", record.date.time)
                put("defectType", record.defectType)
                put("defectLocation", record.defectLocation)
                put("defectSeverity", record.defectSeverity)
                put("startTime", record.startTime)
                put("endTime", record.endTime)
                put("isPassed", record.isPassed)
                put("testDuration", record.testDuration)
                put("defectTypeErrors", JSONObject(record.defectTypeErrors))
            })
        }
        sendData(json)
    }

    fun sendChartData(dataPoint: ChartDataPoint) {
        val json = JSONObject().apply {
            put("type", "chart_data")
            put("data", JSONObject().apply {
                put("timestamp", dataPoint.timestamp)
                put("value", dataPoint.value)
            })
        }
        sendData(json)
    }

    private suspend fun receiveData(): String? = suspendCoroutine { continuation ->
        try {
            val line = reader?.readLine()
            continuation.resume(line)
        } catch (e: SocketException) {
            Log.e(TAG, "Socket error while receiving data: ${e.message}")
            handleDisconnection()
            continuation.resume(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error receiving data: ${e.message}")
            continuation.resume(null)
        }
    }

    fun isConnected(): Boolean {
        return isConnected.get()
    }

    fun disconnect() {
        isConnected.set(false)
        closeConnection()
    }

    private fun closeConnection() {
        try {
            reader?.close()
            writer?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection: ${e.message}")
        }
    }
}
