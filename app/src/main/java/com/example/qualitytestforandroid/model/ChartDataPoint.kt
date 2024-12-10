package com.example.qualitytestforandroid.model

data class ChartDataPoint(
    val productionLine: String,
    val result: String,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChartDataPoint

        if (productionLine != other.productionLine) return false
        if (result != other.result) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = productionLine.hashCode()
        result1 = 31 * result1 + result.hashCode()
        result1 = 31 * result1 + timestamp.hashCode()
        return result1
    }
}
