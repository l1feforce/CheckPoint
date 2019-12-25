package ru.spbstu.gusev.checkpoint.database

import androidx.room.TypeConverter
import ru.spbstu.gusev.checkpoint.model.ProductItem

class ProductItemListTypeConverter {

    @TypeConverter
    fun fromProductItemList(productItemList: List<ProductItem>): String {
        val stringBuilder = StringBuilder()
        productItemList.forEachIndexed { index, it ->
            stringBuilder.append("${it.productName} to ${it.productPrice}")
            if (index + 1 != productItemList.size) {
                stringBuilder.append(", ")
            }
        }
        return stringBuilder.toString()
    }

    @TypeConverter
    fun toProductItemList(string: String): List<ProductItem> {
        val resultList = mutableListOf<ProductItem>()
        val pairs = string.split(", ")
        pairs.forEach {
            val productItem = it.split(" to ")
            resultList.add(ProductItem(productItem.first(), productItem.last().toFloat()))
        }
        return resultList
    }
}