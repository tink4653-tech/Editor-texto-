package com.videoeditor.activity

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Statistics
import com.videoeditor.R
import java.io.File

class VideoEditActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var seekBar: SeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalTime: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var btnTrim: Button
    private lateinit var btnAddMusic: Button
    private lateinit var btnChangeSpeed: Button
    private lateinit var btnExport: Button
    
    private var videoUri: Uri? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val REQUEST_PICK_AUDIO = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)

        videoUri = Uri.parse(intent.getStringExtra("video_uri"))

        setupViews()
        loadVideo()
    }

    private fun setupViews() {
        videoView = findViewById(R.id.videoView)
        seekBar = findViewById(R.id.seekBar)
        txtCurrentTime = findViewById(R.id.txtCurrentTime)
        txtTotalTime = findViewById(R.id.txtTotalTime)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnTrim = findViewById(R.id.btnTrim)
        btnAddMusic = findViewById(R.id.btnAddMusic)
        btnChangeSpeed = findViewById(R.id.btnChangeSpeed)
        btnExport = findViewById(R.id.btnExport)

        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        btnTrim.setOnClickListener {
            trimVideo()
        }

        btnAddMusic.setOnClickListener {
            addBackgroundMusic()
        }

        btnChangeSpeed.setOnClickListener {
            changeVideoSpeed()
        }

        btnExport.setOnClickListener {
            exportVideo()
        }

        videoView.setOnPreparedListener { mediaPlayer ->
            val duration = mediaPlayer.duration
            txtTotalTime.text = formatTime(duration)
            seekBar.max = duration

            handler.post(object : Runnable {
                override fun run() {
                    if (isPlaying) {
                        val currentPosition = mediaPlayer.currentPosition
                        seekBar.progress = currentPosition
                        txtCurrentTime.text = formatTime(currentPosition)
                        handler.postDelayed(this, 1000)
                    }
                }
            })
        }

        videoView.setOnCompletionListener {
            isPlaying = false
            btnPlayPause.text = "Reproducir"
            seekBar.progress = 0
            txtCurrentTime.text = formatTime(0)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
                    txtCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadVideo() {
        videoUri?.let { uri ->
            videoView.setVideoURI(uri)
        }
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            videoView.pause()
            btnPlayPause.text = "Reproducir"
        } else {
            videoView.start()
            btnPlayPause.text = "Pausar"
        }
        isPlaying = !isPlaying
    }

    private fun trimVideo() {
        Toast.makeText(this, "Función de recorte - Selecciona inicio y fin", Toast.LENGTH_SHORT).show()
        // Implementación básica de trim
        val startTime = 0 // segundos
        val duration = 10 // segundos
        
        val outputPath = File(cacheDir, "trimmed_video.mp4").absolutePath
        
        val command = "-i ${getVideoPath()} -ss $startTime -t $duration -c copy $outputPath"
        
        FFmpegKit.execute(command) { session ->
            runOnUiThread {
                if (ReturnCode.isSuccess(session)) {
                    Toast.makeText(this, "Video recortado exitosamente", Toast.LENGTH_SHORT).show()
                    // Cargar video recortado
                    videoView.setVideoURI(Uri.fromFile(File(outputPath)))
                } else {
                    Toast.makeText(this, "Error al recortar video", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addBackgroundMusic() {
        Toast.makeText(this, "Selecciona un archivo de audio", Toast.LENGTH_SHORT).show()
        val intent = android.content.Intent(android.content.Intent.ACTION_PICK)
        intent.type = "audio/*"
        startActivityForResult(intent, REQUEST_PICK_AUDIO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_PICK_AUDIO) {
            val audioUri = data.data
            audioUri?.let { uri ->
                mixAudioWithVideo(uri)
            }
        }
    }

    private fun mixAudioWithVideo(audioUri: Uri) {
        val outputPath = File(cacheDir, "video_with_music.mp4").absolutePath
        
        val command = "-i ${getVideoPath()} -i $audioUri -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 -shortest $outputPath"
        
        FFmpegKit.execute(command) { session ->
            runOnUiThread {
                if (ReturnCode.isSuccess(session)) {
                    Toast.makeText(this, "Música agregada exitosamente", Toast.LENGTH_SHORT).show()
                    videoView.setVideoURI(Uri.fromFile(File(outputPath)))
                } else {
                    Toast.makeText(this, "Error al agregar música", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun changeVideoSpeed() {
        Toast.makeText(this, "Cambiando velocidad del video", Toast.LENGTH_SHORT).show()
        
        val outputPath = File(cacheDir, "speed_video.mp4").absolutePath
        val speed = 2.0 // 2x speed
        
        val command = "-i ${getVideoPath()} -filter:v \"setpts=PTS/$speed\" -filter:a \"atempo=$speed\" $outputPath"
        
        FFmpegKit.execute(command) { session ->
            runOnUiThread {
                if (ReturnCode.isSuccess(session)) {
                    Toast.makeText(this, "Velocidad cambiada exitosamente", Toast.LENGTH_SHORT).show()
                    videoView.setVideoURI(Uri.fromFile(File(outputPath)))
                } else {
                    Toast.makeText(this, "Error al cambiar velocidad", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun exportVideo() {
        Toast.makeText(this, "Exportando video...", Toast.LENGTH_SHORT).show()
        
        val outputPath = File(getExternalFilesDir(null), "exported_video_${System.currentTimeMillis()}.mp4").absolutePath
        
        val command = "-i ${getVideoPath()} -c:v libx264 -preset medium -crf 23 -c:a aac -b:a 128k $outputPath"
        
        FFmpegKit.execute(command) { session ->
            runOnUiThread {
                if (ReturnCode.isSuccess(session)) {
                    Toast.makeText(this, "Video exportado: $outputPath", Toast.LENGTH_LONG).show()
                    
                    // Guardar en galería
                    saveToGallery(File(outputPath))
                } else {
                    Toast.makeText(this, "Error al exportar video", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveToGallery(file: File) {
        val values = android.content.ContentValues()
        values.put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, file.name)
        values.put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/VideoEditor")
        
        val uri = contentResolver.insert(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }

    private fun getVideoPath(): String {
        return videoUri?.path ?: ""
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            videoView.pause()
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
