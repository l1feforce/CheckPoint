package ru.spbstu.gusev.checkpoint.model

import android.content.Context
import ru.spbstu.gusev.checkpoint.R

class MockRepository(val context: Context) {
    fun getCheckList(): List<CheckItem> {
        val tmpList = mutableListOf<CheckItem>()
        val categories = listOf("Видеокарта", "Монитор", "Оперативная память")
        val icons = listOf(
            R.drawable.icon_vga,
            R.drawable.icon_display,
            R.drawable.icon_dram
        )
        for (i in 0 until 10) {
            val randomNumber = (0..2).random()
            tmpList.add(
                CheckItem(
                    categories[randomNumber],
                    (1..15000).random().toFloat(),
                    getRandomDate(),
                    checkImagePath = "",
                    products = generateProducts()
                )
            )
        }
        return tmpList
    }

    private fun generateProducts(): List<ProductItem> {
        val tmpList = mutableListOf<ProductItem>()
        val categories = listOf("Видеокарта", "Монитор", "Оперативная память")
        for (i in 0..(1..15).random()) {
            tmpList.add(
                ProductItem(categories.random(), (1000..100000).random().toFloat())
            )
        }
        return tmpList
    }

    fun getEmptyCheckList(): List<CheckItem> = emptyList<CheckItem>()

    private fun getRandomDate(): String {
        val day = (10..31).random().toString() + "."
        val month = (10..12).random().toString() + "."
        val year = (1970..2019).random().toString()
        return (day + month + year)
    }
}