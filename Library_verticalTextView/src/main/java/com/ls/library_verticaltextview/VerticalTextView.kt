package com.ls.library_verticaltextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.max

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if(widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY){
            Log.d("VerticalTextView","固定大小 $widthSize $heightSize")
            setMeasuredDimension(widthSize,heightSize)
            return
        }
        val textSize = getTextSize(
            if(widthMode == MeasureSpec.UNSPECIFIED) -1 else widthSize,
            if(heightMode == MeasureSpec.UNSPECIFIED) -1 else heightSize
        )
        Log.d("VerticalTextView","测量大小 mode ${widthMode == MeasureSpec.EXACTLY} ${heightMode == MeasureSpec.AT_MOST} size ${textSize.width.toInt()} ${textSize.height.toInt()}")
        setMeasuredDimension(textSize.width.toInt(),textSize.height.toInt())
    }

    private fun getTextSize(maxWidth: Int,maxHeight: Int): WidthAndHeight{
        if(maxHeight == 0 || maxWidth == 0 || TextUtils.isEmpty(mText)){
            return WidthAndHeight(0F,0F)
        }
        var widthSize = 0F
        var heightSize = 0F
        var currWidth = 0F
        var currHeight = 0F
        var charWidthAndHeight: WidthAndHeight
        val tempWidthAndHeight = getCharWidthAndHeight('正')
        var char: Char
        var charMaxWidth = tempWidthAndHeight.width
        for (i in mText!!.indices) {
            char = mText!![i]
            charWidthAndHeight = getCharWidthAndHeight(char)
            if(currHeight == 0F && maxHeight > 0 && charWidthAndHeight.height > maxHeight){ //如果每列第一个字的高度都大雨最大限定高度，那么就不用继续计算了，继续计算只会产生死循环
                break
            }
            if(currWidth > maxWidth){ // 如果宽度不够显示当前列，那就也不用计算了，节约性能
                charMaxWidth = 0F
                break
            }
            charMaxWidth = if (charWidthAndHeight.width > charMaxWidth) charWidthAndHeight.width else charMaxWidth // 本行字中宽度最大的
            if(char == '\n' || (maxHeight > 0 && (currHeight + charWidthAndHeight.height) > maxHeight)){ // 如果当前字符是换行符或者剩余高度已经不能显示下当前字符，那就换列
                // 如果最大高度小于0，说明测量模式是MeasureSpec.UNSPECIFIED，那就不用在意最大限定高度了，直接取历史最大高度和当前列的高度的最大值，
                // 否则比完历史高度与当前列的高度最大值，然后再拿最大值与最大限定高度比最小值
                heightSize = if(maxHeight < 0) Math.max(heightSize,currHeight) else Math.min(Math.max(heightSize,currHeight), maxHeight.toFloat())
                currHeight = 0F
                currWidth += charMaxWidth
                if(char == '\n') { // 如果当前字符是换行符，那就不用统计其高度
                    continue
                }
            }
            currHeight += charWidthAndHeight.height
        }
        // 最后一行也要统计在其中
        currWidth += charMaxWidth
        // 如果最大宽度小于0，说明测量模式是MeasureSpec.UNSPECIFIED，那就不用在意最大限定宽度了，直接取测量的宽度
        // 否则那测量宽度和最大限定宽度取最小值
        widthSize = if(maxWidth < 0) currWidth else Math.min(currWidth,maxWidth.toFloat())
        Log.d("VerticalTextView","maxWidth $maxWidth currWidth $currWidth height $maxHeight heightSize $heightSize")
        return WidthAndHeight(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas == null || mText == null){
            return
        }
        var startX = 0f
        var startY = 0f
        var charWidthAndHeight: WidthAndHeight
        val tempWidthAndHeight = getCharWidthAndHeight('正')
        var baseLine : Float
        var charMaxWidth = tempWidthAndHeight.width
        var char: Char
        for (i in mText!!.indices) {
            char = mText!![i]
            charWidthAndHeight = getCharWidthAndHeight(char)
            baseLine = charWidthAndHeight.height - mPaint.fontMetrics.bottom // 获取每个字的基线
            charMaxWidth = if (charWidthAndHeight.width > charMaxWidth) charWidthAndHeight.width else charMaxWidth // 本行字中宽度最大的
            if (startY + charWidthAndHeight.height > measuredHeight && startX + charMaxWidth > measuredWidth) { // 如果高度和高度都超过限制那就不继续绘制了
                break
            }
            if (char == '\n' || startY + charWidthAndHeight.height > measuredHeight) { // 如果只是高度超过限制，那就另起一列继续绘制
                startY = 0f
                startX += charMaxWidth
                charMaxWidth = tempWidthAndHeight.width
            }
            if(char == '\n'){
                continue
            }
            canvas.drawText(char.toString(), startX, startY + baseLine, mPaint)
            startY += charWidthAndHeight.height // 绘制完之后下一个字的开始位置就是现在的开始位置加上现在的字的高度
        }
    }

    private fun getCharWidthAndHeight(char: Char): WidthAndHeight{
        val height = mPaint.fontMetrics.bottom - mPaint.fontMetrics.top
        val widths = FloatArray(1)
        mPaint.getTextWidths(char.toString(),widths)
        val width = widths[0]
        return WidthAndHeight(width, height)
    }

}