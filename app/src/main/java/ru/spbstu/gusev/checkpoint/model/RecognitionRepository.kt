package ru.spbstu.gusev.checkpoint.model

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText
import ru.spbstu.gusev.checkpoint.extensions.getBitmapByPath

class RecognitionRepository {
    companion object {
        val newCheck = CheckItem.createDefault()

        fun recognize(
            pathToPhoto: String, context: Context, onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ): CheckItem {
            recognizeImageFromCamera(pathToPhoto, context, onSuccess, onFailure)
            return newCheck
        }

        private fun recognizeImageFromCamera(
            imagePath: String, context: Context,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            val image = context.getBitmapByPath(imagePath)
            val firebaseImage = FirebaseVisionImage.fromBitmap(image)
            var recognizedText = FirebaseVisionText.zzbnv
            val resultText: Task<FirebaseVisionText> = getTextFromImage(firebaseImage)
            resultText.continueWithTask { task ->
                val firebaseVisionText = task.result
                recognizedText = firebaseVisionText
                Log.v("TAG", recognizedText.text)
                getBarcodesFromImage(firebaseImage)
            }.addOnSuccessListener {
                val recognizedQr = if (it.isNotEmpty()) it.first().rawValue else ""
                val parsedCheck = CheckParser.parse(recognizedText, recognizedQr ?: "")
                newCheck.copy(parsedCheck)
                newCheck.checkImagePath = imagePath
                onSuccess()
            }.addOnFailureListener {
                onFailure(it)
            }
        }

        private fun getTextFromImage(firebaseImage: FirebaseVisionImage): Task<FirebaseVisionText> {
            val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(listOf("en", "ru"))
                .build()
            val textDetector = FirebaseVision.getInstance().getCloudTextRecognizer(options)
            return textDetector.processImage(firebaseImage)
        }

        private fun getBarcodesFromImage(firebaseImage: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> {
            val barcodeDetector = FirebaseVision.getInstance()
                .visionBarcodeDetector
            return barcodeDetector.detectInImage(firebaseImage)
        }
    }
}