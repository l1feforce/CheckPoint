package ru.spbstu.gusev.checkpoint.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import ru.spbstu.gusev.checkpoint.R

class CheckOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    init {
        View.inflate(context, R.layout.check_viewer_overlay, this)
        setBackgroundColor(Color.TRANSPARENT)
    }
}