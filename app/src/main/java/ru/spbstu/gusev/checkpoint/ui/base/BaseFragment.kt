package ru.spbstu.gusev.checkpoint.ui.base

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import androidx.annotation.ColorInt
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import ru.spbstu.gusev.checkpoint.R
import ru.spbstu.gusev.checkpoint.di.DI
import ru.spbstu.gusev.checkpoint.extensions.getColorFromTheme
import toothpick.Toothpick

abstract class BaseFragment : Fragment() {
    abstract val layoutRes: Int
    abstract val menuRes: Int?

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        menuRes?.let {
            setHasOptionsMenu(true)
        }
        return inflater.inflate(layoutRes, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuRes?.let {
            inflater.inflate(it, menu)
            @ColorInt val color = requireContext().getColorFromTheme(R.attr.colorOnPrimary)
            menu.forEach {
                it.icon.setColorFilter(
                    color, PorterDuff.Mode.SRC_ATOP
                )
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appScope = Toothpick.openScope(DI.APP_SCOPE)
        Toothpick.inject(this, appScope)
    }
}