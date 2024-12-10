package com.example.qualitytestforandroid.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class TestRecordDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "quality_test.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_RECORDS = "records"
        private const val TABLE_ERRORS = "errors"

        // Records表字段
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMPLOYEE_ID = "employee_id"
        private const val COLUMN_PRODUCTION_LINE = "production_line"
        private const val COLUMN_START_TIME = "start_time"
        private const val COLUMN_END_TIME = "end_time"
        private const val COLUMN_DEFECT_TYPE = "defect_type"
        private const val COLUMN_DEFECT_LOCATION = "defect_location"
        private const val COLUMN_DEFECT_SEVERITY = "defect_severity"
        private const val COLUMN_IS_PASSED = "is_passed"
        private const val COLUMN_TEST_DURATION = "test_duration"

        // Errors表字段
        private const val COLUMN_RECORD_ID = "record_id"
        private const val COLUMN_ERROR_TYPE = "error_type"
        private const val COLUMN_ERROR_COUNT = "error_count"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(db: SQLiteDatabase) {
        // 创建记录表
        val createRecordsTable = """
            CREATE TABLE $TABLE_RECORDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMPLOYEE_ID TEXT NOT NULL,
                $COLUMN_PRODUCTION_LINE TEXT NOT NULL,
                $COLUMN_START_TIME TEXT NOT NULL,
                $COLUMN_END_TIME TEXT NOT NULL,
                $COLUMN_DEFECT_TYPE TEXT,
                $COLUMN_DEFECT_LOCATION TEXT,
                $COLUMN_DEFECT_SEVERITY TEXT,
                $COLUMN_IS_PASSED INTEGER,
                $COLUMN_TEST_DURATION INTEGER
            )
        """.trimIndent()

        // 创建错误表
        val createErrorsTable = """
            CREATE TABLE $TABLE_ERRORS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RECORD_ID INTEGER NOT NULL,
                $COLUMN_ERROR_TYPE TEXT NOT NULL,
                $COLUMN_ERROR_COUNT INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_RECORD_ID) REFERENCES $TABLE_RECORDS($COLUMN_ID)
            )
        """.trimIndent()

        db.execSQL(createRecordsTable)
        db.execSQL(createErrorsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 简单升级策略：删除旧表，创建新表
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ERRORS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
        onCreate(db)
    }

    fun insertRecord(record: QualityTestRecord): Long {
        val db = writableDatabase

        // 插入记录
        val recordValues = ContentValues().apply {
            put(COLUMN_EMPLOYEE_ID, record.employeeId)
            put(COLUMN_PRODUCTION_LINE, record.productionLine)
            put(COLUMN_START_TIME, dateFormat.format(Date(record.startTime)))
            put(COLUMN_END_TIME, dateFormat.format(Date(record.endTime)))
            put(COLUMN_DEFECT_TYPE, record.defectType)
            put(COLUMN_DEFECT_LOCATION, record.defectLocation)
            put(COLUMN_DEFECT_SEVERITY, record.defectSeverity)
            put(COLUMN_IS_PASSED, if (record.isPassed) 1 else 0)
            put(COLUMN_TEST_DURATION, record.testDuration)
        }

        val recordId = db.insert(TABLE_RECORDS, null, recordValues)

        // 插入错误记录
        if (recordId != -1L) {
            record.defectTypeErrors.forEach { (type, count) ->
                val errorValues = ContentValues().apply {
                    put(COLUMN_RECORD_ID, recordId)
                    put(COLUMN_ERROR_TYPE, type)
                    put(COLUMN_ERROR_COUNT, count)
                }
                db.insert(TABLE_ERRORS, null, errorValues)
            }
        }

        return recordId
    }

    fun getRecordsByDateRange(startDate: Date, endDate: Date): List<QualityTestRecord> {
        val records = mutableListOf<QualityTestRecord>()
        val db = readableDatabase

        val selection = "$COLUMN_START_TIME BETWEEN ? AND ?"
        val selectionArgs = arrayOf(
            dateFormat.format(startDate),
            dateFormat.format(endDate)
        )

        db.query(
            TABLE_RECORDS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_START_TIME ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val recordId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                
                // 获取错误记录
                val defectTypeErrors = mutableMapOf<String, Int>()
                db.query(
                    TABLE_ERRORS,
                    null,
                    "$COLUMN_RECORD_ID = ?",
                    arrayOf(recordId.toString()),
                    null,
                    null,
                    null
                ).use { errorsCursor ->
                    while (errorsCursor.moveToNext()) {
                        val errorType = errorsCursor.getString(errorsCursor.getColumnIndexOrThrow(COLUMN_ERROR_TYPE))
                        val errorCount = errorsCursor.getInt(errorsCursor.getColumnIndexOrThrow(COLUMN_ERROR_COUNT))
                        defectTypeErrors[errorType] = errorCount
                    }
                }

                // 创建记录对象
                records.add(
                    QualityTestRecord(
                        employeeId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMPLOYEE_ID)),
                        date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)))!!,
                        productionLine = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTION_LINE)),
                        defectType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEFECT_TYPE)),
                        defectLocation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEFECT_LOCATION)),
                        defectSeverity = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEFECT_SEVERITY)),
                        startTime = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)))!!.time,
                        endTime = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)))!!.time,
                        defectTypeErrors = defectTypeErrors,
                        isPassed = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PASSED)) == 1,
                        testDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TEST_DURATION))
                    )
                )
            }
        }

        return records
    }
}
