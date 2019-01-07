package com.dqdana.code.bezierview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.animation.LinearInterpolator

class BezierSurfaceView(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs),
        SurfaceHolder.Callback,
        Runnable {

    // 缓冲持有者
    private var mHolder: SurfaceHolder? = null
    // 用于绘图的canvas
    private var mCanvas: Canvas? = null
    // 子线程标志位
    private var mIsDrawing: Boolean = false

    private var mPaint: Paint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        isAntiAlias = true
        isDither = true
    }

    private var mOffsetX = 0
    private var mCenterY = 0

    private val randomXArr by lazy {
        mutableListOf<Int>().apply {
            add((Math.random() * 50).toInt())
            add((Math.random() * 50).toInt())
            add((Math.random() * 50).toInt())
            add((Math.random() * 50).toInt())
            add((Math.random() * 50).toInt())
        }
    }

    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterY = height / 2
    }

    init {
        mHolder = holder
        mHolder?.addCallback(this)
        this.isFocusable = true
        this.isFocusableInTouchMode = true
        this.keepScreenOn = true
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mIsDrawing = true
        Thread(this).start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mIsDrawing = false
    }

    override fun run() {
        while (mIsDrawing) {
            draw()
        }
    }

    private fun draw() {
        try {
            mCanvas = mHolder?.lockCanvas()
            //draw something
            mCanvas?.let {
                //                start(it)
                drawPre(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (null != mCanvas) {
                mHolder?.unlockCanvasAndPost(mCanvas)
            }
        }
    }

    private fun start(canvas: Canvas) {
        animator = ValueAnimator.ofInt(0, width * 2).apply {
            duration = 6 * 1000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                mOffsetX = it.animatedValue as Int
                drawPre(canvas)
            }
            start()
        }
    }

    private fun drawPre(canvas: Canvas) {
        drawPath(
                canvas,
                randomOffsetX = randomXArr[0],
                direction = -1, level = 4, segment = 4, lineWidth = 1f
        )
//        drawPath(
//                canvas,
//                randomOffsetX = randomXArr[1],
//                direction = -1, level = 2, segment = 3, lineWidth = 2f
//        )
//        drawPath(
//                canvas,
//                randomOffsetX = randomXArr[2],
//                direction = -1, level = 1, segment = 2, lineWidth = 2f
//        )
//        drawPath(
//                canvas,
//                randomOffsetX = randomXArr[3],
//                direction = 1, level = 3, segment = 3, lineWidth = 1f
//        )
//        drawPath(
//                canvas,
//                randomOffsetX = randomXArr[4],
//                direction = 1, level = 2, segment = 2, lineWidth = 2f
//        )
    }

    private fun drawPath(
            canvas: Canvas,
            randomOffsetX: Int,
            direction: Int,
            level: Int,
            segment: Int,
            lineWidth: Float
    ) {
        // 重置
        val path = Path().apply { reset() }

        // 确定几个小段
        val mSegmentsCount = segment
        // 确定曲线的宽高
        val mSegmentsWidth = width / mSegmentsCount
        val mSegmentsHeight = height / 10 * level
        // 确定初始方向, 还需要根据 mOffsetX 动态改变方向
        val random = mOffsetX + randomOffsetX
        var dir = if (random / mSegmentsWidth % 2 == 0) 1 else -1
        dir *= direction
        // 确定虚线的的样式
        mPaint.run {
            strokeWidth = lineWidth
            pathEffect = DashPathEffect(floatArrayOf(lineWidth, lineWidth), 0f)
        }

        // 改变偏移量
        val offset = random % mSegmentsWidth
        // 最前面单独的线, 没有规律, 单独绘制
        prefix(canvas, offset, mSegmentsWidth, mSegmentsHeight, dir, path)
        // 有规律的线, 需要不停的变换方向
        repeat(canvas, offset, mSegmentsCount, mSegmentsWidth, mSegmentsHeight, dir, path)
    }

    /**
     * 第一段, 需要动态绘制, 是没有规律的, 所以单独分开
     * 参数分别为, 画布, 小段宽度, 小段高度, 方向, 画笔路径
     */
    private fun prefix(canvas: Canvas, offset: Int, width: Int, height: Int, direction: Int, path: Path) {
        // 设置起点
        val p1 = Point(0, mCenterY)
        // 设置高度, 辅助点位置
        val pHeight = ((offset.toDouble() / width) * height).toInt()
        val p2 = Point(offset / 2, mCenterY + pHeight * direction)
        // 设置终点位置
        val p3 = Point(offset, mCenterY)
        // 设置 path
        path.moveTo(p1.x.toFloat(), p1.y.toFloat())
        path.quadTo(p2.x.toFloat(), p2.y.toFloat(), p3.x.toFloat(), p3.y.toFloat())
        // 画线
        canvas.drawPath(path, mPaint)
    }

    private fun repeat(canvas: Canvas, offset: Int, count: Int, width: Int, height: Int, direction: Int, path: Path) {
        var dir = direction
        for (index in 0 until count) {
            // 设置起点
            val p1 = Point(offset + width * index, mCenterY)
            // 设置高度, 辅助点位置
            dir *= -1
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

}