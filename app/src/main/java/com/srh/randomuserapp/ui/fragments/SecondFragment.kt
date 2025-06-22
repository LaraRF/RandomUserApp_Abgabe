package com.srh.randomuserapp.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.srh.randomuserapp.R
import com.srh.randomuserapp.databinding.FragmentSecondBinding
import com.srh.randomuserapp.ui.viewmodels.UserDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

/**
 * Fragment displaying detailed information about a specific user.
 * Shows user profile, contact details, and additional information.
 */
@AndroidEntryPoint
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val args: SecondFragmentArgs by navArgs()
    private val viewModel: UserDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        loadUserDetails()
        setHasOptionsMenu(true)
    }

    /**
     * Load user details using the provided user ID
     */
    private fun loadUserDetails() {
        viewModel.loadUser(args.userId)
    }

    /**
     * Setup observers for ViewModel data
     */
    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                bindUserData(it)
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                // TODO: Show error message to user (Snackbar or Dialog)
                binding.textViewError.text = it
                binding.textViewError.visibility = View.VISIBLE
            }
        })

        viewModel.qrCodeBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            bitmap?.let {
                showQrCodeDialog(it)
            }
        })
    }

    /**
     * Show QR code in a dialog
     */
    private fun showQrCodeDialog(bitmap: Bitmap) {
        val imageView = ImageView(requireContext()).apply {
            setImageBitmap(bitmap)
            layoutParams = ViewGroup.LayoutParams(
                (300 * resources.displayMetrics.density).toInt(),
                (300 * resources.displayMetrics.density).toInt()
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("QR Code")
            .setView(imageView)
            .setPositiveButton("Close", null)
            .setNeutralButton("Share") { _, _ ->
                shareQrCode(bitmap)
            }
            .show()
    }

    /**
     * Share QR code bitmap
     */
    private fun shareQrCode(bitmap: Bitmap) {
        try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "qr_code.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            val contentUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share QR code", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Bind user data to the UI components
     */
    private fun bindUserData(user: com.srh.randomuserapp.data.models.User) {
        binding.apply {
            // Hide error message
            textViewError.visibility = View.GONE

            // User basic info
            textViewName.text = user.fullName
            textViewEmail.text = user.email
            textViewPhone.text = user.phone
            textViewDateOfBirth.text = user.dateOfBirth

            // Additional info
            textViewGender.text = user.gender
            textViewAddress.text = user.fullAddress

            // Manual creation indicator
            if (user.isManuallyCreated) {
                chipManualUser.visibility = View.VISIBLE
            } else {
                chipManualUser.visibility = View.GONE
            }

            // Load profile image
            Glide.with(requireContext())
                .load(user.profilePictureUrl)
                .placeholder(R.drawable.ic_person_placeholder_24)
                .error(R.drawable.ic_person_placeholder_24)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageViewProfile)

            // QR Code section
            textViewQrCode.text = user.qrCode
            buttonShowQrCode.setOnClickListener {
                viewModel.generateQrCodeBitmap(user.id)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_user_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                // TODO: Navigate to edit screen
                true
            }
            R.id.action_delete -> {
                viewModel.deleteUser()
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}