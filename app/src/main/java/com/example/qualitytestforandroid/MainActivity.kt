package com.example.qualitytestforandroid

import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var titleText: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var thumbnailList: RecyclerView
    private lateinit var submitButton: MaterialButton
    private lateinit var imageAdapter: ImageViewerAdapter
    private lateinit var thumbnailAdapter: ThumbnailAdapter
    
    private var currentProductionLine: String = ""
    private var currentDefectType: String = ""
    private val defectTypes = mutableMapOf<String, MutableList<File>>()
    private val completedDefectTypes = mutableSetOf<String>()
    private var currentImages = mutableListOf<File>()
    private var currentNGIndices = mutableSetOf<Int>()

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化视图
        initializeViews()
        
        // 获取当前产线
        currentProductionLine = intent.getStringExtra("PRODUCTION_LINE") ?: run {
            showError("未指定产线")
            finish()
            return
        }

        // 加载缺陷数据
        loadDefectData()
    }

    private fun initializeViews() {
        titleText = findViewById<TextView>(R.id.titleText)
        viewPager = findViewById<ViewPager2>(R.id.viewPager)
        thumbnailList = findViewById<RecyclerView>(R.id.thumbnailList)
        submitButton = findViewById<MaterialButton>(R.id.submitButton)

        // 图片选择回调
        imageAdapter = ImageViewerAdapter { position ->
            imageAdapter.toggleSelection(position)
            thumbnailAdapter.updateSelection(imageAdapter.getSelectedImages())
        }
        
        // 初始化ViewPager2和缩略图
        val startPosition = Int.MAX_VALUE / 2
        viewPager.adapter = imageAdapter
        viewPager.setCurrentItem(startPosition, false)

        // 初始化缩略图列表
        thumbnailAdapter = ThumbnailAdapter { clickedPosition ->
            // 计算当前在中间显示的实际图片位置
            val currentPosition = viewPager.currentItem
            val imageCount = currentImages.size
            val currentActualPosition = currentPosition % imageCount
            
            // 计算需要移动的位置数
            var delta = clickedPosition - currentActualPosition
            
            // 如果需要向前移动的距离大于图片总数的一半，那么向后移动会更快
            if (delta > imageCount / 2) {
                delta -= imageCount
            } 
            // 如果需要向后移动的距离大于图片总数的一半，那么向前移动会更快
            else if (delta < -imageCount / 2) {
                delta += imageCount
            }
            
            // 执行跳转
            viewPager.setCurrentItem(currentPosition + delta, true)
        }
        thumbnailList.layoutManager = GridLayoutManager(this, 2)
        thumbnailList.adapter = thumbnailAdapter

        // 监听ViewPager2的页面变化
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val actualPosition = position % currentImages.size
                Log.d(TAG, "Page selected: actual=$actualPosition, total=${currentImages.size}")
                thumbnailAdapter.updateSelection(imageAdapter.getSelectedImages())
            }
        })

        submitButton.setOnClickListener {
            checkAnswer()
        }
    }

    private fun loadDefectData() {
        try {
            val baseDir = getExternalFilesDir(null) ?: throw IllegalStateException("External storage is not available")
            val productionLineDir = File(baseDir, "production_lines/$currentProductionLine")
            val defectTypesFile = File(productionLineDir, "defect_types.txt")

            if (!defectTypesFile.exists()) {
                Log.e(TAG, "Defect types file not found: ${defectTypesFile.absolutePath}")
                showError("未找到产线${currentProductionLine}的缺陷类型数据")
                finish()
                return
            }

            // 读取缺陷类型列表并随机排序
            val defectTypesList = defectTypesFile.readLines()
                .filter { it.isNotBlank() }
                .shuffled()
            Log.d(TAG, "Loaded defect types: $defectTypesList")

            if (defectTypesList.isEmpty()) {
                Log.e(TAG, "No defect types found in file")
                showError("产线${currentProductionLine}没有定义缺陷类型")
                finish()
                return
            }

            // 检查每个缺陷类型的图片目录
            defectTypes.clear()
            defectTypesList.forEach { defectType ->
                val defectTypeDir = File(productionLineDir, defectType)
                val okDir = File(defectTypeDir, "OK")
                val ngDir = File(defectTypeDir, "NG")

                if (!okDir.exists()) okDir.mkdirs()
                if (!ngDir.exists()) ngDir.mkdirs()

                val okPhotos = okDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true)) } ?: emptyList()
                val ngPhotos = ngDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true)) } ?: emptyList()

                Log.d(TAG, "Defect type $defectType - OK photos: ${okPhotos.size}, NG photos: ${ngPhotos.size}")
                
                if (okPhotos.isNotEmpty() || ngPhotos.isNotEmpty()) {
                    defectTypes[defectType] = mutableListOf()
                } else {
                    Log.w(TAG, "No photos found for defect type: $defectType")
                }
            }

            // 开始测试第一个缺陷类型
            if (defectTypes.isNotEmpty()) {
                currentDefectType = defectTypes.keys.random()
                Log.d(TAG, "Starting test with defect type: $currentDefectType")
                loadRandomImages(currentProductionLine, currentDefectType)
            } else {
                Log.e(TAG, "No valid defect types with photos found")
                showError("没有可用的缺陷类型或缺陷类型中没有图片")
                finish()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading defect data", e)
            showError("加载缺陷数据时出错：${e.message}")
            finish()
        }
    }

    private fun loadRandomImages(productionLine: String, defectType: String) {
        try {
            val baseDir = getExternalFilesDir(null) ?: throw IllegalStateException("External storage is not available")
            val defectTypeDir = File(baseDir, "production_lines/$productionLine/$defectType")
            val okDir = File(defectTypeDir, "OK")
            val ngDir = File(defectTypeDir, "NG")

            val okPhotos = okDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true)) } ?: emptyList()
            val ngPhotos = ngDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true)) } ?: emptyList()

            if (okPhotos.isEmpty() && ngPhotos.isEmpty()) {
                Log.e(TAG, "No photos found for defect type: $defectType")
                showError("没有找到测试图片")
                return
            }

            // 随机选择4-6张图片
            val totalImages = Random.nextInt(4, 7)  // 4到6张图片
            val ngRatio = Random.nextDouble(0.6, 0.71)  // 60-70%的NG图片
            val ngCount = (totalImages * ngRatio).toInt().coerceAtLeast(1)  // 至少1张NG图片
            val okCount = totalImages - ngCount

            currentImages.clear()
            currentNGIndices.clear()

            // 添加NG图片
            if (ngPhotos.size >= ngCount) {
                ngPhotos.shuffled().take(ngCount).forEachIndexed { index, file ->
                    currentImages.add(file)
                    currentNGIndices.add(index)
                }

                // 添加OK图片
                if (okPhotos.size >= okCount) {
                    okPhotos.shuffled().take(okCount).forEach { file ->
                        currentImages.add(file)
                    }

                    // 随机打乱所有图片顺序
                    val shuffledImages = currentImages.shuffled()
                    currentNGIndices.clear()
                    shuffledImages.forEachIndexed { index, file ->
                        if (file.parent?.endsWith("NG") == true) {
                            currentNGIndices.add(index)
                        }
                    }
                    currentImages = shuffledImages.toMutableList()

                    // 更新适配器
                    imageAdapter.updateImages(currentImages)
                    thumbnailAdapter.updateImages(currentImages)
                    
                    // 设置ViewPager到中间位置以支持无限循环
                    viewPager.setCurrentItem(Int.MAX_VALUE / 2, false)
                    
                    Log.d(TAG, "Loaded ${currentImages.size} images (${currentNGIndices.size} NG)")

                    // 更新提示文本
                    updateInstructionText()
                } else {
                    showError("OK图片数量不足")
                }
            } else {
                showError("NG图片数量不足")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading images", e)
            showError("加载图片时出错：${e.message}")
        }
    }

    private fun checkAnswer() {
        val selectedIndices = imageAdapter.getSelectedImages()
        
        if (selectedIndices.isEmpty()) {
            showMessage("请至少选择一张有缺陷的图片")
            return
        }

        val isCorrect = selectedIndices == currentNGIndices
        if (isCorrect) {
            completedDefectTypes.add(currentDefectType)
            showSuccessDialog("答案正确！") {
                if (completedDefectTypes.size == defectTypes.size) {
                    showCompletionDialog()
                } else {
                    startNextDefectTest()
                }
            }
        } else {
            showErrorDialog("答案错误，请重新选择") {
                loadRandomImages(currentProductionLine, currentDefectType)
            }
        }
    }

    private fun startNextDefectTest() {
        // 获取未完成的缺陷类型
        val remainingDefectTypes = defectTypes.keys.filter { !completedDefectTypes.contains(it) }
        
        if (remainingDefectTypes.isEmpty()) {
            showCompletionDialog()
            return
        }

        // 随机选择一个未完成的缺陷类型
        currentDefectType = remainingDefectTypes.random()
        loadRandomImages(currentProductionLine, currentDefectType)
        
        // 更新提示文本
        updateInstructionText()
    }

    private fun showCompletionDialog() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("测试完成")
                .setMessage("恭喜你完成了所有缺陷类型的测试！")
                .setPositiveButton("确定") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, "Error: $message")
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessDialog(message: String, onConfirm: () -> Unit) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("测试结果")
                .setMessage(message)
                .setPositiveButton("确定") { _, _ -> onConfirm() }
                .setCancelable(false)
                .show()
        }
    }

    private fun showErrorDialog(message: String, onConfirm: () -> Unit) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("测试结果")
                .setMessage(message)
                .setPositiveButton("确定") { _, _ -> onConfirm() }
                .setCancelable(false)
                .show()
        }
    }

    private fun updateInstructionText() {
        val htmlText = "请找出图片中所有的<font color='#FF0000'><b>${currentDefectType}</b></font>缺陷"
        titleText.text = android.text.Html.fromHtml(htmlText, android.text.Html.FROM_HTML_MODE_COMPACT)
    }
}
