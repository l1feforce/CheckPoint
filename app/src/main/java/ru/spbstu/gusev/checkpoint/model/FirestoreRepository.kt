package ru.spbstu.gusev.checkpoint.model

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import ru.spbstu.gusev.checkpoint.database.CheckDatabase
import javax.inject.Inject

class FirestoreRepository @Inject constructor
    (
    checkDatabase: CheckDatabase
) {
    private val remoteDB = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .build()
    }

    private val checkDao = checkDatabase.checkDao()
    val checkList = MutableLiveData<List<CheckItem>>()

    fun insert(checkItem: CheckItem): Task<DocumentReference>? {
        val currentUser = getCurrentUser()
        return if (currentUser != null) {
            remoteDB.collection("users/${currentUser.uid}/checks")
                .add(checkItem)
        } else null
    }

    fun getAll(): Task<QuerySnapshot>? {
        val currentUser = getCurrentUser()
        return if (currentUser != null) {
            remoteDB.collection("users")
                .document(currentUser.uid)
                .collection("checks")
                .get()
        } else null
    }

    suspend fun update(checkItem: CheckItem): Task<Void>? {
        val currentUser = getCurrentUser()
        return if (currentUser != null) {
            val documentId = checkDao.getIdByItem(checkItem.checkImagePath)
            remoteDB.collection("users")
                .document(currentUser.uid)
                .collection("checks").document(documentId)
                .set(checkItem)
        } else null
    }

    private fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

    fun clearAll() {
        remoteDB.clearPersistence()
    }

}
