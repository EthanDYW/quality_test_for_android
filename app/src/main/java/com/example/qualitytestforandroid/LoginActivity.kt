package com.example.qualitytestforandroid

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.example.qualitytestforandroid.data.TestRecordDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var employeeIdInput: TextInputEditText
    private lateinit var productionLineInput: MaterialAutoCompleteTextView
    private lateinit var loginButton: MaterialButton
    private lateinit var adminButton: MaterialButton
    private lateinit var exportButton: MaterialButton
    private lateinit var productionLineManager: ProductionLineManager
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var testRecordDatabase: TestRecordDatabase
    private val STORAGE_PERMISSION_CODE = 1001

    private val productionLineChangeListener: () -> Unit = {
        // 当产线数据变化时更新下拉列表
        updateProductionLineDropdown()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 初始化数据库
        testRecordDatabase = TestRecordDatabase(this)

        // 初始化ProductionLineManager
        productionLineManager = ProductionLineManager.getInstance(this)

        // 初始化视图
        employeeIdInput = findViewById(R.id.employeeIdInput)
        productionLineInput = findViewById(R.id.productionLineInput)
        loginButton = findViewById(R.id.loginButton)
        adminButton = findViewById(R.id.adminButton)
        exportButton = findViewById(R.id.exportButton)

        // 设置产线下拉列表
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        productionLineInput.setAdapter(adapter)
        updateProductionLineDropdown()

        // 添加产线数据变化监听器
        productionLineManager.addChangeListener(productionLineChangeListener)

        // 设置登录按钮点击事件
        loginButton.setOnClickListener {
            val employeeId = employeeIdInput.text?.toString() ?: ""
            val productionLine = productionLineInput.text?.toString() ?: ""

            if (validateInput(employeeId, productionLine)) {
                // 跳转到主界面
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("EMPLOYEE_ID", employeeId)
                intent.putExtra("PRODUCTION_LINE", productionLine)
                startActivity(intent)
            }
        }

        // 设置管理员按钮点击事件
        adminButton.setOnClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
        }

        // 设置导出按钮点击事件
        exportButton.setOnClickListener {
            val intent = Intent(this, ExportRecordsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除监听器，避免内存泄漏
        productionLineManager.removeChangeListener(productionLineChangeListener)
    }

    private fun updateProductionLineDropdown() {
        val productionLines = productionLineManager.getProductionLines()
        adapter.clear()
        adapter.addAll(productionLines)
        adapter.notifyDataSetChanged()
    }

    private fun validateInput(employeeId: String, productionLine: String): Boolean {
        var isValid = true

        if (employeeId.isEmpty()) {
            employeeIdInput.error = "请输入工号"
            isValid = false
        }

        if (productionLine.isEmpty()) {
            productionLineInput.error = "请选择产线"
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, "请填写所有必填项", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun showDateRangePicker() {
        DateRangePickerDialog(this) { startDate, endDate ->
            try {
                val records = testRecordDatabase.getRecordsByDateRange(startDate, endDate)
                if (records.isEmpty()) {
                    Toast.makeText(this, "所选时间范围内没有测试记录", Toast.LENGTH_SHORT).show()
                    return@DateRangePickerDialog
                }

                val exporter = ExcelExporter(this)
                val file = exporter.exportToExcel(records)
                Toast.makeText(this, "记录已导出到: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDateRangePicker()
            } else {
                Toast.makeText(this, "需要存储权限才能导出记录", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
