package com.example.qualitytestforandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ThumbnailAdapter(private val onThumbnailClick: (Int) -> Unit) : RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder>() {
    private var images = mutableListOf<File>()
    private val selectedImages = mutableSetOf<Int>()

    fun updateImages(newImages: List<File>) {
        images.clear()
        images.addAll(newImages)
        selectedImages.clear()
        notifyDataSetChanged()
    }

    fun updateSelection(selectedIndices: Set<Int>) {
        selectedImages.clear()
        selectedImages.addAll(selectedIndices)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        holder.bind(images[position], position in selectedImages)
    }

    override fun getItemCount(): Int = images.size

    inner class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnailImage)
        private val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onThumbnailClick(position)
                }
            }
        }

        fun bind(image: File, isSelected: Boolean) {
            thumbnailImage.setImageBitmap(android.graphics.BitmapFactory.decodeFile(image.absolutePath))
            selectionOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }
}
