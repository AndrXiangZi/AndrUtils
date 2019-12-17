package com.xz.circleprogress

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat


/**
 * @Package: com.example.demoapplication
 * @ClassName: CircleProgressBar
 * @CreateDate: 19-12-17 上午11:23
 * @Description: 圆形进度
 */
class CircleProgressBar(context: Context, attributes: AttributeSet) : View(context, attributes) {


    private var mStrokeWidth = 10f //画笔的宽度（圆弧的宽度）
    private var mPaint: Paint = Paint()
    private var mRectF: RectF? = null
    private var maxProgress = 100
    private var progress = 0
    private var progressBgColor = android.R.color.darker_gray
    private var progressColor = android.R.color.holo_blue_light
    private var animatorDuration = 2000L

    init {
        mPaint.apply {
            //设置画笔的样式
            style = Paint.Style.STROKE
            //设置笔刷的样式
            strokeCap = Paint.Cap.ROUND
            //抗锯齿
            isAntiAlias = true
        }

        context.obtainStyledAttributes(attributes, R.styleable.CircleProgressBar).apply {
            mStrokeWidth = getInt(R.styleable.CircleProgressBar_progressWidth, 10).toFloat()
            maxProgress = getInt(R.styleable.CircleProgressBar_max, 100)
            progress = getInt(R.styleable.CircleProgressBar_progress, 0)
            progressBgColor = getResourceId(
                R.styleable.CircleProgressBar_progressBackgroundColor,
                android.R.color.darker_gray
            )
            progressColor = getResourceId(
                R.styleable.CircleProgressBar_progressColor,
                android.R.color.holo_blue_light
            )

            animatorDuration = getInt(R.styleable.CircleProgressBar_animatorDuration, 2000).toLong()

            recycle()
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        //绘制底部圆环，绘制中心点
        val centerX = (width / 2).toFloat()
        mPaint.apply {
            color = ContextCompat.getColor(context, progressBgColor)
            this.strokeWidth = mStrokeWidth
        }
        canvas?.drawCircle(centerX, centerX, centerX - mStrokeWidth, mPaint)

        if (mRectF == null) {
            mRectF = RectF(mStrokeWidth, mStrokeWidth, width - mStrokeWidth, width - mStrokeWidth)
        }
        mPaint.color = ContextCompat.getColor(context, progressColor)
        canvas?.drawArc(mRectF!!, -90f, (360.0 * progress / maxProgress).toFloat(), false, mPaint)
    }

    /**
     * 设置当前进度
     */
    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }

    /**
     * 设置进度带动画
     */
    fun setProgressWithAnimator(progress: Int) {
        this.progress = progress
        startAnimator()

    }

    /**
     * 进度动画
     */
    private fun startAnimator() {
        ObjectAnimator.ofInt(this, "progress").apply {
            duration = animatorDuration
            setIntValues(0, progress)
            start()
        }
    }

    /**
     * 设置最大进度
     */
    fun setMax(max: Int) {
        this.maxProgress = max
        invalidate()
    }
}