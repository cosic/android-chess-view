package com.cosic.chessview.utils

import android.content.Context
import android.graphics.Point
import android.util.TypedValue
import android.view.WindowManager

object DimenTools {

    fun dpFromPx(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    @JvmStatic
    fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun pxByDensity(context: Context, px: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics)
    }

    fun spFromPx(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.scaledDensity
    }

    @JvmStatic
    fun pxFromSp(context: Context, sp: Float): Float {
        return sp * context.resources.displayMetrics.scaledDensity
    }

    fun displaySize(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }
}