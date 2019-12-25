package ru.spbstu.gusev.checkpoint.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.spbstu.gusev.checkpoint.database.CheckDatabase
import javax.inject.Inject

class FirestoreRepository @Inject constructor
    (
    private val checkDatabase: CheckDatabase
) {
    private val remoteDB = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .build()
    }

    val checkList = MutableLiveData<List<CheckItem>>()

    init {
        FirebaseAuth.getInstance().addAuthStateListener {
            getAll()
        }
    }

    fun insert(checkItem: CheckItem) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            remoteDB.collection("users/${currentUser.uid}/checks")
                .add(checkItem)
                .addOnSuccessListener { doc ->
                    Log.v("tag", "success: ${doc.id}")
                    GlobalScope.launch(Dispatchers.IO) {
                        checkDatabase.checkDao().insert(checkItem.apply { id = doc.id })
                    }
                }
                .addOnFailureListener {
                    Log.v("tag", it.message)
                }
        }
    }

    fun getAll() {
        val currentUser = getCurrentUser()
        currentUser?.let {
            remoteDB.collection("users")
                .document(it.uid)
                .collection("checks")
                .get()
                .addOnSuccessListener { snapshot ->
                    val result = snapshot.toObjects(CheckItem::class.java)
                    checkList.value = result
                }
                .addOnFailureListener {
                    Log.v("tag", it.message)
                }
        }
    }

    suspend fun update(checkItem: CheckItem) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val documentPath = checkDatabase.checkDao().getIdByItem(checkItem.checkImagePath)
            remoteDB.collection("users")
                .document(currentUser.uid)
                .collection("checks").document(documentPath)
                .set(checkItem.apply { id = "" })
                .addOnFailureListener {
                    Log.v("tag", it.message)
                }
        }
    }

    private fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

}
