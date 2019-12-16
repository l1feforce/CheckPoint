package ru.spbstu.gusev.checkpoint.viewmodel.base

import androidx.lifecycle.ViewModel
import ru.spbstu.gusev.checkpoint.di.DI
import toothpick.Toothpick

abstract class BaseViewModel : ViewModel() {
    private val scope = Toothpick.openScope(DI.APP_SCOPE)

    init {
        Toothpick.inject(this, scope)
    }
}