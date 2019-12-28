package ru.spbstu.gusev.checkpoint.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
                        val localDbInsertion = async {
                            checkDao.insert(checkItem.apply { id = doc.id })
                        }
                        localDbInsertion.invokeOnCompletion {
                            GlobalScope.launch { update(checkItem) }
                        }
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
                    GlobalScope.launch(Dispatchers.IO) {
                        val localData = checkDao.getAllAsync()
                        Log.v("tag", "local db: $localData")
                        Log.v("tag", "remote db: $result")
                        if (localData.isEmpty() && result.isNotEmpty()) {
                            Log.v("tag", "inside insert all")
                            checkDao.insertAll(result)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.v("tag", it.message)
                }
        }
    }

    suspend fun update(checkItem: CheckItem) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val documentId = checkDao.getIdByItem(checkItem.checkImagePath)
            remoteDB.collection("users")
                .document(currentUser.uid)
                .collection("checks").document(documentId)
                .set(checkItem)
                .addOnFailureListener {
                    Log.v("tag", it.message)
                }
                .addOnSuccessListener {
                    Log.v("tag", "update successfuly")
                }
        }
    }

    private fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

    fun cleanAll() {
        remoteDB.clearPersistence()
    }

}
