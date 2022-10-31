package com.example.gcore_vod_demo.screens.videos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gcore_vod_demo.data.VideoItemModel
import com.google.android.material.imageview.ShapeableImageView
import gcore_vod_demo.R

class VideoItemsAdapter : RecyclerView.Adapter<VideoItemsAdapter.VideoItemViewHolder>() {

    private val videoItems: MutableList<VideoItemModel> = ArrayList()
    private lateinit var itemsAdapterListener: VideoItemsAdapterListener

    fun setData(newVideos: List<VideoItemModel>) {
        val diffCallback = VideosDiffCallback(videoItems, newVideos)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        videoItems.clear()
        videoItems.addAll(newVideos)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setListener(listener: VideoItemsAdapterListener) {
        itemsAdapterListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        return VideoItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.li_video,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        holder.bind(videoItems[position])
        holder.setListener(itemsAdapterListener, position)
    }

    override fun getItemCount(): Int {
        return videoItems.size
    }

    class VideoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val videoPreview: ShapeableImageView = itemView.findViewById(R.id.videoPreview)
        private val videoName: TextView = itemView.findViewById(R.id.videoNameTextView)
        private val videoSize: TextView = itemView.findViewById(R.id.videoSizeTextView)
        private val videoDuration: TextView = itemView.findViewById(R.id.duration_video_text_view)
        private val videoId: TextView = itemView.findViewById(R.id.id_video_text_view)

        fun bind(videoItem: VideoItemModel) {

            Glide.with(itemView)
                .load(videoItem.videoPreviewUri)
                .into(videoPreview)

            videoName.text = videoItem.videoTitle
            videoSize.text =
                itemView.context.getString(R.string.size_in_MB, videoItem.videoSizeInMB)
            videoDuration.text = videoItem.videoDuration
            videoId.text = itemView.context.getString(R.string.id, videoItem.videoId)
        }

        fun setListener(listener: VideoItemsAdapterListener, position: Int) {
            itemView.setOnClickListener {
                listener.onItemClick(position)
            }
        }
    }
}