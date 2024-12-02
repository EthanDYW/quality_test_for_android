package com.example.qualitytestforandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ImageViewerAdapter(private val onImageClick: (Int) -> Unit) : RecyclerView.Adapter<ImageViewerAdapter.ImageViewHolder>() {
    private var images = mutableListOf<File>()
    private val selectedImages = mutableSetOf<Int>()

    fun updateImages(newImages: List<File>) {
        images.clear()
        images.addAll(newImages)
        selectedImages.clear()
        notifyDataSetChanged()
    }

    fun getSelectedImages(): Set<Int> = selectedImages.toSet()

    fun toggleSelection(position: Int) {
        val actualPosition = position % images.size
        if (actualPosition in selectedImages) {
            selectedImages.remove(actualPosition)
        } else {
            selectedImages.add(actualPosition)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_viewer, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val actualPosition = position % images.size
        holder.bind(images[actualPosition], actualPosition in selectedImages)
    }

    override fun getItemCount(): Int = if (images.isEmpty()) 0 else Int.MAX_VALUE

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val checkMark: ImageView = itemView.findViewById(R.id.checkMark)

        init {
            itemView.setOnClickListener {
                val actualPosition = adapterPosition % images.size
                if (actualPosition != RecyclerView.NO_POSITION) {
                    onImageClick(actualPosition)
                }
            }
        }

        fun bind(image: File, isSelected: Boolean) {
            imageView.setImageBitmap(android.graphics.BitmapFactory.decodeFile(image.absolutePath))
            checkMark.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }
}
