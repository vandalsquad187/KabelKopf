package com.docai.kabelkopf.service

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import com.docai.kabelkopf.util.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentFile: File? = null

    fun isRecording(): Boolean = recorder != null

    fun isPlaying(): Boolean = player?.isPlaying == true

    fun startRecording(): File {
        stopRecording()

        val file = FileUtils.createVoiceFile(context)
        currentFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setAudioSamplingRate(8000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        return file
    }

    fun stopRecording(): File? {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
        return currentFile
    }

    fun startPlayback(filePath: String, onCompletion: () -> Unit = {}) {
        stopPlayback()
        player = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener { onCompletion() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopPlayback() {
        try {
            player?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        player = null
    }

    fun getCurrentFilePath(): String? = currentFile?.absolutePath

    fun cleanup() {
        stopRecording()
        stopPlayback()
    }
}
