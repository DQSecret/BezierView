package com.dqdana.code.bezierview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class Bezier(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var mPaint: Paint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
        isDither = true
        pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
    }
    private var mPath: Path = Path()

    private val mSegmentsCount = 4
    private var mSegmentsWidth = 0
    private var mSegmentsHeight = 0
    private var mOffsetX = 0
    private var mCenterY = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mSegmentsWidth = width / mSegmentsCount
        mSegmentsHeight = height / 4 / 2 * 3
        mOffsetX = mSegmentsWidth / 2
        mCenterY = height / 2
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 一共是5条线, 默认方向不同,高度不同,长度不同
        val direction = -1 // up/down : -1..1
        val level = 3 // 3..1 高度由高到低
        val segment = 4 // 1, 2, 4 段数, 段数越多, 长度越少
        drawPath(canvas, direction, level, segment)
    }

    private fun drawPath(canvas: Canvas, direction: Int, level: Int, segment: Int) {
        // 重置
        mPath.reset()
        // 先确定初始方向
        val dir = if (mOffsetX / mSegmentsWidth % 2 == 0) 1 else -1
        // 改变弧线的高度
        mSegmentsHeight = height / 4 / 2 * 3
        // 改变偏移量
        mOffsetX %= mSegmentsWidth
        // 最前面单独的线
        prefix(canvas, dir)
        // 有规律的线
        repeat(canvas, dir)
    }

    private fun prefix(canvas: Canvas, direction: Int) {
        // 设置起点
        val p1 = Point(0, mCenterY)
        // 设置高度, 辅助点位置
        val height = ((mOffsetX.toDouble() / mSegmentsWidth) * mSegmentsHeight).toInt()
        val p2 = Point(mOffsetX / 2, mCenterY + height * direction)
        // 设置终点位置
        val p3 = Point(mOffsetX, mCenterY)
        // 设置 path
        mPath.moveTo(p1.x.toFloat(), p1.y.toFloat())
        mPath.quadTo(p2.x.toFloat(), p2.y.toFloat(), p3.x.toFloat(), p3.y.toFloat())
        // 画线
        canvas.drawPath(mPath, mPaint)
    }

    private fun repeat(canvas: Canvas, direction: Int) {
        for (index in 0 until mSegmentsCount) {
            // 设置起点
            val p1 = Point(mOffsetX + mSegmentsWidth * index, mCenterY)
            // 设置高度, 辅助点位置
            val dir = if (index % 2 == 0) -direction else direction
            val p2 = Point(p1.x + mSegmentsWidth / 2, mCenterY + mSegmentsHeight * dir)
            // 设置终点位置
            val p3 = Point(p1.x + mSegmentsWidth, mCenterY)
            // 设置 path
            mPath.moveTo(p1.x.toFloat(), p1.y.toFloat())
            mPath.quadTo(p2.x.toFloat(), p2.y.toFloat(), p3.x.toFloat(), p3.y.toFloat())
            // 画线
            canvas.drawPath(mPath, mPaint)
        }
    }

    private var animator: ValueAnimator? = null

    fun toggle() {
        if (animator == null) {
            start()
        } else {
            if (animator?.isRunning == true) {
                stop()
            } else {
                start()
            }
        }
    }

    private fun start() {
        animator = ValueAnimator.ofInt(0, mSegmentsWidth * 12).apply {
            duration = 6 * 3 * 1000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                mOffsetX = it.animatedValue as Int
                postInvalidate()
            }
            start()
        }
    }

    private fun stop() {
        animator?.end()
    }
}