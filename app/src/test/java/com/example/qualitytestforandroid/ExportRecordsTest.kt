package com.example.qualitytestforandroid

import android.content.Context
import com.example.qualitytestforandroid.data.QualityTestRecord
import com.example.qualitytestforandroid.data.TestResult
import com.example.qualitytestforandroid.data.DefectType
import com.example.qualitytestforandroid.data.DefectSeverity
import com.example.qualitytestforandroid.network.NetworkService
import com.example.qualitytestforandroid.utils.ExcelExporter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class ExportRecordsTest {
    private lateinit var mockContext: MockContext
    
    @Mock
    private lateinit var networkService: NetworkService
    
    private lateinit var excelExporter: ExcelExporter
    
    @Before
    fun setup() {
        mockContext = MockContext()
        excelExporter = ExcelExporter(mockContext, networkService)
    }
    
    @Test
    fun testExportRecords() {
        // 准备测试数据
        val testRecords = listOf(
            QualityTestRecord(
                timestamp = LocalDateTime.now(),
                productionLine = "Line A",
                testResult = TestResult.PASS,
                defectType = null,
                defectLocation = null,
                defectSeverity = null
            ),
            QualityTestRecord(
                timestamp = LocalDateTime.now(),
                productionLine = "Line B",
                testResult = TestResult.FAIL,
                defectType = DefectType.SCRATCH,
                defectLocation = "左上角",
                defectSeverity = DefectSeverity.MAJOR
            )
        )
        
        // 执行导出
        val exportFile = excelExporter.exportToExcel(testRecords)
        
        // 验证文件是否创建
        assertTrue(exportFile.exists(), "导出文件应该被创建")
        assertTrue(exportFile.length() > 0, "导出文件不应该为空")
        
        // 验证网络服务调用
        verify(networkService).uploadFile(eq(exportFile))
        
        // 清理测试文件
        exportFile.delete()
    }
    
    @Test
    fun testExportEmptyRecords() {
        // 测试空记录列表
        val emptyRecords = emptyList<QualityTestRecord>()
        
        // 执行导出
        val exportFile = excelExporter.exportToExcel(emptyRecords)
        
        // 验证文件是否创建（即使是空记录也应该创建文件）
        assertTrue(exportFile.exists(), "空记录也应该创建导出文件")
        assertTrue(exportFile.length() > 0, "空记录导出文件应该包含表头")
        
        // 验证网络服务没有被调用（因为是空记录）
        verify(networkService, never()).uploadFile(any())
        
        // 清理测试文件
        exportFile.delete()
    }
}
