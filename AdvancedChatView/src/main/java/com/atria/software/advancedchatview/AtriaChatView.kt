package com.atria.software.advancedchatview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import kotlin.math.roundToInt

class AtriaChatView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet?=null, defStyle: Int=0) :
    ViewGroup(context, attrs, defStyle) {

    companion object{
        private const val TAG = "AtriaChatView"
    }

    private lateinit var circularView: ImageView
    private lateinit var headingTextView: TextView
//    private lateinit var latestTextView: TextView
//    private lateinit var timingTextView: TextView
//    private lateinit var countTextView: TextView
//    private lateinit var actionButton: Button

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        circularView = children.toList()[0] as ImageView
        headingTextView = children.toList()[1] as TextView
//        latestTextView = children.toList()[2] as TextView
//        timingTextView = children.toList()[3] as TextView
//        countTextView = children.toList()[4] as TextView
//        actionButton = children.toList()[5] as Button

        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val x = width.div(60)
        val y = height.div(5)
        val params = circularView.layoutParams.width
        val height = circularView.layoutParams.height

        circularView.layout(x,y,x+params, y+height)

        val start_x = width.div(5.85)
        val heading_width = headingTextView.layoutParams.width
        val heading_height = headingTextView.layoutParams.height
        headingTextView.layout(start_x.roundToInt(),y,start_x.roundToInt()+heading_width,y+heading_height)

    }
}