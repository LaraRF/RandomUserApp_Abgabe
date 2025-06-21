package com.srh.randomuserapp.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.srh.randomuserapp.R
import com.srh.randomuserapp.databinding.FragmentCameraBinding
import com.srh.randomuserapp.ui.viewmodels.UserListViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Fragment implementing camera functionality with AR overlay for user detection.
 * This demonstrates advanced camera features and potential AR integration.
 */
@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserListViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var cameraExecutor: ExecutorService

    // Camera permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupUI()
        checkCameraPermission()

        setHasOptionsMenu(true)
    }

    private fun setupUI() {
        // Camera capture button
        binding.buttonCapture.setOnClickListener {
            capturePhoto()
        }

        // Switch camera button (front/back)
        binding.buttonSwitchCamera.setOnClickListener {
            switchCamera()
        }

        // Toggle flash button
        binding.buttonFlash.setOnClickListener {
            toggleFlash()
        }

        // AR overlay toggle
        binding.buttonArOverlay.setOnClickListener {
            toggleArOverlay()
        }

        // Back button
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Initial UI state
        updateUIState()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(
                    context,
                    "Camera access is needed for AR user detection",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                showError("Failed to start camera")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    // Here you could implement AR user detection logic
                    // For now, we'll just process frames for demonstration
                    processImageForAR(imageProxy)
                    imageProxy.close()
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )

            updateUIState()
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            showError("Failed to bind camera use cases")
        }
    }

    private fun processImageForAR(imageProxy: ImageProxy) {
        // Placeholder for AR processing logic
        // In a real implementation, you might:
        // 1. Detect faces in the image
        // 2. Match faces with user database
        // 3. Overlay user information on detected faces
        // 4. Update AR overlay in real-time

        // For demonstration, we'll just simulate processing
        if (binding.arOverlay.visibility == View.VISIBLE) {
            requireActivity().runOnUiThread {
                // Update AR overlay with mock data
                updateArOverlay()
            }
        }
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        // For simplicity, we'll just show a toast
        // In a real app, you'd save the photo and possibly analyze it
        Toast.makeText(context, "Photo captured! (Demo mode)", Toast.LENGTH_SHORT).show()

        // Animate capture button
        binding.buttonCapture.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                binding.buttonCapture.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun switchCamera() {
        // This is a simplified implementation
        // In a real app, you'd switch between front and back cameras
        Toast.makeText(context, "Camera switch (Demo mode)", Toast.LENGTH_SHORT).show()
    }

    private fun toggleFlash() {
        camera?.let { camera ->
            val flashMode = camera.cameraInfo.torchState.value
            camera.cameraControl.enableTorch(flashMode == TorchState.OFF)
            updateUIState()
        }
    }

    private fun toggleArOverlay() {
        val isVisible = binding.arOverlay.visibility == View.VISIBLE
        binding.arOverlay.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.buttonArOverlay.text = if (isVisible) "Show AR" else "Hide AR"

        if (!isVisible) {
            updateArOverlay()
        }
    }

    private fun updateArOverlay() {
        // Simulate AR overlay with user data
        viewModel.users.value?.let { users ->
            if (users.isNotEmpty()) {
                val randomUser = users.random()
                binding.arUserName.text = "${randomUser.firstName} ${randomUser.lastName}"
                binding.arUserEmail.text = randomUser.email
                binding.arUserLocation.text = "${randomUser.city}, ${randomUser.country}"
            }
        }
    }

    private fun updateUIState() {
        camera?.let { camera ->
            val torchState = camera.cameraInfo.torchState.value
            binding.buttonFlash.text = when (torchState) {
                TorchState.ON -> "Flash: ON"
                else -> "Flash: OFF"
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_camera, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_camera_settings -> {
                Toast.makeText(context, "Camera settings (Demo)", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}