package com.xz.pcm_record

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

/**
 * PCM 音频播放
 */
class PCMAudioPlayer private constructor() {

    private val TAG = this::class.simpleName

    companion object {
        /**
         * 单例模式，防止出现多个音频播放问题
         */
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PCMAudioPlayer()
        }
    }

    //采样率
    private val sampleRateInHz = 16000

    //单声道
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO

    //量化位数
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    /**
     * MODE_STREAM：在这种模式下，需要先play，然后通过write一次次把音频数据写到AudioTrack中。每次都需要把数据从用户提供的Buffer中拷贝到AudioTrack内部的Buffer中，这在一定程度上会使引入延时。为解决这一问题，AudioTrack就引入了第二种模式。
     * MODE_STATIC：这种模式下，需要先write，再play.。先把所有数据通过一次write调用传递到AudioTrack中的内部缓冲区，后续就不必再传递数据了。但它也有一个缺点，就是一次write的数据不能太多，否则系统无法分配足够的内存来存储全部数据。
     */
    private val playModel = AudioTrack.MODE_STREAM

    private val bufferSize: Int
    private val audioTrack: AudioTrack

    //音频播放地址
//    private var pcmFilePath = ""
    private var totalDuration = 0
    private var currentDuration = 0
    private var timeTimer: Timer? = null
    private val playHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    //播放更新进度
    private var onUpdatePlayDuration: ((currentDuration: Int, totalDuration: Int) -> Unit)? = null

    //播放完成
    private var onPlayComplete: (() -> Unit)? = null

    //播放停止，点击了暂停
    private var onPlayerStop: (() -> Unit)? = null

    //播放开始
    private var onPlayerStart: (() -> Unit)? = null

    //播放失败
    private var onPlayError: ((String) -> Unit)? = null

    /**
     * 初始话一个音频播放器
     */
    init {
        bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder().apply {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            }.build()

            val audioFormat = AudioFormat.Builder().apply {
                setSampleRate(sampleRateInHz)
                setEncoding(audioFormat)
                setChannelMask(channelConfig)
            }.build()

            audioTrack = AudioTrack(
                audioAttributes,
                audioFormat,
                bufferSize,
                playModel,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
        } else {
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                bufferSize,
                playModel
            )
        }
    }


    //开始播放，需要传入播放地址
    fun startPlay(filePath: String?): PCMAudioPlayer {

        //是否初始话完成
        if (audioTrack.state == AudioTrack.STATE_UNINITIALIZED) {
            onPlayError("player init error")
            return this
        }

        if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            stopAudioPlay()
        }

        val path = filePath ?: ""
        if (!verificationFile(path)) {
            onPlayError("file path is null or file not found")
            return this
        }

        Timer().schedule(timerTask {
            //开始播放
            startAudioPlay(path)
            onPlayerStart?.invoke()
        }, 200)
        return this
    }

    private fun startAudioPlay(pcmFilePath: String) {
        try {
            audioTrack.play()
            val pcmFile = File(pcmFilePath)
            val fileInputStream = FileInputStream(pcmFile)
            val buffer = ByteArray(bufferSize)
            thread {
                while (fileInputStream.available() > 0 && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    val readCount = fileInputStream.read(buffer)
                    //判断读的数据是否出问题了
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue
                    }
                    if (readCount != -1 && readCount != 0) {
                        audioTrack.write(buffer, 0, readCount)
                    }
                }

                onPlayComplete()
            }
            updatePlayProgress(pcmFilePath)
        } catch (e: Exception) {
            e.printStackTrace()
            onPlayError("player play error")
        }
    }

    /**
     * 更新播放进度
     */
    private fun updatePlayProgress(pcmFilePath: String) {
        currentDuration = 0
        totalDuration = getTotalDuration(pcmFilePath)
        onUpdatePlayDuration?.invoke(currentDuration, totalDuration)
        timeTimer = timer("pcm_player", false, 200, 200) {
            currentDuration += 200
            playHandler.post {
                onUpdatePlayDuration?.invoke(
                    getDurationSecond(currentDuration / 1000.0),
                    totalDuration
                )
            }
        }
    }

    /**
     * 验证文件是否存在
     */
    private fun verificationFile(filePath: String?): Boolean = if (filePath.isNullOrEmpty()) {
        false
    } else {
        val file = File(filePath)
        file.exists()
    }

    /**
     * 播放出问题了
     */
    private fun onPlayError(string: String) {
        onPlayError?.invoke(string)
    }

    /**
     * 停止播放
     */
    private fun stopPlay() {
        timeTimer?.cancel()
        playHandler.post {
            onPlayerStop?.invoke()
        }
        currentDuration = 0
    }

    /**
     * 停止播放
     */
    fun stopAudioPlay() {
        //是否正在播放
        if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop()
        }
    }

    /**
     * 播放完成
     * 1.正常播放完成
     * 2.外部调用了stop play
     */
    private fun onPlayComplete() {
        //播放完成
        //正常播放完成
        if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            stopPlay()
            stopAudioPlay()
            playHandler.post {
                onPlayComplete?.invoke()
            }
        } else if (audioTrack.playState == AudioTrack.PLAYSTATE_STOPPED) {
            //外部调用了暂停
            stopPlay()
        }
    }

    /**
     * 获取播放时长
     */
    fun getTotalDuration(filePath: String): Int {
        val file = File(filePath)
        var recordTime = 0.0

        if (file.exists()) {
            val tmp: Long = file.length()
            recordTime =
                tmp / (audioTrack.channelCount * audioTrack.sampleRate * audioFormat).toDouble()
        }

        return getDurationSecond(recordTime)
    }

    private fun getDurationSecond(duration: Double): Int =
        BigDecimal(duration).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()

    fun setOnUpdateDuration(listener: ((Int, Int) -> Unit)): PCMAudioPlayer {
        onUpdatePlayDuration = listener
        return this
    }

    fun setOnPlayerComplete(listener: (() -> Unit)): PCMAudioPlayer {
        onPlayComplete = listener
        return this
    }

    fun setOnPlayerStop(listener: (() -> Unit)): PCMAudioPlayer {
        onPlayerStop?.invoke()
        onPlayerStop = listener
        return this
    }

    fun setOnPlayerStart(listener: (() -> Unit)): PCMAudioPlayer {
        onPlayerStart = listener
        return this
    }

    fun setLifeCycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onActivityStop() {
                stopAudioPlay()
            }
        })
    }
}