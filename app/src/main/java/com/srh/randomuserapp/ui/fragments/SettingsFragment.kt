package com.srh.randomuserapp.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.srh.randomuserapp.R
import com.srh.randomuserapp.databinding.FragmentSettingsBinding
import com.srh.randomuserapp.ui.viewmodels.SettingsViewModel
import com.srh.randomuserapp.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for app settings and configuration.
 * Provides options for database management, app preferences, and user settings.
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        loadCurrentSettings()
        setHasOptionsMenu(true)
    }

    /**
     * Setup observers for ViewModel data
     */
    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            setButtonsEnabled(!isLoading)
        })

        viewModel.message.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        })

        viewModel.userStats.observe(viewLifecycleOwner, Observer { stats ->
            updateStatsDisplay(stats)
        })
    }

    /**
     * Setup click listeners for all buttons and switches
     */
    private fun setupClickListeners() {
        // Database Management
        binding.buttonFillDatabase.setOnClickListener {
            showFillDatabaseDialog()
        }

        binding.buttonClearAllUsers.setOnClickListener {
            showClearAllUsersDialog()
        }

        binding.buttonClearApiUsers.setOnClickListener {
            showClearApiUsersDialog()
        }

        binding.buttonClearManualUsers.setOnClickListener {
            showClearManualUsersDialog()
        }

        // Theme Settings
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkModeEnabled(isChecked)
            applyTheme(isChecked)
        }

        // Language Settings
        binding.buttonLanguageGerman.setOnClickListener {
            changeLanguage("de")
        }

        binding.buttonLanguageEnglish.setOnClickListener {
            changeLanguage("en")
        }

        // App Info
        binding.buttonAboutApp.setOnClickListener {
            showAboutDialog()
        }

        // GitHub Button
        binding.buttonViewOnGithub.setOnClickListener {
            openGithubRepository()
        }


        binding.buttonShareApp.setOnClickListener {
            shareApp()
        }

        // Data Export/Import
        binding.buttonExportData.setOnClickListener {
            viewModel.exportUserData()
        }

        binding.buttonImportData.setOnClickListener {
            Toast.makeText(context, "Import functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Load current app settings
     */
    private fun loadCurrentSettings() {
        viewModel.loadSettings()
        viewModel.loadUserStats()

        // Set current theme switch state
        binding.switchDarkMode.isChecked = viewModel.isDarkModeEnabled()

        // Update language button states
        updateLanguageButtons()
    }

    /**
     * Show dialog for filling database with random users
     */
    private fun showFillDatabaseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_fill_database, null)
        val editTextCount = dialogView.findViewById<EditText>(R.id.editTextUserCount)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Fill Database")
            .setMessage("How many random users would you like to add? (1-50)")
            .setView(dialogView)
            .setPositiveButton("Add Users") { _, _ ->
                val countText = editTextCount.text.toString()
                val count = countText.toIntOrNull()

                when {
                    count == null -> {
                        Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                    }
                    count < 1 || count > 50 -> {
                        Toast.makeText(context, "Please enter a number between 1 and 50", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        viewModel.fillDatabaseWithRandomUsers(count)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show confirmation dialog for clearing all users
     */
    private fun showClearAllUsersDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear All Users")
            .setMessage("This will permanently delete all users from the database. This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                viewModel.clearAllUsers()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show confirmation dialog for clearing API users
     */
    private fun showClearApiUsersDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear API Users")
            .setMessage("This will delete all users fetched from the Random User API.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.clearApiUsers()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show confirmation dialog for clearing manual users
     */
    private fun showClearManualUsersDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear Manual Users")
            .setMessage("This will delete all manually created users.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.clearManualUsers()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Apply theme change
     */
    private fun applyTheme(isDarkMode: Boolean) {
        val nightMode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    /**
     * Change app language
     */
    private fun changeLanguage(languageCode: String) {
        viewModel.setLanguage(languageCode)

        // Apply locale change
        val context = LocaleHelper.setLocale(requireContext(), languageCode)

        // Update activity with new locale
        requireActivity().finish()
        requireActivity().startActivity(requireActivity().intent)
        requireActivity().overridePendingTransition(0, 0)
    }

    /**
     * Update language button states
     */
    private fun updateLanguageButtons() {
        val currentLanguage = viewModel.getCurrentLanguage()
        binding.buttonLanguageGerman.isEnabled = currentLanguage != "de"
        binding.buttonLanguageEnglish.isEnabled = currentLanguage != "en"
    }

    /**
     * Update statistics display
     */
    private fun updateStatsDisplay(stats: SettingsViewModel.AppStats) {
        binding.apply {
            textViewTotalUsers.text = "Total Users: ${stats.totalUsers}"
            textViewApiUsers.text = "API Users: ${stats.apiUsers}"
            textViewManualUsers.text = "Manual Users: ${stats.manualUsers}"
            textViewDatabaseSize.text = "Database Size: ${stats.databaseSizeMB} MB"
        }
    }

    /**
     * Show about dialog
     */
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About RandomUserApp")
            .setMessage("""
                Version: 1.0.0
                
                This app demonstrates modern Android development practices including:
                • Random User API integration
                • Local SQLite database
                • Camera & AR functionality
                • Material Design 3
                
                Developed for SRH Hochschule Heidelberg
                VR, AR and Mobile Development Course
                
                © 2025 RandomUserApp - Lara Friedrich
            """.trimIndent())
            .setPositiveButton("OK", null)
            .setNeutralButton("View Source") { _, _ ->
                // Open GitHub
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LaraRF/RandomUserApp_Abgabe"))
                startActivity(intent)
            }
            .show()
    }

    /**
     * Open Play Store for rating - Now shows feature not available message
     */
    private fun openPlayStore() {
        Toast.makeText(context, "Feature currently not available", Toast.LENGTH_SHORT).show()
    }

    /**
     * Open GitHub repository
     */
    private fun openGithubRepository() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LaraRF/RandomUserApp_Abgabe"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open GitHub repository", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Share app with others
     */
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out RandomUserApp!")
            putExtra(Intent.EXTRA_TEXT, "I found this great app for managing random user data: https://play.google.com/store/apps/details?id=${requireContext().packageName}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    /**
     * Enable/disable buttons during loading
     */
    private fun setButtonsEnabled(enabled: Boolean) {
        binding.apply {
            buttonFillDatabase.isEnabled = enabled
            buttonClearAllUsers.isEnabled = enabled
            buttonClearApiUsers.isEnabled = enabled
            buttonClearManualUsers.isEnabled = enabled
            buttonExportData.isEnabled = enabled
            buttonImportData.isEnabled = enabled
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset_settings -> {
                showResetSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Show dialog for resetting all settings
     */
    private fun showResetSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset Settings")
            .setMessage("This will reset all app settings to their default values.")
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetAllSettings()
                loadCurrentSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}