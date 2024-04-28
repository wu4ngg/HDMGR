package com.example.hdmgr.etc

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewSeperator (
    context: Context,
    resourceId: Int,
) : RecyclerView.ItemDecoration() {
    private var divider: Drawable = ContextCompat.getDrawable(context, resourceId)!!
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        for (i in 0 until parent.childCount){
            if(i != parent.childCount - 1){
                var child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val dividerTop = child.bottom + params.bottomMargin
                val dividerBottom = dividerTop + divider.intrinsicHeight
                divider.setBounds(0, dividerTop, 0 ,dividerBottom)
                divider.draw(c)
            }
        }
    }
}