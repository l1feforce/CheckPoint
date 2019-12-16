package ru.spbstu.gusev.checkpoint.model

import android.util.Log
import com.google.firebase.ml.vision.text.FirebaseVisionText

class CheckParser {
    companion object {
        val parsedCheck = CheckItem.createDefault()

        fun parse(recognizedText: FirebaseVisionText?, recognizedQr: String): CheckItem {
            parseQr(recognizedQr)
            parseText(recognizedText)
            return parsedCheck
        }

        private fun parseQr(qrText: String) {
            val date = """t=(.*?)T""".toRegex().find(qrText)?.value ?: ""
            val sum = """s=(.*?)&""".toRegex().find(qrText)?.value ?: ""
            Log.v("tag", "date: $date")
            Log.v("tag", "finalPrice: $sum")
            if (date.isNotEmpty()) {
                parsedCheck.date =
                    (date.subSequence(2..5).toString() + "." + date.subSequence(6..7) + "." + date.subSequence(
                        8..9
                    ))
            }
            if (sum.isNotEmpty()) {
                parsedCheck.finalPrice = sum.subSequence(2..sum.length - 2).toString().toFloat()
            }
        }

        private fun parseText(text: FirebaseVisionText?) {
            val defaultDate = "12.12.1754"
            val defaultPrice = CheckItem.createDefault().finalPrice
            text?.let {
                if (parsedCheck.date == defaultDate) {
                    //12.12.2012 or 12.12.12
                    val dateRegex =
                        """((\d\d)\.(\d\d)\.(\d\d\d\d))|((\d\d)\.(\d\d)\.(\d\d))""".toRegex()
                    val iterator = dateRegex.findAll(text.text).iterator()
                    val values = iterator.asSequence().toList().map { it.value }
                    if (values.isNotEmpty()) {
                        parsedCheck.date = values.first()
                    }
                }
                if (parsedCheck.finalPrice == defaultPrice) {
                    val priceRegex =
                        """(\d+\.\d\d)""".toRegex()
                    val iterator = priceRegex.findAll(text.text).iterator()
                    val values = iterator.asSequence().toList().map { it.value }
                    if (values.isNotEmpty()) {
                        parsedCheck.finalPrice = values.maxBy { it.toDouble() }?.toFloat() ?: defaultPrice
                    }
                }
            }
        }

        fun List<String>.getNearestDate(): String {
            TODO()
        }
    }
}