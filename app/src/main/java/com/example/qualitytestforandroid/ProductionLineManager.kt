package com.example.qualitytestforandroid

import android.content.Context
import android.content.SharedPreferences

class ProductionLineManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val listeners = mutableListOf<() -> Unit>()

    companion object {
        private const val PREFS_NAME = "production_lines"
        private const val LINES_KEY = "lines"
        private const val DEFAULT_LINES = "产线1,产线2,产线3" // 默认产线列表

        @Volatile
        private var instance: ProductionLineManager? = null

        fun getInstance(context: Context): ProductionLineManager {
            return instance ?: synchronized(this) {
                instance ?: ProductionLineManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun addChangeListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeChangeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }

    fun getProductionLines(): List<String> {
        val linesStr = sharedPreferences.getString(LINES_KEY, DEFAULT_LINES) ?: DEFAULT_LINES
        return linesStr.split(",").filter { it.isNotEmpty() }
    }

    fun saveProductionLines(lines: List<String>) {
        val linesStr = lines.joinToString(",")
        sharedPreferences.edit().putString(LINES_KEY, linesStr).apply()
        notifyListeners() // 通知所有监听器数据已更新
    }
}
