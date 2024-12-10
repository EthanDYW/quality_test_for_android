package com.example.qualitytestforandroid.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qualitytestforandroid.data.ChartDataManager
import com.example.qualitytestforandroid.databinding.ActivityChartDisplayBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

class ChartDisplayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartDisplayBinding
    private lateinit var chartDataManager: ChartDataManager
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chartDataManager = ChartDataManager.getInstance(this)
        setupCharts()
        observeData()
    }

    private fun setupCharts() {
        setupLineChart()
        setupBarChart()
        setupPieChart()
    }

    private fun setupLineChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
                granularity = 20f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = true
            
            setExtraOffsets(10f, 10f, 10f, 20f)
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                granularity = 10f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = true
            
            setExtraOffsets(10f, 10f, 10f, 20f)
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "测试结果分布"
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            
            legend.apply {
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
            }
            
            setExtraOffsets(20f, 0f, 80f, 0f)
        }
    }

    private fun observeData() {
        chartDataManager.chartData.observe(this) { dataPoints ->
            updateCharts()
        }
    }

    private fun updateCharts() {
        updateLineChart()
        updateBarChart()
        updatePieChart()
    }

    private fun updateLineChart() {
        val (dates, values) = chartDataManager.getPassRateTrend()
        
        val entries = values.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }

        val dataSet = LineDataSet(entries, "合格率趋势").apply {
            color = ColorTemplate.MATERIAL_COLORS[0]
            setCircleColor(ColorTemplate.MATERIAL_COLORS[0])
            valueTextSize = 10f
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            invalidate()
        }
    }

    private fun updateBarChart() {
        val stats = chartDataManager.getProductionLineComparison()
        
        val entries = stats.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.average.toFloat())
        }

        val dataSet = BarDataSet(entries, "产线统计").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 10f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(stats.keys.toList())
            invalidate()
        }
    }

    private fun updatePieChart() {
        val distribution = chartDataManager.getResultDistribution()
        
        val entries = distribution.entries.map { (date, stats) ->
            PieEntry(stats.percentage.toFloat(), date)
        }

        val dataSet = PieDataSet(entries, "结果分布").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 10f
            valueFormatter = PercentFormatter()
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            invalidate()
        }
    }
}
