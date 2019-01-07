package com.dqdana.code.bezierview.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

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
        isAntiAlias = true // 锯齿
        // isDither = true // 抖动
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

        // 去除黑底
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
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
            // 绘制
            mCanvas?.let {
                start(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (null != mCanvas) {
                mHolder?.unlockCanvasAndPost(mCanvas)
            }
        }
    }

    /**
     * 这里用来控制循环长度(时间)or速度
     */
    private fun start(canvas: Canvas) {
        val max = width * 2 // 一整段循环结束后,会顿一下;所以弄长一点,不容易被发现O(∩_∩)O哈！
        val unit = max / 200
        if (mOffsetX < max) {
            mOffsetX += unit
        } else {
            mOffsetX = 0
        }
        drawPre(canvas)
    }

    private fun drawPre(canvas: Canvas) {
        // 清屏
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        // 绘制
        drawPath(
                canvas,
                randomOffsetX = randomXArr[0],
                direction = -1, level = 4, segment = 4, lineWidth = 1f
        )
        drawPath(
                canvas,
                randomOffsetX = randomXArr[1],
                direction = -1, level = 2, segment = 3, lineWidth = 2f
        )
        drawPath(
                canvas,
                randomOffsetX = randomXArr[2],
                direction = -1, level = 1, segment = 2, lineWidth = 2f
        )
        drawPath(
                canvas,
                randomOffsetX = randomXArr[3],
                direction = 1, level = 3, segment = 3, lineWidth = 1f
        )
        drawPath(
                canvas,
                randomOffsetX = randomXArr[4],
                direction = 1, level = 2, segment = 2, lineWidth = 2f
        )
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
        // 分段绘制
        when (mSegmentsCount) {
            1 -> {
            }
            2 -> {
            }
            3 -> {
            }
            4 -> {
            }
            5 -> {
            }
        }

        prefix(canvas,
                0,
                offset,
                offset,
                mSegmentsWidth, mSegmentsHeight, dir, path)
        dir *= -1 // 每绘制完一小段,改变一次方向


        prefix(canvas,
                offset,

                (mSegmentsWidth * 1 + offset * 0.5).toInt(),

                offset + mSegmentsWidth * 1,
                mSegmentsWidth, mSegmentsHeight, dir, path)

        dir *= -1
        prefix(canvas,
                offset + mSegmentsWidth * 1,

                (mSegmentsWidth * 1.5 + offset * 0.25).toInt(),

                offset + mSegmentsWidth * 2,
                mSegmentsWidth, mSegmentsHeight, dir, path)

        dir *= -1
        prefix(canvas,
                offset + mSegmentsWidth * 2,

                (mSegmentsWidth * 1.75 - offset * 0.5).toInt(),

                offset + mSegmentsWidth * 3,
                mSegmentsWidth, mSegmentsHeight, dir, path)

        dir *= -1
        prefix(canvas,
                offset + mSegmentsWidth * 3,

                (mSegmentsWidth * 1.25 - offset * 0.5).toInt(),

                offset + mSegmentsWidth * 4,
                mSegmentsWidth, mSegmentsHeight, dir, path)
    }

    /**
     * 第一段, 需要动态绘制, 是没有规律的, 所以单独分开
     * 参数分别为, 画布, 小段宽度, 小段高度, 方向, 画笔路径
     */
    private fun prefix(canvas: Canvas,
                       start: Int, offset: Int, end: Int,
                       width: Int, height: Int, direction: Int, path: Path) {
        // 设置起点
        val p1 = Point(start, mCenterY)
        // 设置高度, 辅助点位置, 根据宽度的比例, 计算高度
        val pHeight = ((offset.toDouble() / width) * height).toInt()
        val p2 = Point(start + (end - start) / 2, mCenterY + pHeight * direction)
        // 设置终点位置
        val p3 = Point(end, mCenterY)
        // 设置 path
        path.moveTo(p1.x.toFloat(), p1.y.toFloat())
        path.quadTo(p2.x.toFloat(), p2.y.toFloat(), p3.x.toFloat(), p3.y.toFloat())
        // 画线
        canvas.drawPath(path, mPaint)
    }
}