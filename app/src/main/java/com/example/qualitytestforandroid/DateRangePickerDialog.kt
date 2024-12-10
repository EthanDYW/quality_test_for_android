package com.example.qualitytestforandroid

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.DatePicker
import java.util.*

class DateRangePickerDialog(
    context: Context,
    private val onDateRangeSelected: (startDate: Date, endDate: Date) -> Unit
) : Dialog(context) {

    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_date_range_picker)

        // 初始化视图
        startDatePicker = findViewById(R.id.startDatePicker)
        endDatePicker = findViewById(R.id.endDatePicker)
        confirmButton = findViewById(R.id.confirmButton)
        cancelButton = findViewById(R.id.cancelButton)

        // 设置按钮点击事件
        confirmButton.setOnClickListener {
            val startDate = Calendar.getInstance().apply {
                set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endDate = Calendar.getInstance().apply {
                set(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            onDateRangeSelected(startDate, endDate)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }
}
