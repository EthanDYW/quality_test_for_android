package com.example.qualitytestforandroid

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.util.Log
import java.io.File

class AdminMainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductionLineAdapter
    private lateinit var addLineButton: MaterialButton
    private lateinit var deleteLineButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var saveAndExitButton: MaterialButton
    private lateinit var exitWithoutSaveButton: MaterialButton
    lateinit var productionLineManager: ProductionLineManager
    
    private var isAddMode = false
    var isDeleteMode = false 
    private lateinit var originalLines: List<String>
    private lateinit var currentLines: MutableList<String>
    var hasUnsavedChanges = false

    companion object {
        private const val TAG = "AdminMainActivity"
        private const val REQUEST_CODE_DEFECT_TYPES = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_admin_main)

        // 防止重新创建时重复初始化
        if (savedInstanceState == null) {
            initializeData()
        }
        initializeViews()
    }

    private fun initializeData() {
        Log.d(TAG, "initializeData")
        productionLineManager = ProductionLineManager.getInstance(this)
        originalLines = productionLineManager.getProductionLines()
        currentLines = originalLines.toMutableList()
    }

    private fun initializeViews() {
        Log.d(TAG, "initializeViews")
        recyclerView = findViewById(R.id.recyclerView)
        addLineButton = findViewById(R.id.addLineButton)
        deleteLineButton = findViewById(R.id.deleteLineButton)
        saveButton = findViewById(R.id.saveButton)
        saveAndExitButton = findViewById(R.id.saveAndExitButton)
        exitWithoutSaveButton = findViewById(R.id.exitWithoutSaveButton)

        adapter = ProductionLineAdapter(
            currentLines,
            onEditClick = { position ->
                // 处理编辑按钮点击
            },
            onDeleteClick = { position ->
                // 处理删除按钮点击
                currentLines.removeAt(position)
                adapter.notifyItemRemoved(position)
                hasUnsavedChanges = true
            },
            onDefectTypesClick = { productionLine ->
                // 处理缺陷类型按钮点击
                if (hasUnsavedChanges) {
                    AlertDialog.Builder(this)
                        .setTitle("未保存的更改")
                        .setMessage("在进入缺陷类型管理前，是否保存当前更改？")
                        .setPositiveButton("保存并继续") { _, _ ->
                            productionLineManager.saveProductionLines(currentLines)
                            hasUnsavedChanges = false
                            startDefectTypesActivity(productionLine)
                        }
                        .setNegativeButton("放弃更改") { _, _ ->
                            currentLines = originalLines.toMutableList()
                            adapter.updateProductionLines(currentLines)
                            hasUnsavedChanges = false
                            startDefectTypesActivity(productionLine)
                        }
                        .setNeutralButton("取消", null)
                        .show()
                } else {
                    startDefectTypesActivity(productionLine)
                }
            },
            this 
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, "onRestoreInstanceState")
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

    private fun startDefectTypesActivity(productionLine: String) {
        Log.d(TAG, "startDefectTypesActivity: $productionLine")
        val intent = Intent(this, DefectTypesActivity::class.java).apply {
            putExtra("PRODUCTION_LINE", productionLine)
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_DEFECT_TYPES)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting DefectTypesActivity", e)
            Toast.makeText(this, "启动缺陷类型管理失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_CODE_DEFECT_TYPES) {
            // 从DefectTypesActivity返回后的处理
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "DefectTypesActivity returned OK")
            } else {
                Log.d(TAG, "DefectTypesActivity returned CANCELED")
            }
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed")
        if (hasUnsavedChanges) {
            showExitConfirmDialog()
        } else {
            exitToLogin()
        }
    }

    private fun exitToLogin() {
        Log.d(TAG, "exitToLogin")
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun setupButtons() {
        addLineButton.setOnClickListener {
            toggleAddMode()
        }

        deleteLineButton.setOnClickListener {
            toggleDeleteMode()
        }

        saveButton.setOnClickListener {
            productionLineManager.saveProductionLines(currentLines)
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            hasUnsavedChanges = false
        }

        saveAndExitButton.setOnClickListener {
            productionLineManager.saveProductionLines(currentLines)
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            exitToLogin()
        }

        exitWithoutSaveButton.setOnClickListener {
            showExitConfirmDialog()
        }
    }

    private fun toggleAddMode() {
        isAddMode = !isAddMode
        if (isAddMode) {
            currentLines.add("")
            adapter.notifyItemInserted(currentLines.size - 1)
            addLineButton.text = "取消增加产线"
            hasUnsavedChanges = true
        } else {
            if (currentLines.isNotEmpty() && currentLines[currentLines.size - 1].isEmpty()) {
                currentLines.removeAt(currentLines.size - 1)
                adapter.notifyItemRemoved(currentLines.size)
            }
            addLineButton.text = "增加产线"
        }
    }

    private fun toggleDeleteMode() {
        isDeleteMode = !isDeleteMode
        adapter.notifyDataSetChanged()
        deleteLineButton.text = if (isDeleteMode) "取消删除" else "删除产线"
    }

    private fun showExitConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("未保存的更改")
            .setMessage("是否保存更改后退出？")
            .setPositiveButton("保存并退出") { _, _ ->
                productionLineManager.saveProductionLines(currentLines)
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                exitToLogin()
            }
            .setNegativeButton("放弃更改") { _, _ ->
                exitToLogin()
            }
            .setNeutralButton("取消", null)
            .show()
    }
}

