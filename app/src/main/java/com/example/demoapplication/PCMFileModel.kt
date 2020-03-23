package com.example.demoapplication

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt

class PCMFileModel {

    var name = "哈哈哈"
    var duration = 0
    var playProgress = ObservableInt()
    var isPlaying = ObservableBoolean()
    var path = ""

    companion object{
        @BindingAdapter("srcCompat")
        @JvmStatic
        fun setImageDrawable(imageView: ImageView, state: Boolean) {
            if (state) {
                imageView.setImageResource(R.drawable.ic_pause)
            } else {
                imageView.setImageResource(R.drawable.ic_play)
            }
        }
    }

}