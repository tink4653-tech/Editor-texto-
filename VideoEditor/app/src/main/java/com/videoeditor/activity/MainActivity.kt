package com.videoeditor.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.videoeditor.R
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private val videoList = mutableListOf<VideoItem>()

    companion object {
        private const val REQUEST_PICK_VIDEO = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        loadVideos()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewVideos)
        val selectVideoBtn: View = findViewById(R.id.btnSelectVideo)
        
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        videoAdapter = VideoAdapter(videoList) { video ->
            openVideoEditor(video.uri)
        }
        recyclerView.adapter = videoAdapter

        selectVideoBtn.setOnClickListener {
            pickVideoFromGallery()
        }
    }

    private fun loadVideos() {
        // Load videos from device
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA
            ),
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val duration = it.getLong(durationColumn)
                val path = it.getString(dataColumn)

                val uri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                videoList.add(VideoItem(uri, name, duration, path))
            }
        }
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_PICK_VIDEO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_PICK_VIDEO -> {
                    data.data?.let { uri ->
                        openVideoEditor(uri)
                    }
                }
            }
        }
    }

    private fun openVideoEditor(videoUri: Uri) {
        val intent = Intent(this, VideoEditActivity::class.java)
        intent.putExtra("video_uri", videoUri.toString())
        startActivity(intent)
    }

    data class VideoItem(
        val uri: Uri,
        val name: String,
        val duration: Long,
        val path: String?
    )

    class VideoAdapter(
        private val videos: List<VideoItem>,
        private val onItemClick: (VideoItem) -> Unit
    ) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

        inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val thumbnailImage: ImageView = itemView.findViewById(R.id.imageThumbnail)
            val videoName: TextView = itemView.findViewById(R.id.textVideoName)
            val videoDuration: TextView = itemView.findViewById(R.id.textDuration)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_video, parent, false)
            return VideoViewHolder(view)
        }

        override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
            val video = videos[position]
            
            Glide.with(holder.itemView.context)
                .load(video.uri)
                .centerCrop()
                .into(holder.thumbnailImage)

            holder.videoName.text = video.name
            holder.videoDuration.text = formatDuration(video.duration)

            holder.itemView.setOnClickListener {
                onItemClick(video)
            }
        }

        override fun getItemCount() = videos.size

        private fun formatDuration(durationMs: Long): String {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = durationMs / (1000 * 60 * 60)
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
    }
}
