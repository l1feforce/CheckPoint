package ru.spbstu.gusev.checkpoint.ui.checklist


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.annotation.ColorInt
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.check_list_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.spbstu.gusev.checkpoint.App
import ru.spbstu.gusev.checkpoint.R
import ru.spbstu.gusev.checkpoint.extensions.getBitmapByPath
import ru.spbstu.gusev.checkpoint.extensions.snackbar
import ru.spbstu.gusev.checkpoint.extensions.toUri
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.RecognitionRepository
import ru.spbstu.gusev.checkpoint.ui.adapter.CheckAdapter
import ru.spbstu.gusev.checkpoint.ui.base.BaseFragment
import ru.spbstu.gusev.checkpoint.viewmodel.CheckListViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CheckListFragment : BaseFragment() {

    private lateinit var checkAdapter: CheckAdapter
    private lateinit var viewModel: CheckListViewModel
    private val REQUEST_CAMERA = 1488
    private val RC_SIGN_IN = 1337
    private var currentPhotoPath = ""

    override val layoutRes: Int
        get() = R.layout.check_list_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu_auth, menu)
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true)
        @ColorInt val color = typedValue.data
        menu.findItem(R.id.auth_action).icon.setColorFilter(
            color, PorterDuff.Mode.SRC_ATOP
        )
        super.onCreateOptionsMenu(menu, inflater)
    }

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

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                try {
                    showProgress()
                    GlobalScope.launch(Dispatchers.IO) {
                        val bitmap = requireContext().getBitmapByPath(currentPhotoPath)
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        Log.v("tag", "photo path check list frgm: $currentPhotoPath")
                        withContext(Dispatchers.Main) {
                            if (bitmap.height > 32) {
                                recognizePhoto(currentPhotoPath)
                            } else hideProgress()
                        }
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

    private fun recognizePhoto(imagePath: String) {
        val onSuccess = {
            viewModel.currentCheckItem = RecognitionRepository.newCheck
            hideProgress()
            findNavController().navigate(R.id.editCheckFragment)
        }
        val onFailure: (Exception) -> (Unit) = {
            val check = CheckItem.createDefault()
            check.checkImagePath = imagePath
            viewModel.currentCheckItem = check
            hideProgress()
            requireView().snackbar(it.message.toString())
        }
        viewModel.currentCheckItem =
            RecognitionRepository.recognize(imagePath, requireContext(), onSuccess, onFailure)
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
                checkList?.let {
                    checkAdapter.updateChecks(it)
                    it.forEach {
                        //create files if not exists
                        if (it.checkImagePath.toUri().toString() == "") {
                            val file = createImageFile()
                            it.checkImagePath = file.absolutePath
                        }
                    }
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.auth_action -> {
                // Choose authentication providers
                val providers = arrayListOf(
                    AuthUI.IdpConfig.PhoneBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )
                if (FirebaseAuth.getInstance().currentUser == null) {
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.logo_checkpoint)
                            .setTheme(R.style.AppTheme)
                            .build(),
                        RC_SIGN_IN
                    )
                } else {
                    //show user account page
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }
}
