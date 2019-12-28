package ru.spbstu.gusev.checkpoint.extensions

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt



fun Context.getColorFromTheme(id: Int): Int {
    val typedValue = TypedValue()
    val theme = this.theme
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}