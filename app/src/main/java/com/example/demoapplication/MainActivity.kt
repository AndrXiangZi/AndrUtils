package com.example.demoapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import id.zelory.compressor.Compressor
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    fun btnCompress(view: View) {
        mainCircleProgress.setProgressWithAnimator(100)
//        val open = assets.open("big_picture.jpg")
//        val openFileOutput = openFileOutput("big_picture.jpg", MODE_PRIVATE)
//
//        var i = 0
//        open.use {input->
//            openFileOutput.use {
//                while (input.read().also { i = it } != -1){
//                    it.write(i)
//                }
//            }
//        }


//        Luban.with(this)
//            .load(getFileStreamPath("big_picture.jpg"))
//            .ignoreBy((5*1024))
//            .filter {
//                val file = File(it)
//                file.length() >= 5*1024*1014
//            }
//            .setCompressListener(object : OnCompressListener {
//                override fun onSuccess(file: File?) {
//                    Log.e("Luban", "file path----------->${file?.path}")
//                    Log.e("Luban", "file size----------->${file?.length()}")
//                }
//
//                override fun onError(e: Throwable?) {
//                    e?.printStackTrace()
//                }
//
//                override fun onStart() {
//                }
//
//            }).launch()

//        Compressor(this).setMaxWidth(1080).setMaxHeight(1920)
//            .compressToFileAsFlowable(getFileStreamPath("big_picture.jpg"))
//            .subscribeOn(Schedulers.io())
//            .subscribe { file ->
//                Log.e("Luban", "file path----------->${file?.path}")
//                Log.e("Luban", "file size----------->${file?.length()}")
//            }

//        val file = PictureUtils.compressPic(this, 720, 1080, getFileStreamPath("big_picture.jpg").path)
//        Log.e("Luban", "file path----------->${file?.path}")
//        Log.e("Luban", "file size----------->${file?.length()}")
    }
}
