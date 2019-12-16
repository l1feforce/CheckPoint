package ru.spbstu.gusev.checkpoint.ui.checklist.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.check_item.view.*
import ru.spbstu.gusev.checkpoint.R
import ru.spbstu.gusev.checkpoint.extensions.toRubles
import ru.spbstu.gusev.checkpoint.model.CategoryIconPicker
import ru.spbstu.gusev.checkpoint.model.CheckItem

class CheckAdapter :
    RecyclerView.Adapter<CheckAdapter.CheckViewHolder>() {

    private var checkItemList = emptyList<CheckItem>()
    var onItemClickListener:((CheckItem) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckViewHolder =
        CheckViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.check_item,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = checkItemList.size

    override fun onBindViewHolder(holder: CheckViewHolder, position: Int) =
        holder.bind(checkItemList[position])

    fun updateChecks(checkList: List<CheckItem>) {
        this.checkItemList = checkList
        notifyDataSetChanged()
    }

    inner class CheckViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(checkItem: CheckItem) {
            view.apply {
                category_name_text.text = checkItem.categoryName
                final_price_text.text = checkItem.finalPrice.toRubles()
                date_text.text = checkItem.date
                val icon = CategoryIconPicker.getIconByName(checkItem.categoryName)
                check_icon.setImageResource(icon)
                setOnClickListener {
                    onItemClickListener?.invoke(checkItem)
                }
            }
        }
    }

}
