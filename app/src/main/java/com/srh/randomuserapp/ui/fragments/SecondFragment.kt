package com.srh.randomuserapp.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.srh.randomuserapp.R
import com.srh.randomuserapp.databinding.FragmentSecondBinding
import com.srh.randomuserapp.ui.viewmodels.UserDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

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