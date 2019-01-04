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

    private var mOffsetX = 0
    private var mCenterY = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
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
        val path = Path().apply { reset() }

        // 确定几个小段
        val mSegmentsCount = segment
        // 确定曲线的宽高
        val mSegmentsWidth = width / mSegmentsCount
        val mSegmentsHeight = height / 8 * level
        // 确定初始方向, 还需要根据 mOffsetX 动态改变方向
        val dir = direction * if (mOffsetX / mSegmentsWidth % 2 == 0) 1 else -1

        // 改变偏移量
        mOffsetX %= mSegmentsWidth
        // 最前面单独的线, 没有规律, 单独绘制
        prefix(canvas, mSegmentsWidth, mSegmentsHeight, dir, path)
        // 有规律的线, 需要不停的变换方向
        repeat(canvas, mSegmentsCount, mSegmentsWidth, mSegmentsHeight, dir, path)
    }

    /**
     * 第一段, 需要动态绘制, 是没有规律的, 所以单独分开
     * 参数分别为, 画布, 小段宽度, 小段高度, 方向, 画笔路径
     */
    private fun prefix(canvas: Canvas, width: Int, height: Int, direction: Int, path: Path) {
        // 设置起点
        val p1 = Point(0, mCenterY)
        // 设置高度, 辅助点位置
        val pHeight = ((mOffsetX.toDouble() / width) * height).toInt()
        val p2 = Point(mOffsetX / 2, mCenterY + pHeight * direction)
        // 设置终点位置
        val p3 = Point(mOffsetX, mCenterY)
        // 设置 path
        path.moveTo(p1.x.toFloat(), p1.y.toFloat())
        path.quadTo(p2.x.toFloat(), p2.y.toFloat(), p3.x.toFloat(), p3.y.toFloat())
        // 画线
        canvas.drawPath(path, mPaint)
    }

    private fun repeat(canvas: Canvas, count: Int, width: Int, height: Int, direction: Int, path: Path) {
        for (index in 0 until count) {
            // 设置起点
            val p1 = Point(mOffsetX + width * index, mCenterY)
            // 设置高度, 辅助点位置
            val dir = if (index % 2 == 0) -direction else direction
            val p2 = Point(p1.x + width / 2, mCenterY + height * dir)
            // 设置终点位置
            val p3 = Point(p1.x + width, mCenterY)
            // 设置 path
            path.moveTo(p1.x.toFloat(), p1.y.toFloat())
            path.quadTo(p2.x.toFloat(), p2.y.toFloat(), p3.x.toFloat(), p3.y.toFloat())
            // 画线
            canvas.drawPath(path, mPaint)
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
        animator = ValueAnimator.ofInt(0, width * 2).apply {
            duration = 3 * 1000
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