package ru.spbstu.gusev.checkpoint.extensions

import android.content.Context
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.google.android.material.snackbar.Snackbar
import com.stfalcon.imageviewer.StfalconImageViewer
import ru.spbstu.gusev.checkpoint.ui.CheckOverlayView

val Bitmap.getDefault: Bitmap
    get() = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)


fun toast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}

fun Long.toDate() =
    "${this.toString().subSequence(0..1)}.${this.toString().subSequence(2..3)}.${this.toString().subSequence(
        4..7
    )}"

fun View.snackbar(text: String) {
    Snackbar.make(this, text, Snackbar.LENGTH_LONG).show()
}

fun Context.getBitmapByPath(path: String): Bitmap {
    return try {
        setRightRotation(
            MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                Uri.parse("file://$path")
            )
            , path
        )

    } catch (e: Exception) {
        Log.v("TAG", "There is no photo by this path: $path")
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }
}

fun String.toUri(): Uri {
    return try {
        Uri.parse("file://$this")
    } catch (e: Exception) {
        Log.v("TAG", "There is no photo by this path")
        Uri.parse("")
    }
}

private fun setRightRotation(image: Bitmap, currentPhotoPath: String): Bitmap {
    val ei = ExifInterface(currentPhotoPath)
    val orientation = ei.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> TransformationUtils.rotateImage(image, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> TransformationUtils.rotateImage(image, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> TransformationUtils.rotateImage(image, 270)
        else -> image
    }
}

fun Fragment.showImageViewer(photoPath: Uri, imageForTransition: ImageView) {
    StfalconImageViewer.Builder<Uri>(
        context,
        listOf(photoPath)
    ) { view, image ->
        Glide.with(requireContext()).load(image).into(view)
    }.withOverlayView(CheckOverlayView(requireContext()))
        .withTransitionFrom(imageForTransition)
        .show()
}
