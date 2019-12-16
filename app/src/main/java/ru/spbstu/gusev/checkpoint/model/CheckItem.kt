package ru.spbstu.gusev.checkpoint.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_table")
data class CheckItem(
    var categoryName: String = "",
    var finalPrice: Float = 0f,
    var date: String = "",
    var shop: String = "",
    var products: List<ProductItem> = listOf(),
    var checkImagePath: String = "",
    var checkText: String = "",
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) {
    companion object {
        fun createDefault() = CheckItem(
            "",
            0f,
            "",
            shop = "",
            products = listOf(
                ProductItem("", 0f)
            )
        )
    }

    fun copy(checkItem: CheckItem) {
        id = checkItem.id
        categoryName = checkItem.categoryName
        finalPrice = checkItem.finalPrice
        date = checkItem.date
        shop = checkItem.shop
        products = checkItem.products
        checkImagePath = checkItem.checkImagePath
        checkText = checkItem.checkText
    }
}
