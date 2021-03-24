package com.ls.library_verticaltextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class VerticalTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mPaint: Paint = Paint()
    private var mText: String? = null

    init {
        mPaint.color = Color.BLACK
        mPaint.isAntiAlias = true
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.textSize = 60f
    }

    fun setText(text: String){
        mText = text
        requestLayout()

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas == null || mText == null){
            return
        }
        var startX = 0f
        var startY = 0f
        val baseLine = getBaseLine()
        var charWidthAndHeight: WidthAndHeight
        val tempWidthAndHeight = getCharWidthAndHeight('正')
        var charMaxWidth = tempWidthAndHeight.width
        for (i in mText!!.indices) {
            charWidthAndHeight = getCharWidthAndHeight(mText!![i])
            charMaxWidth = if (charWidthAndHeight.width > charMaxWidth) charWidthAndHeight.width else charMaxWidth
            if (startY > measuredHeight && startX > measuredWidth) {
                break
            }
            canvas.drawText(mText!![i].toString(), startX, startY + baseLine, mPaint)
            if (startY + charWidthAndHeight.height > measuredHeight) {
                charMaxWidth = tempWidthAndHeight.width
                startY = 0f
                startX += charMaxWidth
            }
            else{
                startY += charWidthAndHeight.height
            }
        }
    }

    private fun getBaseLine(): Float{
        val height = mPaint.fontMetrics.bottom - mPaint.fontMetrics.top
        return height - mPaint.fontMetrics.bottom
    }

    private fun getCharWidthAndHeight(char: Char): WidthAndHeight{
        val height = mPaint.fontMetrics.bottom - mPaint.fontMetrics.top
        val widths = FloatArray(1)
        mPaint.getTextWidths(char.toString(),widths)
        val width = widths[0]
        return WidthAndHeight(width, height)
    }

}