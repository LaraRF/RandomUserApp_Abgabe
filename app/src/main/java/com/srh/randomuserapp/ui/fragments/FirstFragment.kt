package com.srh.randomuserapp.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.srh.randomuserapp.R
import com.srh.randomuserapp.databinding.FragmentFirstBinding
import com.srh.randomuserapp.ui.adapters.UserAdapter
import com.srh.randomuserapp.ui.viewmodels.UserListViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment displaying the main user list overview.
 * This is the primary screen showing all users from the database.
 */
@AndroidEntryPoint
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserListViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupFab()
        setHasOptionsMenu(true)
    }

    /**
     * Initialize the RecyclerView with user adapter
     */
    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user ->
            // Navigate to user details when item is clicked
            try {
                val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(user.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                // Fallback: Create bundle manually if directions don't work yet
                val bundle = Bundle().apply {
                    putString("userId", user.id)
                }
                findNavController().navigate(R.id.SecondFragment, bundle)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    /**
     * Setup observers for ViewModel data
     */
    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner, Observer { users ->
            userAdapter.submitList(users)

            // Show/hide empty state
            if (users.isEmpty()) {
                binding.textViewEmptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.textViewEmptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    /**
     * Setup floating action button for camera/AR functionality
     */
    private fun setupFab() {
        binding.fab.setOnClickListener {
            // Navigate to camera/AR screen
            try {
                findNavController().navigate(R.id.action_FirstFragment_to_CameraFragment)
            } catch (e: Exception) {
                // Fallback if camera fragment doesn't exist yet
                findNavController().navigate(R.id.CameraFragment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        // Setup search functionality
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.setSearchQuery(newText ?: "")
                    return true
                }
            })

            setOnCloseListener {
                viewModel.clearSearch()
                false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_user -> {
                try {
                    findNavController().navigate(R.id.action_FirstFragment_to_AddUserFragment)
                } catch (e: Exception) {
                    // Fallback if AddUserFragment doesn't exist yet
                    Toast.makeText(context, "Add user feature coming soon", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_settings -> {
                try {
                    findNavController().navigate(R.id.action_FirstFragment_to_SettingsFragment)
                } catch (e: Exception) {
                    // Fallback if settings fragment doesn't exist yet
                    //findNavController().navigate(R.id.SettingsFragment)
                }
                true
            }
            R.id.action_sort_name -> {
                viewModel.setSortOrder(UserListViewModel.SortOrder.NAME)
                true
            }
            R.id.action_sort_date -> {
                viewModel.setSortOrder(UserListViewModel.SortOrder.DATE_CREATED)
                true
            }
            R.id.action_sort_date_of_birth -> {
                viewModel.setSortOrder(UserListViewModel.SortOrder.DATE_OF_BIRTH)
                true
            }
            R.id.action_refresh -> {
                viewModel.fetchRandomUsers()
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