class ProductionLineAdapter(
    private var productionLines: MutableList<String>,
    private val onEditClick: (position: Int) -> Unit,
    private val onDeleteClick: (position: Int) -> Unit,
    private val onDefectTypesClick: (productionLine: String) -> Unit,
    private val activity: AdminMainActivity 
) : RecyclerView.Adapter<ProductionLineAdapter.ViewHolder>() {

    private fun renameProductionLineDirectory(oldName: String, newName: String) {
        try {
            val baseDir = activity.getExternalFilesDir(null)
            val oldDir = File(baseDir, "production_lines/$oldName")
            val newDir = File(baseDir, "production_lines/$newName")

            if (oldDir.exists()) {
                // 确保目标目录的父目录存在
                newDir.parentFile?.mkdirs()
                
                // 重命名目录
                val success = oldDir.renameTo(newDir)
                if (!success) {
                    // 如果重命名失败，尝试复制文件夹内容
                    oldDir.copyRecursively(newDir, true)
                    oldDir.deleteRecursively()
                }
            }
        } catch (e: Exception) {
            Log.e("ProductionLineAdapter", "Error renaming directory", e)
            Toast.makeText(activity, "重命名文件夹失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextInputEditText = view.findViewById(R.id.lineNameInput)
        val editButton: MaterialButton = view.findViewById(R.id.editButton)
        val deleteButton: MaterialButton = view.findViewById(R.id.deleteButton)
        val defectTypesButton: MaterialButton = view.findViewById(R.id.defectTypesButton)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_production_line, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productionLine = productionLines[position]
        holder.textView.setText(productionLine)
        holder.textView.isEnabled = false

        holder.editButton.setOnClickListener {
            if (holder.textView.isEnabled) {
                // 保存修改
                val newName = holder.textView.text?.toString() ?: ""
                if (newName.isNotEmpty() && newName != productionLine) {
                    // 先重命名文件夹
                    renameProductionLineDirectory(productionLine, newName)
                    // 再更新列表
                    productionLines[position] = newName
                    notifyItemChanged(position)
                    activity.hasUnsavedChanges = true
                }
                holder.textView.isEnabled = false
                holder.editButton.text = "编辑"
            } else {
                // 进入编辑模式
                holder.textView.isEnabled = true
                holder.editButton.text = "保存"
                holder.textView.requestFocus()
            }
        }

        // 处理回车键保存
        holder.textView.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val newName = holder.textView.text?.toString() ?: ""
                if (newName.isNotEmpty() && newName != productionLine) {
                    // 先重命名文件夹
                    renameProductionLineDirectory(productionLine, newName)
                    // 再更新列表
                    productionLines[position] = newName
                    notifyItemChanged(position)
                    activity.hasUnsavedChanges = true
                }
                holder.textView.isEnabled = false
                holder.editButton.text = "编辑"
                true
            } else {
                false
            }
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(position)
            activity.hasUnsavedChanges = true
        }

        holder.defectTypesButton.setOnClickListener {
            if (holder.textView.isEnabled) {
                Toast.makeText(activity, "请先保存当前编辑", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onDefectTypesClick(productionLine)
        }

        holder.checkBox.visibility = if (activity.isDeleteMode) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = productionLines.size

    fun updateProductionLines(newLines: List<String>) {
        productionLines.clear()
        productionLines.addAll(newLines)
        notifyDataSetChanged()
    }
}