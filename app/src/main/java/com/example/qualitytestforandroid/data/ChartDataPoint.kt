package com.example.qualitytestforandroid.data

data class ChartDataPoint(
    val date: String,
    val value: Int,
    val timestamp: Long = System.currentTimeMillis()
)
