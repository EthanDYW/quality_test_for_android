package com.example.qualitytestforandroid

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream

class PhotoLibraryActivity : AppCompatActivity() {
    private lateinit var toolbarTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomBar: LinearLayout
    private lateinit var deleteSelectedFab: FloatingActionButton
    private lateinit var adapter: PhotoAdapter
    private lateinit var productionLine: String
    private lateinit var defectType: String
    private lateinit var libraryType: String
    private var photos = mutableListOf<String>()
    private var selectedPhotos = mutableSetOf<Int>()
    private var isSelectionMode = false

    companion object {
        private const val VIEW_TYPE_PHOTO = 0
        private const val VIEW_TYPE_ADD = 1
        private const val VIEW_TYPE_REMOVE = 2
    }

    private val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let { selectedUris ->
            if (selectedUris.isNotEmpty()) {
                showAddPhotosDialog(selectedUris)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_library)

        productionLine = intent.getStringExtra("PRODUCTION_LINE") ?: return
        defectType = intent.getStringExtra("DEFECT_TYPE") ?: return
        libraryType = intent.getStringExtra("LIBRARY_TYPE") ?: return

        toolbarTitle = findViewById(R.id.toolbarTitle)
        recyclerView = findViewById(R.id.photoRecyclerView)
        bottomBar = findViewById(R.id.bottomBar)
        deleteSelectedFab = findViewById(R.id.deleteSelectedFab)

        toolbarTitle.text = "$defectType - ${libraryType}照片库"

        // 设置删除按钮点击事件
        deleteSelectedFab.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // 设置网格布局
        val gridLayoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = gridLayoutManager

        loadPhotos()
        adapter = PhotoAdapter(photos,
            onItemClick = { position ->
                when {
                    position == photos.size -> openImagePicker() // 添加按钮
                    position == photos.size + 1 -> startSelectionMode() // 删除按钮
                    isSelectionMode -> togglePhotoSelection(position) // 选择照片
                }
            }
        )
        recyclerView.adapter = adapter
    }

    private fun startSelectionMode() {
        isSelectionMode = true
        selectedPhotos.clear()
        deleteSelectedFab.visibility = View.GONE
        adapter.notifyDataSetChanged()
    }

    private fun togglePhotoSelection(position: Int) {
        if (position in 0 until photos.size) {
            if (selectedPhotos.contains(position)) {
                selectedPhotos.remove(position)
            } else {
                selectedPhotos.add(position)
            }
            adapter.notifyItemChanged(position)
            updateDeleteFabVisibility()
        }
    }

    private fun updateDeleteFabVisibility() {
        deleteSelectedFab.visibility = if (selectedPhotos.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除选中的${selectedPhotos.size}张照片吗？")
            .setPositiveButton("确定") { _, _ ->
                deleteSelectedPhotos()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteSelectedPhotos() {
        val photosToDelete = selectedPhotos.sortedDescending().map { photos[it] }
        photosToDelete.forEach { photoPath ->
            File(photoPath).delete()
            photos.remove(photoPath)
        }
        selectedPhotos.clear()
        isSelectionMode = false
        deleteSelectedFab.visibility = View.GONE
        adapter.notifyDataSetChanged()
    }

    private fun loadPhotos() {
        val photoDir = getPhotoDirectory()
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }
        photos = photoDir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png") }
            ?.map { it.absolutePath }
            ?.toMutableList() ?: mutableListOf()
    }

    private fun openImagePicker() {
        pickImages.launch("image/*")
    }

    private fun showAddPhotosDialog(uris: List<Uri>) {
        AlertDialog.Builder(this)
            .setTitle("添加照片")
            .setMessage("确定要添加选中的${uris.size}张照片吗？")
            .setPositiveButton("确定") { _, _ ->
                savePhotos(uris)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun savePhotos(uris: List<Uri>) {
        val photoDir = getPhotoDirectory()
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }

        uris.forEach { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val fileName = "photo_${System.currentTimeMillis()}_${photos.size}.jpg"
                val file = File(photoDir, fileName)
                
                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                photos.add(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun getPhotoDirectory(): File {
        val baseDir = getExternalFilesDir(null)
        return File(baseDir, "production_lines/$productionLine/$defectType/$libraryType")
    }

    inner class PhotoAdapter(
        private val photos: List<String>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.photoImageView)
            val checkmarkImageView: ImageView = view.findViewById(R.id.checkmarkImageView)
            val cardView: MaterialCardView = view as MaterialCardView

            init {
                cardView.setOnClickListener {
                    onItemClick(adapterPosition)
                }
            }
        }

        inner class ActionButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.photoImageView)
            
            init {
                view.setOnClickListener {
                    onItemClick(adapterPosition)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                photos.size -> VIEW_TYPE_ADD
                photos.size + 1 -> VIEW_TYPE_REMOVE
                else -> VIEW_TYPE_PHOTO
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_PHOTO -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_photo, parent, false)
                    PhotoViewHolder(view)
                }
                VIEW_TYPE_ADD, VIEW_TYPE_REMOVE -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_photo, parent, false)
                    // 设置按钮样式
                    view.findViewById<MaterialCardView>(R.id.photoCard).apply {
                        strokeWidth = 0
                        cardElevation = 0f
                        radius = resources.getDimension(R.dimen.card_corner_radius)
                    }
                    view.findViewById<ImageView>(R.id.photoImageView).apply {
                        scaleType = ImageView.ScaleType.CENTER
                        setBackgroundResource(R.drawable.button_background)
                        val padding = resources.getDimensionPixelSize(R.dimen.button_padding)
                        setPadding(padding, padding, padding, padding)
                    }
                    ActionButtonViewHolder(view)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                VIEW_TYPE_PHOTO -> {
                    holder as PhotoViewHolder
                    val photoPath = photos[position]
                    try {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(photoPath)
                        holder.imageView.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        holder.imageView.setImageResource(R.drawable.ic_error_image)
                    }
                    
                    holder.checkmarkImageView.visibility = if (isSelectionMode && selectedPhotos.contains(position)) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    holder.cardView.isChecked = isSelectionMode && selectedPhotos.contains(position)
                }
                VIEW_TYPE_ADD -> {
                    holder as ActionButtonViewHolder
                    holder.imageView.setImageResource(R.drawable.ic_add_photo)
                }
                VIEW_TYPE_REMOVE -> {
                    holder as ActionButtonViewHolder
                    holder.imageView.setImageResource(R.drawable.ic_delete)
                }
            }
        }

        override fun getItemCount() = photos.size + 2 // 加2是为了显示添加按钮和删除按钮
    }
}
