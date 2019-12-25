package ru.spbstu.gusev.checkpoint.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class BitmapTypeConverter {
    @TypeConverter
    fun toBitmap(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    @TypeConverter
    fun fromBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes?.size ?: 0)
    }
}