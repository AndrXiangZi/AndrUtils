package com.example.demoapplication

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.demoapplication.databinding.ActivityDatabingBinding
import com.xz.pcm_record.PCMAudioRecord
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding = ActivityDatabingBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        ActivityCompat.requestPermissions(this,arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        ),1001)


        dataBinding.progressBar.max = 15
        val pcmAudioRecord = PCMAudioRecord()

        pcmAudioRecord.setRecordProgressListener { progress, maxRecord ->
            dataBinding.progressBar.progress = BigDecimal(progress/1000).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()
            dataBinding.textView3.text = dataBinding.progressBar.progress.toString()
        }

        pcmAudioRecord.setRecordComplete {
            Toast.makeText(this, "录音完成$it", Toast.LENGTH_LONG).show()
        }

        dataBinding.startButton.setOnClickListener {
            pcmAudioRecord.start(
                cacheDir.path+"/".plus(
                    System.currentTimeMillis().toString().plus(".pcm")
                )
            )
        }

        dataBinding.stopButton.setOnClickListener {
            pcmAudioRecord.stop()
        }

        dataBinding.recordList.setOnClickListener {
            ActivityCompat.startActivity(this,Intent(this,RecordListActivity::class.java),null)
        }
    }

    fun test(view: View) {
        mainCircleProgress.setProgressWithAnimator(100)
    }


}
