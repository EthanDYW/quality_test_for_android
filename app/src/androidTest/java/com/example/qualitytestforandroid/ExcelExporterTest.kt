package com.example.qualitytestforandroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.qualitytestforandroid.data.QualityTestRecord
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
class ExcelExporterTest {

    @Test
    fun testExportToCSV() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val exporter = ExcelExporter(appContext)

        // Create test data
        val testRecords = listOf(
            QualityTestRecord(
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
            ),
            QualityTestRecord(
                employeeId = "EMP002",
                date = Date(),
                productionLine = "Line B",
                defectType = "Dent",
                defectLocation = "Bottom",
                defectSeverity = "Low",
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis() + 25000,
                defectTypeErrors = mutableMapOf("Dent" to 1),
                isPassed = true,
                testDuration = 25
            )
        )

        // Export to CSV
        val exportFile = exporter.exportToExcel(testRecords)

        // Verify file exists and not empty
        assertTrue("Export file should exist", exportFile.exists())
        assertTrue("Export file should not be empty", exportFile.length() > 0)

        // Read file content
        val content = exportFile.readText()

        // Verify CSV structure
        assertTrue("CSV should contain header", content.contains("日期,员工ID,生产线,缺陷类型,缺陷位置,缺陷严重度,测试结果,测试时长(秒)"))
        assertTrue("CSV should contain Line A", content.contains("Line A"))
        assertTrue("CSV should contain Line B", content.contains("Line B"))
    }
}
