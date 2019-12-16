package ru.spbstu.gusev.checkpoint.ui.checklist

import android.os.Bundle
import android.view.View
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.new_check_fragment.*
import kotlinx.android.synthetic.main.new_check_layout.*
import kotlinx.android.synthetic.main.product_item_editable.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import ru.spbstu.gusev.checkpoint.App
import ru.spbstu.gusev.checkpoint.R
import ru.spbstu.gusev.checkpoint.extensions.snackbar
import ru.spbstu.gusev.checkpoint.extensions.toUri
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.ProductItem
import ru.spbstu.gusev.checkpoint.model.ProductType
import ru.spbstu.gusev.checkpoint.model.RecognitionRepository
import ru.spbstu.gusev.checkpoint.ui.base.BaseFragment
import ru.spbstu.gusev.checkpoint.ui.checklist.adapter.ProductAdapter
import ru.spbstu.gusev.checkpoint.viewmodel.CheckListViewModel


class EditCheckFragment : BaseFragment() {
    private lateinit var newCheck: CheckItem
    private lateinit var productAdapter: ProductAdapter
    private var isOldCheck = ""
    private var imagePath = ""

    override val layoutRes: Int
        get() = R.layout.new_check_fragment

    private lateinit var viewModel: CheckListViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(activity?.application as App).get(CheckListViewModel::class.java)

        setupRecycler()
        setErrorsCancellation()

        imagePath = arguments?.getString("image") ?: ""
        isOldCheck = arguments?.getString("is_old_check") ?: ""
        if (isOldCheck == "true") {
            stopShimming()
            setCheckItemToView(viewModel.currentCheckItem)
            newCheck = viewModel.currentCheckItem
        } else {
            recognizePhoto(imagePath)
        }

        complete_button.setOnClickListener {
            if (!setErrors()) {
                setViewToCheckItem()
                val databaseAccess = if (isOldCheck == "true") {
                    GlobalScope.async { viewModel.updateCheck(newCheck) }
                } else {
                    GlobalScope.async {
                        viewModel.addCheck(newCheck)
                    }
                }
                databaseAccess.invokeOnCompletion { openCheckDetails(newCheck) }
            }
        }

        add_button.setOnClickListener {
            setViewToCheckItem()
            productAdapter.updateProducts(newCheck.products)
            productAdapter.addProducts(listOf(ProductItem("", 0f)))
        }
    }

    private fun setErrorsCancellation() {
        val inputs = mutableListOf<TextInputLayout>()
        new_check_constraint.forEach {
            if (it is TextInputLayout) {
                inputs.add(it)
                it.editText?.onFocusChangeListener = object : View.OnFocusChangeListener {
                    override fun onFocusChange(v: View?, hasFocus: Boolean) {
                        inputs.forEach { editText -> editText.isErrorEnabled = false }
                    }
                }
            }
        }
    }

    private fun setErrors(): Boolean {
        var isError = false
        new_check_constraint.forEach {
            if (it is TextInputLayout) {
                if (it.editText?.text.toString().isBlank()) {
                    it.error = getString(R.string.field_is_empty_error)
                    isError = true
                }
            }
        }
        return isError
    }

    private fun recognizePhoto(imagePath: String) {
        shimmer_layout.startShimmerAnimation()
        val onSuccess = {
            stopShimming()
            setCheckItemToView(RecognitionRepository.newCheck)
        }
        val onFailure: (Exception) -> (Unit) = {
            stopShimming()
            val check = CheckItem.createDefault()
            check.checkImagePath = imagePath
            setCheckItemToView(check)
            requireView().snackbar(it.message.toString())
        }
        newCheck =
            RecognitionRepository.recognize(imagePath, requireContext(), onSuccess, onFailure)
    }

    private fun stopShimming() {
        shimmer_layout.stopShimmerAnimation()
        new_check_include.visibility = View.VISIBLE
        shimmer_layout.visibility = View.GONE
    }

    private fun openCheckDetails(checkItem: CheckItem) {
        viewModel.setCurrentCheckView(checkItem)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.checkListFragment, false)
            .build()
        findNavController().navigate(R.id.checkDetailsFragment, arguments, navOptions)
    }

    private fun setCheckItemToView(checkItem: CheckItem) {
        category_name_text.setText(checkItem.categoryName)
        final_price_text.setText(checkItem.finalPrice.toString())
        date_text.setText(checkItem.date)
        shop_text.setText(checkItem.shop)
        productAdapter.updateProducts(checkItem.products)
        Glide.with(requireContext()).load(checkItem.checkImagePath.toUri()).into(check_image)
    }

    private fun setViewToCheckItem() {
        newCheck.apply {
            categoryName = category_name_text.text.toString()
            finalPrice = final_price_text.text.toString().toFloat()
            date = date_text.text.toString()
            shop = shop_text.text.toString()
            val productsList = mutableListOf<ProductItem>()
            val viewHoldersAmount = products_recycler.childCount
            for (i in 0 until viewHoldersAmount) {
                val currentViewHolder = products_recycler.getChildAt(i)
                productsList.add(
                    ProductItem(
                        currentViewHolder.product_name_text.text.toString(),
                        currentViewHolder.product_price_text.text.toString().toFloat()
                    )
                )
            }
            products = productsList
        }

    }

    private fun setupRecycler() {
        productAdapter = ProductAdapter(ProductType.EDITABLE_PRODUCT)
        val recyclerLayoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(
            products_recycler.context,
            recyclerLayoutManager.orientation
        )
        products_recycler.apply {
            adapter = productAdapter
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            addItemDecoration(dividerItemDecoration)
        }
    }
}
