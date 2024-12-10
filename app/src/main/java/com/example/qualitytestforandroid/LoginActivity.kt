package com.example.qualitytestforandroid

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    private lateinit var employeeIdInput: TextInputEditText
    private lateinit var productionLineInput: MaterialAutoCompleteTextView
    private lateinit var loginButton: MaterialButton
    private lateinit var adminButton: MaterialButton
    private lateinit var productionLineManager: ProductionLineManager
    private lateinit var adapter: ArrayAdapter<String>

    private val productionLineChangeListener: () -> Unit = {
        // 当产线数据变化时更新下拉列表
        updateProductionLineDropdown()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 初始化ProductionLineManager
        productionLineManager = ProductionLineManager.getInstance(this)

        // 初始化视图
        employeeIdInput = findViewById(R.id.employeeIdInput)
        productionLineInput = findViewById(R.id.productionLineInput)
        loginButton = findViewById(R.id.loginButton)
        adminButton = findViewById(R.id.adminButton)

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
}
