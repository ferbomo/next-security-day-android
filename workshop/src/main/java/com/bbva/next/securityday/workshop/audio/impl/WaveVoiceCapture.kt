package com.bbva.next.securityday.workshop.audio.impl

import android.content.Context
import android.media.AudioFormat
import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.core.util.Pair
import com.bbva.next.securityday.workshop.audio.VoiceCapture
import com.github.squti.androidwaverecorder.WaveRecorder
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class WaveVoiceCapture : VoiceCapture {

    companion object {
        val TEMP_FILE = "temp.wav"
    }

    private var recorder: WaveRecorder? = null
    private var callback: VoiceCapture.Callback? = null
    private var context: WeakReference<Context>? = null

    override fun start(context: Context, callback: VoiceCapture.Callback) {

        kotlin.runCatching { recorder?.stopRecording() }

        this.context = WeakReference(context)
        this.callback = callback
        context.deleteFile(TEMP_FILE)
        val outputPath = "${context.filesDir.path}/$TEMP_FILE"
        WaveRecorder(outputPath)
            .also {
                it.waveConfig.channels = AudioFormat.CHANNEL_IN_MONO
                it.waveConfig.sampleRate = 8000
                it.waveConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
                it.startRecording()
                this.recorder = it
            }
    }

    override fun stop() {

        recorder?.stopRecording()

        val context = context?.get() ?: return

        val executor = Executors.newSingleThreadExecutor()
        val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

        executor.execute {
            val bytes = context.openFileInput(TEMP_FILE).readBytes()
            mainHandler.post {
                callback?.onBytesRecorded(bytes)
            }
        }
    }

    override fun isWavValid(filePath: String): Pair<Boolean, String> {

        val isOk = File(filePath).exists()
        val message = if (isOk) {
            "Audio is ok"
        } else {
            "Audio file is missing"
        }
        return Pair(isOk, message)
    }
}