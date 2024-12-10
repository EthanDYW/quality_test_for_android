package com.example.qualitytestforandroid.chart

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.qualitytestforandroid.model.ChartDataPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class ChartDataManager private constructor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val dataPoints = Collections.synchronizedList(mutableListOf<ChartDataPoint>())
    private val productionLineData = HashMap<String, MutableList<ChartDataPoint>>()
    
    private val _chartDataLiveData = MutableLiveData<List<ChartDataPoint>>()
    val chartDataLiveData: LiveData<List<ChartDataPoint>> = _chartDataLiveData

    companion object {
        @Volatile
        private var instance: ChartDataManager? = null
        private const val MAX_DATA_POINTS = 1000 // 每个产线最多保存的数据点数量

        fun getInstance(context: Context): ChartDataManager {
            return instance ?: synchronized(this) {
                instance ?: ChartDataManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun addDataPoint(dataPoint: ChartDataPoint) {
        synchronized(dataPoints) {
            dataPoints.add(dataPoint)
            productionLineData.getOrPut(dataPoint.productionLine) { mutableListOf() }.add(dataPoint)
            
            // 限制数据点数量
            if (productionLineData[dataPoint.productionLine]?.size ?: 0 > MAX_DATA_POINTS) {
                productionLineData[dataPoint.productionLine]?.removeAt(0)
            }
            
            updateLiveData()
        }
    }

    fun getChartData(): List<ChartDataPoint> = dataPoints.toList()

    fun getChartDataByProductionLine(productionLine: String): List<ChartDataPoint> {
        return productionLineData[productionLine]?.toList() ?: emptyList()
    }

    fun getPassRateByProductionLine(productionLine: String): Double {
        val lineData = productionLineData[productionLine] ?: return 0.0
        if (lineData.isEmpty()) return 0.0
        
        val passCount = lineData.count { it.result == "PASS" }
        return passCount.toDouble() / lineData.size
    }

    fun getPassRatesByTimeRange(startTime: Long, endTime: Long): Map<String, Double> {
        return productionLineData.mapValues { (_, data) ->
            val filteredData = data.filter { it.timestamp in startTime..endTime }
            if (filteredData.isEmpty()) 0.0 else {
                val passCount = filteredData.count { it.result == "PASS" }
                passCount.toDouble() / filteredData.size
            }
        }
    }

    fun clearData() {
        synchronized(dataPoints) {
            dataPoints.clear()
            productionLineData.clear()
            updateLiveData()
        }
    }

    private fun updateLiveData() {
        scope.launch {
            _chartDataLiveData.value = dataPoints.toList()
        }
    }
}
