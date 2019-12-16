package ru.spbstu.gusev.checkpoint.ui.checklist


import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.check_list_fragment.*
import ru.spbstu.gusev.checkpoint.App
import ru.spbstu.gusev.checkpoint.R
import ru.spbstu.gusev.checkpoint.extensions.getBitmapByPath
import ru.spbstu.gusev.checkpoint.extensions.snackbar
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.ui.base.BaseFragment
import ru.spbstu.gusev.checkpoint.ui.checklist.adapter.CheckAdapter
import ru.spbstu.gusev.checkpoint.viewmodel.CheckListViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CheckListFragment : BaseFragment() {

    private lateinit var checkAdapter: CheckAdapter
    private val REQUEST_CAMERA = 1488
    private var currentPhotoPath = ""
    private lateinit var viewModel: CheckListViewModel

    override val layoutRes: Int
        get() = R.layout.check_list_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(activity?.application as App).get(CheckListViewModel::class.java)

        setupRecycler()
        getData()


        add_photo_fab.setOnClickListener {
            takePicture()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                try {
                    val bitmap = requireContext().getBitmapByPath(currentPhotoPath)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val bundle = bundleOf("image" to currentPhotoPath)
                    Log.v("tag", "photo path check list frgm: $currentPhotoPath")
                    if (bitmap.height > 32) {
                        findNavController().navigate(R.id.editCheckFragment, bundle)
                    }
                } catch (e: Exception) {
                    requireView().snackbar(e.message.toString())
                }
            }
        }
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            val photoFile: File? = createImageFile()
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "ru.spbstu.gusev.checkpoint.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_CAMERA)
            }
        }

    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun setupRecycler() {
        checkAdapter = CheckAdapter()
        val recyclerLayoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(
            check_list_recycler.context,
            recyclerLayoutManager.orientation
        )
        check_list_recycler.apply {
            adapter = checkAdapter
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            addItemDecoration(dividerItemDecoration)
        }
        checkAdapter.onItemClickListener = {
            openCheckDetails(it)
        }
    }

    private fun openCheckDetails(checkItem: CheckItem) {
        viewModel.setCurrentCheckView(checkItem)
        findNavController().navigate(R.id.checkDetailsFragment)
    }

    private fun getData() {
        viewModel.allChecks.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { checkList ->
                checkList?.let { checkAdapter.updateChecks(it) }
            })
    }
}
