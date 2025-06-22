package com.srh.randomuserapp.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.srh.randomuserapp.R
import com.srh.randomuserapp.databinding.FragmentAddUserBinding
import com.srh.randomuserapp.ui.viewmodels.AddUserViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for manually adding new users to the database.
 */
@AndroidEntryPoint
class AddUserFragment : Fragment() {

    private var _binding: FragmentAddUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddUserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        setHasOptionsMenu(true)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            setFormEnabled(!isLoading)
        })

        viewModel.message.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        })

        viewModel.userCreated.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                findNavController().navigateUp()
            }
        })
    }

    private fun setupClickListeners() {
        binding.buttonSaveUser.setOnClickListener {
            createUser()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun createUser() {
        val firstName = binding.editTextFirstName.text.toString().trim()
        val lastName = binding.editTextLastName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val dateOfBirth = binding.editTextDateOfBirth.text.toString().trim()
        val gender = when (binding.radioGroupGender.checkedRadioButtonId) {
            R.id.radioButtonMale -> "Male"
            R.id.radioButtonFemale -> "Female"
            else -> ""
        }
        val country = binding.editTextCountry.text.toString().trim()
        val city = binding.editTextCity.text.toString().trim()
        val street = binding.editTextStreet.text.toString().trim()

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(context, "Please fill in required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createUser(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            dateOfBirth = dateOfBirth,
            gender = gender,
            country = country,
            city = city,
            street = street
        )
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.editTextFirstName.isEnabled = enabled
        binding.editTextLastName.isEnabled = enabled
        binding.editTextEmail.isEnabled = enabled
        binding.editTextPhone.isEnabled = enabled
        binding.editTextDateOfBirth.isEnabled = enabled
        binding.editTextCountry.isEnabled = enabled
        binding.editTextCity.isEnabled = enabled
        binding.editTextStreet.isEnabled = enabled
        binding.radioGroupGender.isEnabled = enabled
        binding.buttonSaveUser.isEnabled = enabled
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_user, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_form -> {
                clearForm()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearForm() {
        binding.editTextFirstName.text?.clear()
        binding.editTextLastName.text?.clear()
        binding.editTextEmail.text?.clear()
        binding.editTextPhone.text?.clear()
        binding.editTextDateOfBirth.text?.clear()
        binding.editTextCountry.text?.clear()
        binding.editTextCity.text?.clear()
        binding.editTextStreet.text?.clear()
        binding.radioGroupGender.clearCheck()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}