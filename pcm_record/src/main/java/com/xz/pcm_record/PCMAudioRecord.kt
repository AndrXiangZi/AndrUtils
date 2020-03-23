package com.xz.pcm_record

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Looper
import androidx.core.os.HandlerCompat
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer

/**
 * PCM 录音
 *
 * 一般语音转换成文字会使用PCM格式的
 */
class PCMAudioRecord {

    //采样率
    private val sampleRateInHz = 16000

    //单声道
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO

    //量化位数
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    //缓存区大小
    private var bufferSize = 0

    private var recordPath = ""
    private var maxRecordTime = 15
    private var currentProgress = 0

    private var timer: Timer? = null


    private var mAudioRecord: AudioRecord? = null

    private val recordHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    //录音进度的监听，回调在主线程
    private var progressListener: ((progress: Int, maxRecord: Int) -> Unit)? = null

    //录音完成的监听，回调在主线程
    private var onCompleteListener: ((Int) -> Unit)? = null

    //录音发生错误
    private var onRecordError: ((Exception) -> Unit)? = null

    private var onUpdateVolumeLevel:((Int)->Unit)? = null

    /**
     * 初始化录音控件
     */
    private fun initAudioRecord() {
        bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        mAudioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSize
        )
    }

    /**
     * 开始录音
     * 设置录音地址，默认最大时间15s
     */
    fun start(recordPath: String) {
        start(recordPath, 15)
    }

    /**
     * 开始录音（设置录音地址 和 最大录音时间 ）
     */
    fun start(recordPath: String?, maxRecordTime: Int) {

        if (recordPath.isNullOrEmpty() || maxRecordTime <= 0) {
            onRecordError(Exception("record path or max time param error"))
            return
        }

        this.recordPath = recordPath
        this.maxRecordTime = maxRecordTime
        startPCMRecord()
    }

    /**
     * 开始PCM录音
     * 需要权限
     * Manifest.permission.READ_EXTERNAL_STORAGE,
     * Manifest.permission.WRITE_EXTERNAL_STORAGE,
     * Manifest.permission.RECORD_AUDIO
     */
    private fun startPCMRecord() {
        try {
            initAudioRecord()

            if (mAudioRecord == null) {
                throw RuntimeException("The AudioRecord is not uninitialized")
            }

            mAudioRecord?.apply {
                //判断AudioRecord的状态是否初始化完毕
                //在AudioRecord对象构造完毕之后，就处于AudioRecord.STATE_INITIALIZED状态了。
                if (state == AudioRecord.STATE_UNINITIALIZED) {
                    throw RuntimeException("The AudioRecord is not uninitialized")
                }
                startRecording()
                startReaderData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onRecordError(e)
        }
    }

    /**
     * 开始读取数据
     */
    private fun startReaderData() {
        val file = File(recordPath)
        if (file.exists()) {
            file.delete()
        }

        try {
            file.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
            onRecordError(e)
        }

        try {
            thread(true) {
                val bytes = ByteArray(bufferSize)
                val fileOutputStream = FileOutputStream(file)
                while (mAudioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val read = mAudioRecord?.read(bytes, 0, bytes.size) ?: 0
                    fileOutputStream.write(bytes, 0, read)
                    fileOutputStream.flush()
                    updateVolumeLevel(read,bytes)
                }
                //录音结束,关闭文件读写
                fileOutputStream.flush()
                fileOutputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onRecordError(e)
        }
        updateProgress()
    }

    /**
     * 开始计时录音时间
     */
    private fun updateProgress() {
        //初始化时间
        currentProgress = 0
        progressListener?.invoke(0, maxRecordTime * 1000)

        timer = timer("recorder", false, 200, 200) {
            currentProgress += 200
            recordHandler.post {
                progressListener?.invoke(currentProgress, maxRecordTime * 1000)
            }
            //到达最大时间
            if (currentProgress >= maxRecordTime * 1000) {
                stop()
            }
        }
    }

    /**
     * 停止录音
     */
    fun stop() {
        timer?.apply {
            cancel()
        }
        timer = null
        stopRecord()
        recordHandler.post {
            onCompleteListener?.invoke(currentProgress)
            currentProgress = 0
        }
    }

    /**
     * 停止录音
     */
    private fun stopRecord() {
        if (mAudioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord?.stop()
        }

        if (mAudioRecord?.state == AudioRecord.STATE_INITIALIZED) {
            mAudioRecord?.release()
        }
    }


    /**
     * 录音出现错误
     */
    private fun onRecordError(e: Exception) {
        onRecordError?.invoke(e)
    }

    /**
     * 录音音量,可以在界面更新录音动画
     * 0-30
     */
    private fun updateVolumeLevel(read: Int, readBuffer: ByteArray): Double {
        var v = 0.0
        readBuffer.forEach {
            v += it * it
        }

        return v / read / 100
    }

    /**
     * 更新进度（回调更新当前进度时长）
     */
    fun setRecordProgressListener(progressListener: ((progress: Int, maxRecord: Int) -> Unit)?) {
        this.progressListener = progressListener
    }

    /**
     * 完成 回调录音时长
     */
    fun setRecordComplete(onCompleteListener: ((Int) -> Unit)) {
        this.onCompleteListener = onCompleteListener
    }

    /**
     * 设置录音失败的监听
     */
    fun setRecordError(error: ((Exception) -> Unit)?) {
        this.onRecordError = error
    }

    /**
     * 录音音量的回调
     */
    fun setUpdateVolumeLevelListener(updateVolumeLevel: ((Int) -> Unit)){
        this.onUpdateVolumeLevel = updateVolumeLevel
    }

}