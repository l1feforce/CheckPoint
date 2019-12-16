package ru.spbstu.gusev.checkpoint.extensions

import java.text.NumberFormat
import java.util.*

fun Float.toRubles(): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance("RUB")

    return format.format(this)
}