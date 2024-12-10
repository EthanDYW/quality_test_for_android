package com.example.qualitytestforandroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.qualitytestforandroid.data.QualityTestRecord
import com.example.qualitytestforandroid.network.NetworkService
import com.example.qualitytestforandroid.data.ChartDataManager
import com.example.qualitytestforandroid.data.ChartDataPoint
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.ServerSocket
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread
import java.util.Date

@RunWith(AndroidJUnit4::class)
class NetworkServiceTest {
    private lateinit var serverSocket: ServerSocket
    private var serverThread: Thread? = null
    private val TEST_PORT = 8989
    private val TEST_HOST = "localhost"

    @Before
    fun setup() {
        // 启动测试服务器
        serverThread = thread {
            serverSocket = ServerSocket(TEST_PORT)
            println("Test server started on port $TEST_PORT")
        }
    }

    @Test
    fun testServerClientConnection() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val networkService = NetworkService(context)
        
        // 测试连接
        val connected = withTimeout(5000) {
            networkService.connect(TEST_HOST, TEST_PORT)
        }
        assertTrue("连接服务器失败", connected)

        // 创建测试数据
        val testRecord = QualityTestRecord(
            employeeId = "EMP001",
            date = Date(),
            productionLine = "Line A",
            defectType = "Scratch",
            defectLocation = "Top",
            defectSeverity = "High",
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + 30000,
            defectTypeErrors = mutableMapOf("Scratch" to 1),
            isPassed = false,
            testDuration = 30
        )

        // 测试数据发送
        val sent = networkService.sendData(testRecord.toJson())
        assertTrue("发送数据失败", sent)

        networkService.disconnect()
    }

    @Test
    fun testChartDataSync() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val networkService = NetworkService(context)
        val chartDataManager = ChartDataManager(context)

        // 创建测试数据点
        val testPoints = listOf(
            ChartDataPoint("2023-01-01", 10),
            ChartDataPoint("2023-01-02", 15),
            ChartDataPoint("2023-01-03", 12)
        )

        // 测试数据同步
        testPoints.forEach { point ->
            chartDataManager.addDataPoint(point)
        }

        // 连接并同步
        val connected = withTimeout(5000) {
            networkService.connect(TEST_HOST, TEST_PORT)
        }
        assertTrue("连接服务器失败", connected)

        // 验证同步
        val syncedPoints = chartDataManager.getDataPoints()
        assertEquals("数据点数量不匹配", testPoints.size, syncedPoints.size)
        
        // 验证数据内容
        testPoints.forEachIndexed { index, expected ->
            val actual = syncedPoints[index]
            assertEquals("日期不匹配", expected.date, actual.date)
            assertEquals("数值不匹配", expected.value, actual.value)
        }

        networkService.disconnect()
    }
}
