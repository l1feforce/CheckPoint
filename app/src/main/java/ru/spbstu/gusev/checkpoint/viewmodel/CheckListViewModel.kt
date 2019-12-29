package ru.spbstu.gusev.checkpoint.viewmodel

import androidx.lifecycle.MutableLiveData
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.CheckRepository
import ru.spbstu.gusev.checkpoint.viewmodel.base.BaseViewModel
import javax.inject.Inject

class CheckListViewModel : BaseViewModel() {
    //Data part
    @Inject
    lateinit var currentCheckItem: CheckItem
    @Inject
    lateinit var checkRepository: CheckRepository

    //UI part
    val allChecks = checkRepository.checkList
    val isLoading = MutableLiveData(true)
    val errorText = MutableLiveData("")

    suspend fun addCheck(check: CheckItem) {
        checkRepository.insert(check)?.also {
            it.addOnFailureListener { exception ->
                errorText.value = exception.message
            }
        }
    }

    fun refreshData() {
        isLoading.value = true
        checkRepository.getAll()?.also {
            it.addOnFailureListener { exception ->
                errorText.value = exception.message
            }
        }
    }

    suspend fun updateCheck(check: CheckItem) = checkRepository.update(check)?.also {
        it.addOnFailureListener { exception ->
            errorText.value = exception.message
        }
    }

    fun setCurrentCheckView(checkItem: CheckItem) {
        currentCheckItem.copy(checkItem)
    }


    fun cleanData() {
        checkRepository.clearAll()
    }

}