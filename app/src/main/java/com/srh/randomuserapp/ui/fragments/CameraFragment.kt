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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.srh.randomuserapp.R
import com.srh.randomuserapp.data.models.User
import com.srh.randomuserapp.databinding.FragmentCameraBinding
import com.srh.randomuserapp.ui.viewmodels.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Fragment implementing camera functionality with AR overlay for user detection.
 * This demonstrates advanced camera features and QR code scanning for AR integration.
 */
@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentScannedUser: User? = null
    private var isArVisible = false

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
            toggleArVisibility()
        }

        // Back button
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Initial UI state
        updateUIState()
        hideUserOverlay() // Initial state: no overlay
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

        // QR Code Analysis
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        setupQRCodeAnalyzer()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalysis
            )

            updateUIState()
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            showError("Failed to bind camera use cases")
        }
    }

    /**
     * Setup QR code analyzer
     */
    private fun setupQRCodeAnalyzer() {
        val qrCodeAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcode.rawValue?.let { qrCodeData ->
                                // QR-Code gefunden - User aus Datenbank laden
                                loadUserFromQRCode(qrCodeData)
                            }
                        }

                        // Wenn keine QR-Codes gefunden wurden, User zurücksetzen
                        if (barcodes.isEmpty()) {
                            currentScannedUser = null
                            hideUserOverlay()
                        }
                    }
                    .addOnFailureListener {
                        // QR-Code Scanning fehlgeschlagen
                        currentScannedUser = null
                        hideUserOverlay()
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }

        imageAnalysis?.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), qrCodeAnalyzer)
    }

    /**
     * Lade User-Daten basierend auf QR-Code
     */
    private fun loadUserFromQRCode(qrCodeData: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = viewModel.getUserByQrCode(qrCodeData)
                if (user != null && user != currentScannedUser) {
                    currentScannedUser = user
                    showUserOverlay(user)
                } else if (user == null && currentScannedUser != null) {
                    currentScannedUser = null
                    hideUserOverlay()
                }
            } catch (e: Exception) {
                currentScannedUser = null
                hideUserOverlay()
            }
        }
    }

    /**
     * Zeige User-Overlay mit echten Daten
     */
    private fun showUserOverlay(user: User) {
        if (!isArVisible) return

        binding.apply {
            // User-Daten setzen (angepasst an bestehende Layout-Namen)
            arUserName.text = user.fullName
            arUserEmail.text = user.email
            arUserLocation.text = user.fullAddress

            // Overlay anzeigen
            arOverlay.isVisible = true

            // Click-Listener für Navigation zum User-Detail
            arOverlay.setOnClickListener {
                navigateToUserDetail(user)
            }
        }
    }

    /**
     * Verstecke User-Overlay
     */
    private fun hideUserOverlay() {
        binding.arOverlay.isVisible = false
        binding.arOverlay.setOnClickListener(null)
    }

    /**
     * Navigiere zum User-Detail
     */
    private fun navigateToUserDetail(user: User) {
        try {
            val action = CameraFragmentDirections.actionCameraFragmentToSecondFragment(user.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open user details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        // For simplicity, it just shows a toast
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
        // This is a simplified implementation -> switch not implemented
        Toast.makeText(context, "Camera switch (Demo mode)", Toast.LENGTH_SHORT).show()
    }

    private fun toggleFlash() {
        camera?.let { camera ->
            val flashMode = camera.cameraInfo.torchState.value
            camera.cameraControl.enableTorch(flashMode == TorchState.OFF)
            updateUIState()
        }
    }

    /**
     * Toggle AR visibility
     */
    private fun toggleArVisibility() {
        isArVisible = !isArVisible
        binding.apply {
            buttonArOverlay.text = if (isArVisible) "Hide AR" else "Show AR"

            // Wenn AR versteckt wird, auch das Overlay verstecken
            if (!isArVisible) {
                hideUserOverlay()
            } else if (currentScannedUser != null) {
                // Wenn AR wieder angezeigt wird und ein User gescannt ist, Overlay anzeigen
                showUserOverlay(currentScannedUser!!)
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