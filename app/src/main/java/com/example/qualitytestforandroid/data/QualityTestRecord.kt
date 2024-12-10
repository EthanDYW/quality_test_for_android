package com.example.qualitytestforandroid.data

import org.json.JSONObject
import java.util.Date

data class QualityTestRecord(
    val employeeId: String,
    val date: Date,
    val productionLine: String,
    val defectType: String,
    val defectLocation: String,
    val defectSeverity: String,
    val startTime: Long,
    val endTime: Long,
    val defectTypeErrors: Map<String, Int>,
    val isPassed: Boolean,
    val testDuration: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("employeeId", employeeId)
            put("date", date.time)
            put("productionLine", productionLine)
            put("defectType", defectType)
            put("defectLocation", defectLocation)
            put("defectSeverity", defectSeverity)
            put("startTime", startTime)
            put("endTime", endTime)
            put("defectTypeErrors", JSONObject(defectTypeErrors.mapValues { it.value }))
            put("isPassed", isPassed)
            put("testDuration", testDuration)
            put("timestamp", timestamp)
        }
    }
}
