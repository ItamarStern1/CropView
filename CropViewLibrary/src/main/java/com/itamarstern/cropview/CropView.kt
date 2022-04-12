package com.itamarstern.cropview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt

const val ANDROID_STYLE_NAMESPACE = "http://schemas.android.com/apk/res-auto"
const val TAG = "CropViewTag"

class CropView(
    context: Context,
    private val attrs: AttributeSet,
) : View(
    context,
    attrs
) {
    private val cornerOffset = 4.dpToPixel()


    private var halfLayoutWidth: Int = 0
    private var halfLayoutHeight: Int = 0

    private val cornerBitmapTopLeft: Bitmap =
        getBitmapByAttr(R.styleable.CropView_cornerTopLeft, R.drawable.cropview_corner_top_left)
    private val cornerBitmapTopRight: Bitmap =
        getBitmapByAttr(R.styleable.CropView_cornerTopRight, R.drawable.cropview_corner_top_right)
    private val cornerBitmapBottomLeft: Bitmap =
        getBitmapByAttr(R.styleable.CropView_cornerBottomLeft, R.drawable.cropview_corner_bottom_left)
    private val cornerBitmapBottomRight: Bitmap =
        getBitmapByAttr(R.styleable.CropView_cornerBottomRight, R.drawable.cropview_corner_bottom_right)

    private var cropWidth = getCropDimensionInPx(R.styleable.CropView_initialWidthCrop, 200f)
    private var cropHeight = getCropDimensionInPx(R.styleable.CropView_initialHeightCrop, 200f)

    private val minWidth = getCropDimensionInPx(R.styleable.CropView_minWidthCrop, 200f)
    private val minHeight = getCropDimensionInPx(R.styleable.CropView_minHeightCrop, 200f)

    private var cropMarginTop = getCropDimensionInPx(R.styleable.CropView_cropMarginTop, 100f)

    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cropPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        //Transparent
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var startTouchPointX = 0F
    private var startTouchPointY = 0F

    private var lastX: Float = 0F
    private var lastY: Float = 0F

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        setHalfLayoutDimensionsValues()

        canvas?.apply {
            //Camera rect:
            val sideMargin: Float = ((width - cropWidth) / 2).toFloat()
            drawRect(
                sideMargin,
                cropMarginTop.toFloat(),
                cropWidth + sideMargin,
                (cropHeight + cropMarginTop).toFloat(),
                cropPaint
            )
            //4 corner bitmaps:
            drawBitmap(
                cornerBitmapTopLeft,
                sideMargin - cornerOffset,
                (cropMarginTop - cornerOffset).toFloat(),
                cornerPaint
            )
            drawBitmap(
                cornerBitmapTopRight,
                cropWidth + sideMargin - (cornerBitmapTopRight.width - cornerOffset),
                (cropMarginTop - cornerOffset).toFloat(),
                cornerPaint
            )
            drawBitmap(
                cornerBitmapBottomLeft,
                sideMargin - cornerOffset,
                (cropHeight + cropMarginTop - (cornerBitmapBottomLeft.height - cornerOffset)).toFloat(),
                cornerPaint
            )
            drawBitmap(
                cornerBitmapBottomRight,
                cropWidth + sideMargin - (cornerBitmapBottomRight.width - cornerOffset),
                (cropHeight + cropMarginTop - (cornerBitmapBottomRight.height - cornerOffset)).toFloat(),
                cornerPaint
            )

        }
    }

    private var resizing = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return detector.onTouchEvent(event).let { result ->
            if (!result) {
                when (event?.action) {
                    MotionEvent.ACTION_MOVE -> {
                        //Check of lastX and lastY is for prevent resizing immediately after the touch - but only after touch and move.
                        //For this lastX and lastY are reset immediately after each start of touch - and receive values after moving started.
                        if (lastX != 0F && lastY != 0F) {

                            //Width resizing:
                            resizing = 2 *
                                    if (startTouchPointX >= halfLayoutWidth) (event.x - lastX).roundToInt()
                                    else (lastX - event.x).roundToInt()

                            //Check if the resizing going to over the min/max size: yes - set to the size the min/max size. no - do the resizing.
                            if (cropWidth + resizing < minWidth) cropWidth = minWidth
                            else if (cropWidth + resizing < width) cropWidth += resizing


                            //Height resizing:
                            resizing = 2 *
                                    if (startTouchPointY >= cropMarginTop + cropHeight / 2) (event.y - lastY).roundToInt()
                                    else (lastY - event.y).roundToInt()

                            if (cropHeight + resizing < minHeight) {
                                cropMarginTop += ((cropHeight - minHeight) / 2)
                                cropHeight = minHeight
                            } else if (cropMarginTop - resizing / 2 > 0 && resizing / 2 + cropMarginTop + cropHeight < height) {
                                cropHeight += resizing
                                cropMarginTop -= (resizing / 2)
                            }

                            invalidate()
                        }
                        lastX = event.x
                        lastY = event.y
                        true
                    }
                    else -> {
                        true
                    }
                }
            } else true
        }
    }

    private val touchListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            startTouchPointX = e.x
            startTouchPointY = e.y
            lastX = 0F
            lastY = 0F
            return true
        }
    }

    private val detector: GestureDetector = GestureDetector(context, touchListener)

    private fun getCropDimensionInPx(attr: Int, defValueInDp: Float): Int {
        val typedArray: TypedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CropView, 0, 0
        )
        return typedArray.getDimensionPixelSize(attr, defValueInDp.dpToPixel())
    }


    private fun getBitmapByAttr(attribute: Int, defBitmapResId: Int): Bitmap {
        val typedArray: TypedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CropView, 0, 0)
        val bitmapResId = typedArray.getResourceId(attribute, defBitmapResId)

        return getDrawable(context, bitmapResId)!!.toBitmap()
    }

    private fun Float.dpToPixel(): Int = ((this * resources.displayMetrics.density).roundToInt())
    private fun Int.dpToPixel(): Int = (this * resources.displayMetrics.density).roundToInt()

    private fun setHalfLayoutDimensionsValues() {
        halfLayoutWidth = width / 2
        halfLayoutHeight = height / 2
        if (cropMarginTop == -1) cropMarginTop = ((height - cropHeight) / 2)
    }


    fun getCropRect(): Rect {
        val widthMargin = ((right - cropWidth) / 2)
        val heightMargin = cropMarginTop
        return Rect(
            widthMargin,
            heightMargin,
            (widthMargin + cropWidth),
            (heightMargin + cropHeight)
        )
    }

}