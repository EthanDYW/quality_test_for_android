package com.example.qualitytestforandroid.model

data class QualityTestRecord(
    val id: String,
    val productionLine: String,
    val testResult: String,
    val timestamp: Long
)
