package com.example.qualitytestforandroid

import android.content.Context
import com.example.qualitytestforandroid.data.QualityTestRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExcelExporter(private val context: Context) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportToExcel(records: List<QualityTestRecord>): File {
        val exportFile = File(context.getExternalFilesDir(null), "quality_test_records.csv")
        exportFile.bufferedWriter().use { writer ->
            // 写入 CSV 头
            writer.write("日期,员工ID,生产线,缺陷类型,缺陷位置,缺陷严重度,测试结果,测试时长(秒)\n")

            // 写入记录
            records.forEach { record ->
                val line = StringBuilder().apply {
                    append(dateFormat.format(record.date))
                    append(",")
                    append(record.employeeId)
                    append(",")
                    append(record.productionLine)
                    append(",")
                    append(record.defectType)
                    append(",")
                    append(record.defectLocation)
                    append(",")
                    append(record.defectSeverity)
                    append(",")
                    append(if (record.isPassed) "合格" else "不合格")
                    append(",")
                    append(record.testDuration)
                }.toString()
                writer.write(line)
                writer.newLine()
            }
        }
        return exportFile
    }
}
