package ru.spbstu.gusev.checkpoint.extensions

import androidx.lifecycle.MutableLiveData

operator fun <T> MutableLiveData<List<T>>.plusAssign(values: List<T>) {
    val oldValue = this.value ?: listOf()
    val temp = mutableListOf<T>()
    temp.addAll(oldValue)
    temp.addAll(values)
    this.value = temp
}