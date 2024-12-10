package com.example.qualitytestforandroid.data

import java.util.Date

data class TestRecord(
    val employeeId: String,
    val productionLine: String,
    val startTime: Date,
    val endTime: Date,
    val defectTypeErrors: Map<String, Int>,
    val date: Date,
    val defectType: String,
    val isPassed: Boolean,
    val testDuration: Long
) {
    companion object {
        fun fromQualityTestRecord(record: QualityTestRecord): TestRecord {
            return TestRecord(
                employeeId = record.employeeId,
                productionLine = record.productionLine,
                startTime = Date(record.startTime),
                endTime = Date(record.endTime),
                defectTypeErrors = record.defectTypeErrors,
                date = record.date,
                defectType = record.defectType,
                isPassed = record.isPassed,
                testDuration = record.testDuration.toLong()
            )
        }
    }
}
