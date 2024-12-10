package com.example.qualitytestforandroid.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.qualitytestforandroid.R
import com.example.qualitytestforandroid.data.QualityTestRecord
import com.example.qualitytestforandroid.databinding.ActivityProductionLineBinding
import java.util.*

class ProductionLineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductionLineBinding
    private lateinit var defectTypeSpinner: Spinner
    private lateinit var defectLocationSpinner: Spinner
    private lateinit var defectSeveritySpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var productionLineTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductionLineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 获取传递的生产线信息
        val productionLine = intent.getStringExtra("PRODUCTION_LINE") ?: "未知生产线"
        
        initializeViews()
        setupSpinners()
        
        // 显示当前生产线
        productionLineTextView.text = "当前生产线: $productionLine"
        
        submitButton.setOnClickListener {
            submitDefectRecord()
        }
    }
    
    private fun initializeViews() {
        defectTypeSpinner = binding.defectTypeSpinner
        defectLocationSpinner = binding.defectLocationSpinner
        defectSeveritySpinner = binding.defectSeveritySpinner
        submitButton = binding.submitButton
        productionLineTextView = binding.productionLineTextView
    }
    
    private fun setupSpinners() {
        // 设置缺陷类型选项
        ArrayAdapter.createFromResource(
            this,
            R.array.defect_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            defectTypeSpinner.adapter = adapter
        }
        
        // 设置缺陷位置选项
        ArrayAdapter.createFromResource(
            this,
            R.array.defect_locations,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            defectLocationSpinner.adapter = adapter
        }
        
        // 设置缺陷严重程度选项
        ArrayAdapter.createFromResource(
            this,
            R.array.defect_severities,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            defectSeveritySpinner.adapter = adapter
        }
    }
    
    private fun submitDefectRecord() {
        val defectType = defectTypeSpinner.selectedItem.toString()
        val defectLocation = defectLocationSpinner.selectedItem.toString()
        val defectSeverity = defectSeveritySpinner.selectedItem.toString()
        
        val record = QualityTestRecord(
            employeeId = "EMP001", // TODO: 使用实际的员工ID
            date = Date(),
            productionLine = productionLineTextView.text.toString().substringAfter(": "),
            defectType = defectType,
            defectLocation = defectLocation,
            defectSeverity = defectSeverity,
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis(),
            defectTypeErrors = mapOf(defectType to 1),
            isPassed = false,
            testDuration = 0
        )
        
        // TODO: 保存记录到数据库或发送到服务器
        
        Toast.makeText(this, "缺陷记录已提交", Toast.LENGTH_SHORT).show()
        
        // 重置选择
        defectTypeSpinner.setSelection(0)
        defectLocationSpinner.setSelection(0)
        defectSeveritySpinner.setSelection(0)
    }
}
