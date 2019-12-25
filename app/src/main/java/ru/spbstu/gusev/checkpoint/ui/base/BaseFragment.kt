package ru.spbstu.gusev.checkpoint.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.spbstu.gusev.checkpoint.di.DI
import toothpick.Toothpick

abstract class BaseFragment: Fragment() {
    abstract val layoutRes: Int


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(layoutRes, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appScope = Toothpick.openScope(DI.APP_SCOPE)
        Toothpick.inject(this, appScope)
    }
}