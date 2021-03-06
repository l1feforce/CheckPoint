package ru.spbstu.gusev.checkpoint.ui.checklist


import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.check_list_fragment.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.spbstu.gusev.checkpoint.App
import ru.spbstu.gusev.checkpoint.R
import ru.spbstu.gusev.checkpoint.extensions.getBitmapByPath
import ru.spbstu.gusev.checkpoint.extensions.getColorFromTheme
import ru.spbstu.gusev.checkpoint.extensions.snackbar
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.RecognitionRepository
import ru.spbstu.gusev.checkpoint.ui.adapter.CheckAdapter
import ru.spbstu.gusev.checkpoint.ui.base.BaseFragment
import ru.spbstu.gusev.checkpoint.viewmodel.CheckListViewModel
import team.clevel.documentscanner.ImageCropActivity
import team.clevel.documentscanner.helpers.ScannerConstants
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class CheckListFragment : BaseFragment() {

    private lateinit var checkAdapter: CheckAdapter
    private lateinit var viewModel: CheckListViewModel
    private val REQUEST_CAMERA = 1488
    private val RC_SIGN_IN = 1337
    private val REQUEST_CROP = 1228
    private var currentPhotoPath = ""

    override val layoutRes: Int
        get() = R.layout.check_list_fragment
    override val menuRes: Int?
        get() = R.menu.toolbar_menu_edit

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(activity?.application as App).get(CheckListViewModel::class.java)

        setupRecycler()
        getData()
        setupCropView()

        viewModel.refreshData()
        viewModel.isLoading.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) showProgress() else hideProgress()
        })

        viewModel.errorText.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                requireView().snackbar(it)
            }
        })

        FirebaseAuth.getInstance().addAuthStateListener {
            viewModel.refreshData()
            if (it.currentUser == null) {
                showNoUser()
                requireActivity().nav_view.getHeaderView(0).header_logo_image.setOnClickListener {
                    startAuth()
                }
                viewModel.cleanData()
            } else {
                showNewUser(
                    it.currentUser?.displayName ?: it.currentUser?.email
                    ?: it.currentUser?.phoneNumber ?: getString(R.string.user_name_example)
                )
                requireActivity().nav_view.getHeaderView(0).sign_out_button.setOnClickListener {
                    FirebaseAuth.getInstance().signOut()
                }
            }
        }

        add_photo_fab.setOnClickListener {
            takePicture()
        }
    }

    private fun setupCropView() {
        ScannerConstants.backColor = "#F0F0F0"
        ScannerConstants.cropText = resources.getString(R.string.crop_text)
        ScannerConstants.backText = resources.getString(R.string.back_text)
        ScannerConstants.cropError = resources.getString(R.string.crop_error)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == REQUEST_CAMERA -> {
                try {
                    viewModel.isLoading.value = true
                    GlobalScope.launch(Dispatchers.IO) {
                        val bitmap = requireContext().getBitmapByPath(currentPhotoPath)
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        withContext(Dispatchers.Main) {
                            if (bitmap.height > 32) {
                                ScannerConstants.selectedImageBitmap = bitmap
                                File(currentPhotoPath).delete()
                                startActivityForResult(
                                    Intent(
                                        requireContext(),
                                        ImageCropActivity::class.java
                                    ), REQUEST_CROP
                                )
                            } else viewModel.isLoading.value = false
                        }
                    }
                } catch (e: Exception) {
                    viewModel.isLoading.value = false
                    requireView().snackbar(e.message.toString())
                }
            }
            requestCode == REQUEST_CROP && resultCode == Activity.RESULT_OK -> {
                createImageFile().also {
                    try {
                        val out = FileOutputStream(it);
                        ScannerConstants.selectedImageBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            out
                        )
                        out.flush()
                        out.close()
                    } catch (e: Exception) {
                        viewModel.isLoading.value = false
                        requireView().snackbar(e.message.toString())
                    }
                }
                recognizePhoto(currentPhotoPath)
            }
            requestCode == REQUEST_CROP -> viewModel.isLoading.value = false
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
            viewModel.isLoading.value = false
            findNavController().navigate(R.id.editCheckFragment)
        }
        val onFailure: (Exception) -> (Unit) = {
            val check = CheckItem.createDefault()
            check.checkImagePath = imagePath
            viewModel.currentCheckItem = check
            viewModel.isLoading.value = false
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
                viewModel.isLoading.value = false
                checkList?.let {
                    checkAdapter.updateChecks(it)
                    if (it.isNotEmpty()) {
                        showContent()
                    } else {
                        showEmptyPlaceholder()
                    }
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_action -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startAuth() {
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
        }
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    private fun showEmptyPlaceholder() {
        check_list_recycler.visibility = View.GONE
        empty_checklist_include.visibility = View.VISIBLE
    }

    private fun showContent() {
        check_list_recycler.visibility = View.VISIBLE
        empty_checklist_include.visibility = View.GONE
    }

    private fun showNewUser(userName: String) {
        with(requireActivity().nav_view.getHeaderView(0)) {
            this.header_logo_image.setImageResource(R.mipmap.ic_launcher_round)
            this.header_logo_image.imageTintList = null
            this.sign_out_button.visibility = View.VISIBLE
            this.user_name_text.visibility = View.VISIBLE
            this.user_name_text.text = userName
            this.log_in_title_text.visibility = View.GONE
            this.log_in_description_text.visibility = View.GONE
        }
    }

    private fun showNoUser() {
        with(requireActivity().nav_view.getHeaderView(0)) {
            this.header_logo_image.setImageResource(R.drawable.ic_log_in)
            this.header_logo_image.imageTintList =
                ColorStateList.valueOf(requireContext().getColorFromTheme(R.attr.colorOnSecondary))
            this.sign_out_button.visibility = View.GONE
            this.user_name_text.visibility = View.GONE
            this.log_in_title_text.visibility = View.VISIBLE
            this.log_in_description_text.visibility = View.VISIBLE
        }
    }
}
