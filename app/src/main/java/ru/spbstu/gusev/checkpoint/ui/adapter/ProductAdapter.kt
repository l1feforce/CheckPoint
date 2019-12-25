package ru.spbstu.gusev.checkpoint.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.product_item.view.product_name_text
import kotlinx.android.synthetic.main.product_item.view.product_price_text
import kotlinx.android.synthetic.main.product_item_editable.view.*
import ru.spbstu.gusev.checkpoint.R
import ru.spbstu.gusev.checkpoint.extensions.toRubles
import ru.spbstu.gusev.checkpoint.model.ProductItem
import ru.spbstu.gusev.checkpoint.model.ProductType

class ProductAdapter(private val productType: ProductType) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var productItemList = mutableListOf<ProductItem>()
        get() = field

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (productType == ProductType.PRODUCT) {
            ProductViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.product_item,
                    parent,
                    false
                )
            )
        } else {
            EditableProductViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.product_item_editable,
                    parent,
                    false
                )
            )
        }


    override fun getItemCount(): Int = productItemList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        if (productType == ProductType.PRODUCT) {
            (holder as ProductViewHolder).bind(productItemList[position])
        } else {
            (holder as EditableProductViewHolder).bind(productItemList[position])
        }

    fun updateProducts(productList: List<ProductItem>) {
        this.productItemList = productList.toMutableList()
        notifyDataSetChanged()
    }

    fun addProducts(productList: List<ProductItem>) {
        productList.forEach { this.productItemList.add(it) }
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(productItem: ProductItem) {
            view.apply {
                product_name_text.text = productItem.productName
                product_price_text.text = productItem.productPrice.toRubles()
            }
        }
    }

    inner class EditableProductViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(editableProductItem: ProductItem) {
            view.apply {
                product_name_text.text = editableProductItem.productName
                product_price_text.text = editableProductItem.productPrice.toString()
                remove_button.setOnClickListener {
                    productItemList.remove(editableProductItem)
                    notifyDataSetChanged()
                }
            }
        }
    }
}