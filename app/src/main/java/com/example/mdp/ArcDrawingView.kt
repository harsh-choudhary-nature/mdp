package com.example.mdp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import java.util.Collections.max

class ArcDrawingView(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {
    private val paint = Paint()
    private val path = Path()
    private val points = mutableListOf<Pair<Float, Float>>()
    private val textView: TextView
    private lateinit var textViewText: String

    init {
        paint.color = android.graphics.Color.RED
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        textView = TextView(context)
        textView.setTextColor(android.graphics.Color.BLACK)
        textView.textSize = 16f
        addView(textView)
    }

    fun setCoords(x: Float, y: Float) {
        points.add(Pair(x, y))
//        println("Got $x,$y,${points.size}")
        if (points.size == 3) invalidate()
    }

    fun setTextViewText(text:String){
        this.textViewText = text
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // Layout the TextView at the center of the view
//        val centerX = width / 2
//        val centerY = height / 2
        if(points.size==3){
            textView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val textWidth = kotlin.math.max(textView.measuredHeight,500)
            val textHeight = textView.measuredHeight
            val textLeft = points[1].first.toInt()-100
            val textTop = points[1].second.toInt()

            val textRight = textLeft + textWidth
            val textBottom = textTop + textHeight
//        println("$textLeft,$textRight,$textTop,$textBottom,$textWidth,$textHeight")
            textView.layout(textLeft, textTop, textRight, textBottom)
        }

    }

    override fun dispatchDraw(canvas: Canvas) {


        // Calculate the control points for the quadratic Bezier curve
        val x1 = points[0].first
        val y1 = points[0].second
        val x2 = points[1].first
        val y2 = points[1].second
        val x3 = points[2].first
        val y3 = points[2].second

        val cx = 2 * x2 - (x1 + x3) / 2
        val cy = 2 * y2 - (y1 + y3) / 2

        // Clear the path and start drawing the curve
        path.reset()
        path.moveTo(x1, y1)
        path.quadTo(cx, cy, x3, y3)

        // Draw the curve
        canvas.drawPath(path, paint)

        // Set the text to display in the TextView
        textView.text = textViewText
        super.dispatchDraw(canvas)
    }
}
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (touchEnabled && event.action == MotionEvent.ACTION_DOWN) {
//            // Store the touch positions
//            Toast.makeText(context,"(${event.x},${event.y})",Toast.LENGTH_SHORT).show()
//            val x = event.x
//            val y = event.y
//            points.add(Pair(x, y))
//
//            // Redraw the view
//            invalidate()
//
//            return true
//        }
//        return false
//    }
