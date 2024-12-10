package com.example.qualitytestforandroid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.qualitytestforandroid.data.TestRecordDatabase
import com.google.android.material.button.MaterialButton
import java.util.*

class ExportRecordsActivity : AppCompatActivity() {
    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    private lateinit var exportButton: MaterialButton
    private lateinit var backButton: MaterialButton
    private lateinit var database: TestRecordDatabase
    private lateinit var excelExporter: ExcelExporter

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_records)

        // 初始化视图
        startDatePicker = findViewById(R.id.startDatePicker)
        endDatePicker = findViewById(R.id.endDatePicker)
        exportButton = findViewById(R.id.exportButton)
        backButton = findViewById(R.id.backButton)

        // 初始化数据库和导出器
        database = TestRecordDatabase(this)
        excelExporter = ExcelExporter(this)

        // 设置默认日期范围（当前月份）
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        startDatePicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDatePicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // 设置按钮点击事件
        exportButton.setOnClickListener {
            if (checkStoragePermission()) {
                exportRecords()
            } else {
                requestStoragePermission()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportRecords()
            } else {
                Toast.makeText(this, "需要存储权限才能导出文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportRecords() {
        try {
            // 检查外部存储是否可用
            if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                Toast.makeText(this, "外部存储不可用", Toast.LENGTH_SHORT).show()
                return
            }

            // 获取选择的日期范围
            val startCalendar = Calendar.getInstance().apply {
                set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCalendar = Calendar.getInstance().apply {
                set(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val startDate = startCalendar.time
            val endDate = endCalendar.time

            // 验证日期范围
            if (startDate.after(endDate)) {
                Toast.makeText(this, "开始日期不能晚于结束日期", Toast.LENGTH_SHORT).show()
                return
            }

            // 获取记录并导出
            val records = database.getRecordsByDateRange(startDate, endDate)
            if (records.isEmpty()) {
                Toast.makeText(this, "所选日期范围内没有记录", Toast.LENGTH_SHORT).show()
                return
            }

            // 执行导出
            val file = excelExporter.exportToExcel(records)
            Toast.makeText(this, "记录已导出到: ${file.absolutePath}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
