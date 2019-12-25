package ru.spbstu.gusev.checkpoint.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64


fun String.toBitmap(): Bitmap {
    val decodedByte = Base64.decode(this, 0)
    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
}