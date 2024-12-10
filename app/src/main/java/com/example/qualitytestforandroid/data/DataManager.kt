package com.example.qualitytestforandroid.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.qualitytestforandroid.data.QualityTestRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class DataManager private constructor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val records = ConcurrentHashMap<String, QualityTestRecord>()
    private val _testRecordsLiveData = MutableLiveData<List<QualityTestRecord>>()
    val testRecordsLiveData: LiveData<List<QualityTestRecord>> = _testRecordsLiveData

    companion object {
        @Volatile
        private var instance: DataManager? = null

        fun getInstance(context: Context): DataManager {
            return instance ?: synchronized(this) {
                instance ?: DataManager(context).also { instance = it }
            }
        }
    }

    fun addTestRecord(record: QualityTestRecord) {
        scope.launch {
            records[record.employeeId] = record
            _testRecordsLiveData.value = records.values.toList()
        }
    }

    fun getTestRecords(): List<QualityTestRecord> {
        return records.values.toList()
    }

    fun clearRecords() {
        scope.launch {
            records.clear()
            _testRecordsLiveData.value = emptyList()
        }
    }
}
