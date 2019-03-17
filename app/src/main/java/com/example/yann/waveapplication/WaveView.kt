package com.example.yann.waveapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.Keep
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator


/**
 * 水波纹扩散view
 */
class WaveView : View {

    private var centerColor: Int = ContextCompat.getColor(context, R.color.color_897EFF)
    private var centerRadius = Utils.dip2px(context,4f)
    private var maxRadius = Utils.dip2px(context,14f)
    private var waveIntervalTime = 500
    private var waveDuration = 1500
    private var running: Boolean = false
    private var waveList = mutableListOf<Wave>()
    private var waveWidth = Utils.dip2px(context,1.0f)
    private val paint = Paint()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.WaveView, defStyleAttr, 0)
        centerColor = typedArray.getColor(
            R.styleable.WaveView_center_color,
            ContextCompat.getColor(context, R.color.color_897EFF)
        )
        centerRadius = typedArray.getDimension(R.styleable.WaveView_center_radius, 4f).toInt()
        maxRadius = typedArray.getDimension(R.styleable.WaveView_max_radius, 14f).toInt()
        waveWidth = typedArray.getDimension(R.styleable.WaveView_wave_width, 1.0f).toInt()
        waveIntervalTime = typedArray.getInt(R.styleable.WaveView_wave_interval_time, 500)
        waveDuration = typedArray.getInt(R.styleable.WaveView_wave_duration, 1500)
        paint.color = centerColor
        typedArray.recycle()

    }

    fun setWaveStart(waveStart: Boolean) {
        if (waveStart) {
            if (!running) {
                running = true
                waveList.add(Wave())
            }
        } else {
            running = false
            waveList.forEach {
                it.cancelAnimation()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val radius = (Math.min(w, h) / 2.0f).toInt()
        if (radius < maxRadius) {
            maxRadius = radius
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        waveList.forEach {
            paint.alpha = it.getAlpha()
            paint.strokeWidth = waveWidth.toFloat()
            paint.style = Paint.Style.STROKE
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), it.getCurrentRadius(), paint)
        }
        if (waveList.size > 0) {
            paint.alpha = 255
            paint.style = Paint.Style.FILL
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), centerRadius.toFloat(), paint)
        }
    }

    private inner class Wave {

        private var hasCreateNewWave = false

        private val createWaveAnimation = ObjectAnimator.ofFloat(this, "percent", 0f, 1.0f).apply {
            this.interpolator = LinearInterpolator()
            this.duration = waveDuration.toLong()
            this.start()
            this.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (running) {
                        waveList.remove(this@Wave)
                    }
                }
            })
        }

        var percent: Float = 0f
            @Keep
            set(value) {
                field = value
                if (running && value >= waveIntervalTime.toFloat() / waveDuration.toFloat() && !hasCreateNewWave) {
                    waveList.add(Wave())
                    hasCreateNewWave = true
                }
                invalidate()
            }

        fun cancelAnimation() {
            createWaveAnimation.cancel()
        }

        fun getAlpha(): Int {
            return (255 * (1 - percent)).toInt()
        }

        fun getCurrentRadius(): Float {
            return centerRadius + percent * (maxRadius - centerRadius)
        }
    }
}