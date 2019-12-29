package ru.spbstu.gusev.checkpoint.model

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import ru.spbstu.gusev.checkpoint.extensions.getBitmapByPath
import ru.spbstu.gusev.checkpoint.extensions.toUri
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

class PhotosRepository
@Inject constructor(val context: Context) {

    private val remoteStorage = FirebaseStorage.getInstance()

    fun insert(checkItem: CheckItem) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            context.getBitmapByPath(checkItem.checkImagePath).also {
                val baos = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                remoteStorage.reference.child("users/${currentUser.uid}/checks/${checkItem.id}.jpg")
                    .putBytes(data)
            }
        }
    }

    fun getAll(checks: List<CheckItem>) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            Log.v("tag", "inside getAll photos")
            checks.forEach {
                Log.v("tag", it.checkImagePath.toUri().toString())
                if (!File(it.checkImagePath).exists()) {
                    Log.v("tag", "need to create local file")
                    val file = createImageFile(it.checkImagePath)
                    Log.v("tag", "storage path: users/${currentUser.uid}/checks/${it.id}.jpg")
                    remoteStorage.reference.root.child("users/${currentUser.uid}/checks/${it.id}.jpg")
                        .getFile(file)
                        .addOnFailureListener {
                            Log.v("tag", it.message)
                        }
                }
            }
        }
    }

    fun clearAll() {
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        if (storageDir.isDirectory) {
            storageDir.listFiles().forEach {
                it.delete()
            }
        }
        storageDir.delete()
    }

    private fun createImageFile(filePath: String): File {
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val fileName = filePath.split("/").last()
        return File(storageDir, fileName)
    }

    private fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

}