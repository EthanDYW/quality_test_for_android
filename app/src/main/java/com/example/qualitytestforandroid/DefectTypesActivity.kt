package com.example.qualitytestforandroid

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.qualitytestforandroid.data.DefectTypeData
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import android.util.Log

class DefectTypesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DefectTypeAdapter
    private lateinit var saveButton: MaterialButton
    private lateinit var saveAndExitButton: MaterialButton
    private lateinit var exitWithoutSaveButton: MaterialButton
    private lateinit var addDefectButton: MaterialButton
    private lateinit var productionLine: String
    private var defectTypes = mutableListOf<String>()
    private var hasChanges = false

    companion object {
        private const val TAG = "DefectTypesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_defect_types)

        productionLine = intent.getStringExtra("PRODUCTION_LINE") ?: run {
            Log.e(TAG, "No production line provided")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        Log.d(TAG, "Production line: $productionLine")
        initViews()
        loadDefectTypes()
        setupBackPressHandler()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.defectTypesRecyclerView)
        saveButton = findViewById(R.id.saveButton)
        saveAndExitButton = findViewById(R.id.saveAndExitButton)
        exitWithoutSaveButton = findViewById(R.id.exitWithoutSaveButton)
        addDefectButton = findViewById(R.id.addDefectButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DefectTypeAdapter(this, defectTypes)
        recyclerView.adapter = adapter

        saveButton.setOnClickListener { 
            saveData()
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }
        
        saveAndExitButton.setOnClickListener { 
            saveData()
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            returnToAdmin()
        }
        
        exitWithoutSaveButton.setOnClickListener {
            if (hasChanges) {
                showUnsavedChangesDialog()
            } else {
                returnToAdmin()
            }
        }
        
        addDefectButton.setOnClickListener {
            defectTypes.add("")
            adapter.notifyItemInserted(defectTypes.size - 1)
            hasChanges = true
        }
    }

    private fun returnToAdmin() {
        Log.d(TAG, "returnToAdmin")
        setResult(RESULT_OK)
        finish()
    }

    private fun setupBackPressHandler() {
        Log.d(TAG, "setupBackPressHandler")
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: hasChanges=$hasChanges")
                if (hasChanges) {
                    showUnsavedChangesDialog()
                } else {
                    returnToAdmin()
                }
            }
        })
    }

    private fun showUnsavedChangesDialog() {
        Log.d(TAG, "showUnsavedChangesDialog")
        AlertDialog.Builder(this)
            .setTitle("未保存的更改")
            .setMessage("是否保存更改？")
            .setPositiveButton("保存") { _, _ ->
                Log.d(TAG, "Saving changes before return")
                saveData()
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                returnToAdmin()
            }
            .setNegativeButton("不保存") { _, _ ->
                Log.d(TAG, "Discarding changes and returning")
                returnToAdmin()
            }
            .setNeutralButton("取消", null)
            .show()
    }

    private fun saveData() {
        try {
            // 确保目录存在
            val productionLineDir = getProductionLineDir()
            if (!productionLineDir.exists()) {
                productionLineDir.mkdirs()
            }

            // 保存缺陷类型数据
            val defectTypesFile = File(productionLineDir, "defect_types.txt")
            defectTypesFile.writeText(defectTypes.joinToString("\n"))

            // 保存每个缺陷类型的照片数据
            val defectDataList = defectTypes.map { defectType ->
                DefectTypeData(
                    name = defectType,
                    okPhotos = getPhotoList(defectType, "OK"),
                    ngPhotos = getPhotoList(defectType, "NG")
                )
            }

            val defectDataFile = File(productionLineDir, "defect_data.bin")
            ObjectOutputStream(FileOutputStream(defectDataFile)).use { out ->
                out.writeObject(defectDataList)
            }

            hasChanges = false
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving data", e)
            Toast.makeText(this, "保存失败：${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getPhotoList(defectType: String, type: String): List<String> {
        try {
            val photoDir = File(getProductionLineDir(), "$defectType/$type")
            return if (photoDir.exists()) {
                photoDir.listFiles()
                    ?.filter { it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png") }
                    ?.map { it.absolutePath }
                    ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting photo list for $defectType/$type", e)
            return emptyList()
        }
    }

    private fun getProductionLineDir(): File {
        val baseDir = getExternalFilesDir(null) ?: throw IllegalStateException("External storage is not available")
        val dir = File(baseDir, "production_lines/$productionLine")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun loadDefectTypes() {
        val defectTypesFile = File(getProductionLineDir(), "defect_types.txt")
        if (defectTypesFile.exists()) {
            defectTypes.clear()
            defectTypes.addAll(defectTypesFile.readLines())
            adapter.notifyDataSetChanged()
        }
    }

    inner class DefectTypeAdapter(
        private val context: AppCompatActivity,
        private val defectTypes: MutableList<String>
    ) : RecyclerView.Adapter<DefectTypeAdapter.ViewHolder>() {

        private val editModeMap = mutableMapOf<Int, Boolean>()
        private val originalNames = mutableMapOf<Int, String>()

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameInput: TextInputEditText = view.findViewById(R.id.defectNameInput)
            val editButton: MaterialButton = view.findViewById(R.id.editButton)
            val saveButton: MaterialButton = view.findViewById(R.id.saveButton)
            val cancelButton: MaterialButton = view.findViewById(R.id.cancelButton)
            val photoButton: MaterialButton = view.findViewById(R.id.photoButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_defect_type, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val defectType = defectTypes[position]
            holder.nameInput.setText(defectType)

            val isEditMode = editModeMap[position] == true

            // 设置编辑模式状态
            holder.nameInput.isEnabled = isEditMode
            holder.editButton.visibility = if (isEditMode) View.GONE else View.VISIBLE
            holder.saveButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
            holder.cancelButton.visibility = if (isEditMode) View.VISIBLE else View.GONE

            // 编辑按钮点击事件
            holder.editButton.setOnClickListener {
                setEditMode(position, true)
            }

            // 照片按钮点击事件
            holder.photoButton.setOnClickListener {
                val intent = Intent(context, PhotoLibrarySelectorActivity::class.java).apply {
                    putExtra("PRODUCTION_LINE", productionLine)
                    putExtra("DEFECT_TYPE", defectTypes[position])
                }
                context.startActivity(intent)
            }

            // 保存按钮点击事件
            holder.saveButton.setOnClickListener {
                val newName = holder.nameInput.text?.toString()?.trim() ?: ""
                if (newName.isEmpty()) {
                    holder.nameInput.error = "缺陷类型名称不能为空"
                    return@setOnClickListener
                }

                if (defectTypes.indexOf(newName) != position && defectTypes.contains(newName)) {
                    holder.nameInput.error = "缺陷类型名称已存在"
                    return@setOnClickListener
                }

                defectTypes[position] = newName
                setEditMode(position, false)
                hasChanges = true
            }

            // 取消按钮点击事件
            holder.cancelButton.setOnClickListener {
                if (originalNames.containsKey(position)) {
                    // 恢复原始名称
                    defectTypes[position] = originalNames[position] ?: ""
                    holder.nameInput.setText(defectTypes[position])
                } else if (defectType.isEmpty()) {
                    // 如果是新增的空项，则移除
                    defectTypes.removeAt(position)
                    notifyItemRemoved(position)
                    return@setOnClickListener
                }
                setEditMode(position, false)
            }
        }

        override fun getItemCount() = defectTypes.size

        fun setEditMode(position: Int, isEdit: Boolean) {
            if (isEdit && !originalNames.containsKey(position)) {
                // 保存原始名称
                originalNames[position] = defectTypes[position]
            } else if (!isEdit) {
                // 清除原始名称
                originalNames.remove(position)
            }
            editModeMap[position] = isEdit
            notifyItemChanged(position)
        }
    }
}
