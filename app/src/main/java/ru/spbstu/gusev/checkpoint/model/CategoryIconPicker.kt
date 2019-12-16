package ru.spbstu.gusev.checkpoint.model

import ru.spbstu.gusev.checkpoint.R

class CategoryIconPicker {
    companion object {
        fun getIconByName(name: String): Int {
            val icons = listOf(
                R.drawable.icon_vga,
                R.drawable.icon_display,
                R.drawable.icon_dram
            )
            return icons.random()
        }
    }
}