package com.example.qualitytestforandroid.utils

import android.content.Context
import android.os.Environment
import com.example.qualitytestforandroid.data.TestRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelExporter(private val context: Context) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    fun exportToExcel(records: List<TestRecord>): String {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "quality_test_report_$timeStamp.csv"
            
            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "QualityTestReports")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val file = File(directory, fileName)
            FileOutputStream(file).use { fos ->
                // Write CSV header
                fos.write("日期,生产线,缺陷类型,测试结果,测试时长(秒)\n".toByteArray())
                
                // Write records
                records.forEach { record ->
                    val line = "${dateFormat.format(record.date)},${record.productionLine},${record.defectType},${if (record.isPassed) "通过" else "未通过"},${record.testDuration}\n"
                    fos.write(line.toByteArray())
                }
            }
            
            return file.absolutePath
            
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
