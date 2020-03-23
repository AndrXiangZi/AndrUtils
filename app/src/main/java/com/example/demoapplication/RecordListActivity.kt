package com.example.demoapplication

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.demoapplication.databinding.ActivityRecordListBinding
import com.xz.pcm_record.PCMAudioPlayer

class RecordListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityRecordListBinding.inflate(layoutInflater)
        setContentView(inflate.root)
        setSupportActionBar(inflate.toolbar)

        inflate.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        this.lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                PCMAudioPlayer.instance.stopAudioPlay()
            }
        })

        val pcmAdapter = PCMListAdapter()
        inflate.contentRecordList.recycleView.adapter = pcmAdapter
        pcmAdapter.addAll(arrayListOf<PCMFileModel>().apply {
            cacheDir.listFiles()?.forEach {
                add(PCMFileModel().apply {
                    name = it.name
                    path = it.path
                    duration = PCMAudioPlayer.instance.getTotalDuration(it.path)
                    isPlaying.set(false)
                })
            }
        })
        PCMAudioPlayer.instance.setLifeCycle(this.lifecycle)


        pcmAdapter.onItemPlayClickListener = { dataBinding, pcmData ->
            val itemData = dataBinding.itemData
            val isPlay = itemData?.isPlaying?.get() ?: false
            if (isPlay) {
                itemData?.isPlaying?.set(false)
                PCMAudioPlayer.instance.stopAudioPlay()
            } else {
                PCMAudioPlayer.instance.startPlay(itemData?.path).setOnPlayerStart {
                    itemData?.isPlaying?.set(true)
                }.setOnUpdateDuration { i, i2 ->
                    itemData?.playProgress?.set(i)
                }.setOnPlayerComplete {
                    itemData?.playProgress?.set(0)
                    itemData?.isPlaying?.set(false)
                }.setOnPlayerStop {
                    itemData?.isPlaying?.set(false)
                }
            }
        }
    }
}