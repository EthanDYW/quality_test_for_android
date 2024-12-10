package com.example.qualitytestforandroid.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import java.io.File
import java.util.*

class ChartDataManager private constructor(private val context: Context) {
    private val _chartData = MutableLiveData<List<ChartDataPoint>>()
    val chartData: LiveData<List<ChartDataPoint>> = _chartData
    private val dataPoints = mutableListOf<ChartDataPoint>()
    private val dataFile = File(context.filesDir, "chart_data.json")

    companion object {
        @Volatile
        private var instance: ChartDataManager? = null
        private const val TIME_WINDOW_MINUTES = 60 // 默认时间窗口为60分钟

        fun getInstance(context: Context): ChartDataManager {
            return instance ?: synchronized(this) {
                instance ?: ChartDataManager(context).also { instance = it }
            }
        }
    }

    init {
        loadData()
    }

    fun addDataPoint(point: ChartDataPoint) {
        dataPoints.add(point)
        _chartData.postValue(dataPoints.toList())
        saveData()
    }

    fun getDataPoints(): List<ChartDataPoint> = dataPoints.toList()

    fun getPassRateTrend(timeWindowMinutes: Int = TIME_WINDOW_MINUTES): Pair<List<String>, List<Double>> {
        if (dataPoints.isEmpty()) return Pair(emptyList(), emptyList())

        val dates = mutableListOf<String>()
        val values = mutableListOf<Double>()

        // 按日期分组并计算每天的平均值
        dataPoints.groupBy { it.date }
            .toSortedMap()
            .forEach { (date, points) ->
                dates.add(date)
                values.add(points.map { it.value.toDouble() }.average())
            }

        return Pair(dates, values)
    }

    fun getProductionLineComparison(): Map<String, ProductionLineStats> {
        if (dataPoints.isEmpty()) return emptyMap()

        return dataPoints.groupBy { it.date }
            .mapValues { (_, points) ->
                val values = points.map { it.value }
                ProductionLineStats(
                    total = values.size,
                    average = values.average(),
                    max = values.maxOrNull() ?: 0,
                    min = values.minOrNull() ?: 0
                )
            }
    }

    fun getResultDistribution(): Map<String, ResultStats> {
        if (dataPoints.isEmpty()) return emptyMap()

        val total = dataPoints.size.toDouble()
        return dataPoints.groupBy { it.date }
            .mapValues { (_, points) ->
                val count = points.size
                ResultStats(
                    count = count,
                    percentage = (count / total) * 100
                )
            }
    }

    private fun loadData() {
        if (dataFile.exists()) {
            val jsonArray = JSONObject(dataFile.readText()).getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                dataPoints.add(
                    ChartDataPoint(
                        date = obj.getString("date"),
                        value = obj.getInt("value"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
            _chartData.postValue(dataPoints.toList())
        }
    }

    private fun saveData() {
        val jsonObject = JSONObject()
        val jsonArray = dataPoints.map { point ->
            JSONObject().apply {
                put("date", point.date)
                put("value", point.value)
                put("timestamp", point.timestamp)
            }
        }
        jsonObject.put("data", jsonArray)
        dataFile.writeText(jsonObject.toString())
    }

    data class ProductionLineStats(
        val total: Int,
        val average: Double,
        val max: Int,
        val min: Int
    )

    data class ResultStats(
        val count: Int,
        val percentage: Double
    )
}
