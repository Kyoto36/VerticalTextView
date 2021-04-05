package com.ls.library_verticaltextview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.w3c.dom.Text
import kotlin.math.max

class VerticalTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mPaint: Paint = Paint()
    private var mText: String? = null
    private var mHint: String? = null
    private var mTextColor: Int = Color.BLACK
    private var mHintColor: Int = Color.GRAY
    private var mParentAllocateSize = WidthAndHeight()
    private var mWordSpace = 0F
    private var mLineSpace = 0F

    init {
        mPaint.isAntiAlias = true
        mPaint.textAlign = Paint.Align.LEFT
        val ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalTextView)
        mPaint.textSize = ta.getDimension(R.styleable.VerticalTextView_textSize,60f)
        mTextColor = ta.getColor(R.styleable.VerticalTextView_textColor,mTextColor)
        mHintColor = ta.getColor(R.styleable.VerticalTextView_hintColor,mHintColor)
        mText = ta.getString(R.styleable.VerticalTextView_text)
        mHint = ta.getString(R.styleable.VerticalTextView_hint)
        mWordSpace = ta.getDimension(R.styleable.VerticalTextView_wordSpace,mWordSpace)
        mLineSpace = ta.getDimension(R.styleable.VerticalTextView_lineSpace,mLineSpace)
        ta.recycle()
    }

    fun setText(text: String){
        mText = text
        requestLayout()
    }

    fun setTextColor(color: Int){
        mTextColor = color
        if(!TextUtils.isEmpty(mText)){// 如果mText不为空，那就立即重新绘制，以显示新的mTextColor
            requestLayout()
        }
    }

    fun setHint(hint: String){
        mHint = hint
        if(TextUtils.isEmpty(mText)){ // 如果mText是空，那就立即重新绘制，以显示新的Hint，否则不用重新绘制，节省性能
            requestLayout()
        }
    }

    fun setTextSizeSp(sp: Float){
        mPaint.textSize = sp2px(sp)
        if(!TextUtils.isEmpty(mText) || !TextUtils.isEmpty(mHint)){ // 如果mText和mHint其中一个不为空，那就立即重新绘制，以显示新的TextSize
            requestLayout()
        }
    }

    fun setTextSizePx(px: Float){
        mPaint.textSize = px
        if(!TextUtils.isEmpty(mText) || !TextUtils.isEmpty(mHint)){ // 如果mText和mHint其中一个不为空，那就立即重新绘制，以显示新的TextSize
            requestLayout()
        }
    }

    fun setHintColor(color: Int){
        mHintColor = color
        if(TextUtils.isEmpty(mText) && !TextUtils.isEmpty(mHint)){ // 如果mText为空，并且mHint不为空，那就立即重新绘制，以显示新的mHintColor
            requestLayout()
        }
    }

    /**
     * 指定文字是否能完全显示出来
     * 推荐在ui测量完成之后使用,并且在view宽高是warp的情况下算出来的数没用
     * @param text String
     * @return Boolean
     */
    fun canFillFull(text: String): Boolean{
        if(mParentAllocateSize.width <= 0 && mParentAllocateSize.height <= 0){
            requestLayout()
        }
        if(mParentAllocateSize.width <= 0 || mParentAllocateSize.height <= 0){
            return false
        }
        // 宽度不限定，高度限定为父容器提供的最大高度减去上下边距
        val realWidthAndHeight = getTextSize(text,-1,(mParentAllocateSize.height - paddingTop - paddingBottom).toInt() + 2)
        // 如果文字的显示宽度大于父容器提供的最大宽度减去左右边距 return false
        return realWidthAndHeight.width + paddingLeft + paddingRight <= mParentAllocateSize.width
    }

    /**
     * 预测能显示多少个文字（中文）
     * 推荐在ui测量完成之后使用,并且在view宽高是warp的情况下算出来的数没用
     * @return Int
     */
    fun getPreTextLength(): Int{
        if(mParentAllocateSize.width <= 0 && mParentAllocateSize.height <= 0){
            requestLayout()
        }
        if(mParentAllocateSize.width <= 0 || mParentAllocateSize.height <= 0){
            return 0
        }
        val tempWidthAndHeight = getTempCharWidthAndHeight()
        // 每竖行（也就是列）能显示的个数 = （父容器提供的最大宽度 + 一个行间距（最右边不需要行间距） - 上下边距） / （单个字的宽度 + 行间距）
        val columnWords = ((mParentAllocateSize.width + mLineSpace - paddingLeft - paddingRight) / (tempWidthAndHeight.width + mLineSpace)).toInt()
        // 每横排（也就是行）能显示的个数 = （父容器提供的最大高度 + 一个字间距（最下边不需要字间距） - 左右边距） / （单个字的高度 + 字间距）
        val rowWords = ((mParentAllocateSize.height + mWordSpace - paddingTop - paddingBottom) / (tempWidthAndHeight.height + mWordSpace)).toInt()
        return columnWords * rowWords
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        mParentAllocateSize.setWidthAndHeight(widthSize.toFloat(),heightSize.toFloat())
        if(widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY){
            Log.d("VerticalTextView","固定大小 $widthSize $heightSize")
            setMeasuredDimension(widthSize,heightSize)
            return
        }
        val textSize = getTextSize(
            if(TextUtils.isEmpty(mText)) mHint else mText,
            if(widthMode == MeasureSpec.UNSPECIFIED) -1 else widthSize - paddingLeft - paddingRight, // UNSPECIFIED不限定；限定最大宽度不能超过父容器提供的宽度减去左右内边距
            if(heightMode == MeasureSpec.UNSPECIFIED) -1 else heightSize - paddingTop - paddingBottom // UNSPECIFIED不限定；限定最大高度不能超过父容器提供的高度减去上下内边距
        )
        widthSize = if(widthMode == MeasureSpec.EXACTLY) {
            Log.d("VerticalTextView","宽度固定 width $widthSize")
            widthSize
        } else {
            Log.d("VerticalTextView","宽度不固定 width ${textSize.width}")
            if(textSize.width <= 0) 0 else textSize.width.toInt() + 2 // 测量宽度 <=0，就用0，否者用测量宽度 +2
        }
        heightSize = if(heightMode == MeasureSpec.EXACTLY){
            Log.d("VerticalTextView","高度固定 height $heightSize")
            heightSize
        }
        else{
            Log.d("VerticalTextView","高度不固定 height ${textSize.height}")
            if(textSize.height <= 0) 0 else textSize.height.toInt() + 2 // 测量高度 <=0，就用0，否者用测量高度 +2
        }
        setMeasuredDimension(widthSize,heightSize) // +2是因为float转int的时候小数会抹掉，所以给点冗余空间
    }

    private fun getTextSize(text: String?,maxWidth: Int,maxHeight: Int): WidthAndHeight{
        if(maxHeight == 0 || maxWidth == 0 || TextUtils.isEmpty(text)){
            return WidthAndHeight(0F,0F)
        }
        var widthSize = 0F
        var heightSize = 0F
        var currWidth = 0F
        var currHeight = 0F
        var charWidthAndHeight: WidthAndHeight
        val tempWidthAndHeight = getTempCharWidthAndHeight()
        var char: Char
        var charMaxWidth = tempWidthAndHeight.width
        for (i in text!!.indices) {
            char = text[i]
            charWidthAndHeight = getCharWidthAndHeight(char)
            if(currHeight == 0F && maxHeight > 0 && charWidthAndHeight.height > maxHeight){ //如果每列第一个字的高度都大雨最大限定高度，那么就不用继续计算了，继续计算只会产生死循环
                break
            }
            if(maxWidth > 0 && currWidth > maxWidth){ // 如果宽度不够显示当前列，那就也不用计算了，节约性能
                charMaxWidth = 0F
                break
            }
            charMaxWidth = Math.max(charMaxWidth,charWidthAndHeight.width) // 本行字中宽度最大的
            if(char == '\n' || (maxHeight > 0 && (currHeight + charWidthAndHeight.height) > maxHeight)){ // 如果当前字符是换行符或者剩余高度已经不能显示下当前字符，那就换列
                // 如果最大高度小于0，说明测量模式是MeasureSpec.UNSPECIFIED，那就不用在意最大限定高度了，直接取历史最大高度和当前列的高度的最大值，
                // 否则比完历史高度与当前列的高度最大值，然后再拿最大值与最大限定高度比最小值
                heightSize = if(maxHeight < 0) Math.max(heightSize,currHeight) else Math.min(Math.max(heightSize,currHeight), maxHeight.toFloat())
                currHeight = 0F
                currWidth += charMaxWidth + mLineSpace // 加上行间距
                if(char == '\n') { // 如果当前字符是换行符，那就不用统计其高度
                    continue
                }
            }
            currHeight += charWidthAndHeight.height + mWordSpace // 加上字间距
        }
        // 最后一行也要统计在其中
        heightSize = if(maxHeight < 0) Math.max(heightSize,currHeight) else Math.min(Math.max(heightSize,currHeight), maxHeight.toFloat())
        currWidth += charMaxWidth
        // 如果最大宽度小于0，说明测量模式是MeasureSpec.UNSPECIFIED，那就不用在意最大限定宽度了，直接取测量的宽度
        // 否则那测量宽度和最大限定宽度取最小值
        widthSize = if(maxWidth < 0) currWidth else Math.min(currWidth,maxWidth.toFloat())
        Log.d("VerticalTextView","maxWidth $maxWidth currWidth $currWidth maxHeight $maxHeight heightSize $heightSize")
        return WidthAndHeight(widthSize, heightSize)
    }
    
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas == null){
            return
        }
        if(!TextUtils.isEmpty(mText)){
            mPaint.color = mTextColor
            drawText(canvas,mPaint,mText!!)
            return
        }
        if(!TextUtils.isEmpty(mHint)){
            mPaint.color = mHintColor
            drawText(canvas,mPaint,mHint!!)
            return
        }
    }

    private fun drawText(canvas: Canvas,paint: Paint,text: String){
        Log.d("VerticalTextView","draw measure $measuredWidth $measuredHeight")
        var startX = paddingLeft.toFloat()
        var startY = paddingTop.toFloat()
        var charWidthAndHeight: WidthAndHeight
        val tempWidthAndHeight = getTempCharWidthAndHeight()
        var baseLine : Float
        var charMaxWidth = tempWidthAndHeight.width
        var char: Char
        for (i in text.indices) {
            char = text[i]
            charWidthAndHeight = getCharWidthAndHeight(char)
            baseLine = charWidthAndHeight.height - paint.fontMetrics.bottom // 获取每个字的基线
            charMaxWidth = if (charWidthAndHeight.width > charMaxWidth) charWidthAndHeight.width else charMaxWidth // 本行字中宽度最大的
            if (startY + charWidthAndHeight.height + paddingBottom > measuredHeight && startX + charMaxWidth + paddingRight > measuredWidth) { // 如果高度和高度都超过限制那就不继续绘制了
                break
            }
            if (char == '\n' || startY + charWidthAndHeight.height + paddingBottom > measuredHeight) { // 如果只是高度超过限制，那就另起一列继续绘制
                startY = paddingTop.toFloat()
                startX += (charMaxWidth + mLineSpace)  //加上当前行的宽度与行间距
                charMaxWidth = tempWidthAndHeight.width
            }
            if(char == '\n'){
                continue
            }
            canvas.drawText(char.toString(), startX, startY + baseLine, paint)
            startY += (charWidthAndHeight.height + mWordSpace) // 绘制完之后下一个字的开始位置就是现在的开始位置加上现在的字的高度和字间距
        }
    }

    private fun getTempCharWidthAndHeight(): WidthAndHeight{
        return getCharWidthAndHeight('正')
    }

    private fun getCharWidthAndHeight(char: Char): WidthAndHeight{
        val height = mPaint.fontMetrics.bottom - mPaint.fontMetrics.top
        val widths = FloatArray(1)
        mPaint.getTextWidths(char.toString(),widths)
        val width = widths[0]
        return WidthAndHeight(width, height)
    }

    private fun sp2px(sp: Float): Float{
        return (sp * Resources.getSystem().displayMetrics.scaledDensity)
    }
}