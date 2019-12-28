package ru.spbstu.gusev.checkpoint.ui.checklist

import android.os.Bundle
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.check_details_fragment.*
import ru.spbstu.gusev.checkpoint.App
import ru.spbstu.gusev.checkpoint.R
import ru.spbstu.gusev.checkpoint.extensions.showImageViewer
import ru.spbstu.gusev.checkpoint.extensions.toRubles
import ru.spbstu.gusev.checkpoint.extensions.toUri
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.ProductType
import ru.spbstu.gusev.checkpoint.ui.adapter.ProductAdapter
import ru.spbstu.gusev.checkpoint.ui.base.BaseFragment
import ru.spbstu.gusev.checkpoint.viewmodel.CheckListViewModel

class CheckDetailsFragment : BaseFragment() {
    private val productAdapter: ProductAdapter = ProductAdapter(ProductType.PRODUCT)

    override val layoutRes: Int
        get() = R.layout.check_details_fragment
    override val menuRes: Int?
        get() = R.menu.toolbar_menu_edit
    private lateinit var viewModel: CheckListViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(activity?.application as App).get(CheckListViewModel::class.java)

        setupRecycler()
        setCheckItemToView(viewModel.currentCheckItem)
        back_button.setOnClickListener {
            findNavController().popBackStack()
        }
        check_image.setOnClickListener {
            showImageViewer(
                viewModel.currentCheckItem.checkImagePath.toUri(),
                check_image
            )
        }
    }

    private fun setupRecycler() {
        val recyclerLayoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(
            products_recycler.context,
            recyclerLayoutManager.orientation
        )
        products_recycler.apply {
            adapter = productAdapter
            layoutManager = recyclerLayoutManager
            addItemDecoration(dividerItemDecoration)
        }
    }

    private fun setCheckItemToView(checkItem: CheckItem) {
        category_name_text.text = checkItem.categoryName
        final_price_text.text = checkItem.finalPrice.toRubles()
        date_text.text = checkItem.date
        shop_text.text = checkItem.shop
        productAdapter.updateProducts(checkItem.products)
        Glide.with(requireContext()).load(viewModel.currentCheckItem.checkImagePath.toUri()).into(check_image)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_action -> {
                val bundle = bundleOf("is_edit_mode" to "true")
                findNavController().navigate(R.id.editCheckFragment, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}