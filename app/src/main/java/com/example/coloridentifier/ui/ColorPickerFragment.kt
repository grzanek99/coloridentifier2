package com.example.coloridentifier.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.coloridentifier.R
import com.example.coloridentifier.databinding.FragmentColorPickerBinding
import com.example.coloridentifier.model.Color
import com.example.coloridentifier.utils.ColorNameMapper
import com.example.coloridentifier.utils.ColorUtils
import com.example.coloridentifier.utils.ImageUtils
import com.example.coloridentifier.viewmodel.ColorViewModel
import java.io.File

class ColorPickerFragment : Fragment() {

    private var _binding: FragmentColorPickerBinding? = null
    private val binding get() = _binding!!
    private val colorViewModel: ColorViewModel by activityViewModels()
    
    private var currentBitmap: Bitmap? = null
    private var selectedColor: Color? = null
    private var imageCapture: ImageCapture? = null

    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadImage(it) }
    }

    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.camera_permission_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Storage permission launcher  
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.storage_permission_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.chooseImageButton.setOnClickListener {
            checkStoragePermissionAndPick()
        }

        binding.takePhotoButton.setOnClickListener {
            checkCameraPermission()
        }

        binding.btnCapturePhoto.setOnClickListener {
            takePhoto()
        }

        binding.imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && currentBitmap != null) {
                // Pokaż znacznik w miejscu dotyku
                val markerSize = 20 * resources.displayMetrics.density
                binding.touchMarker.x = event.x - (markerSize / 2)
                binding.touchMarker.y = event.y - (markerSize / 2)
                binding.touchMarker.visibility = View.VISIBLE
                
                identifyColorAtPosition(event.x.toInt(), event.y.toInt())
                return@setOnTouchListener true
            }
            false
        }

        binding.saveColorButton.setOnClickListener {
            selectedColor?.let { color ->
                if (colorViewModel.saveColor(color)) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.color_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.color_already_exists),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.copyAllButton.setOnClickListener {
            selectedColor?.let { color ->
                val text = """
                    Name: ${color.name}
                    RGB: ${color.getRGBString()}
                    HEX: ${color.hexValue}
                """.trimIndent()
                
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Color Info", text)
                clipboard.setPrimaryClip(clip)
                
                Toast.makeText(requireContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkStoragePermissionAndPick() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }
            else -> {
                storagePermissionLauncher.launch(permission)
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
            
            imageCapture = ImageCapture.Builder().build()
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
                binding.previewView.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.btnCapturePhoto.visibility = View.VISIBLE
                binding.touchMarker.visibility = View.GONE
                binding.colorInfoCard.visibility = View.GONE
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error starting camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        val photoFile = File(
            requireContext().cacheDir,
            "color_photo_${System.currentTimeMillis()}.jpg"
        )
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    loadImageToBitmap(bitmap)
                    
                    binding.previewView.visibility = View.GONE
                    binding.btnCapturePhoto.visibility = View.GONE
                    binding.imageView.visibility = View.VISIBLE
                    binding.instructionText.visibility = View.VISIBLE
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun loadImageToBitmap(bitmap: Bitmap) {
        currentBitmap = bitmap
        binding.imageView.setImageBitmap(bitmap)
        binding.imageView.visibility = View.VISIBLE
        binding.instructionText.visibility = View.VISIBLE
        binding.colorInfoCard.visibility = View.GONE
    }

    private fun loadImage(uri: Uri) {
        val bitmap = ImageUtils.loadBitmapFromUri(requireContext(), uri)
        if (bitmap != null) {
            loadImageToBitmap(bitmap)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_loading_image),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun identifyColorAtPosition(x: Int, y: Int) {
        val bitmap = currentBitmap ?: return

        // Convert touch coordinates to bitmap coordinates
        val imageView = binding.imageView
        val imageMatrix = imageView.imageMatrix
        val values = FloatArray(9)
        imageMatrix.getValues(values)

        val scaleX = values[0]
        val scaleY = values[4]
        val transX = values[2]
        val transY = values[5]

        val bitmapX = ((x - transX) / scaleX).toInt()
        val bitmapY = ((y - transY) / scaleY).toInt()

        // Get color from bitmap
        val colorInt = ColorUtils.getPixelColor(bitmap, bitmapX, bitmapY)
        
        if (colorInt != android.graphics.Color.TRANSPARENT) {
            val red = android.graphics.Color.red(colorInt)
            val green = android.graphics.Color.green(colorInt)
            val blue = android.graphics.Color.blue(colorInt)
            val hex = ColorUtils.rgbToHex(red, green, blue)
            val colorName = ColorNameMapper.getColorName(red, green, blue)

            val color = Color(
                name = colorName,
                red = red,
                green = green,
                blue = blue,
                hexValue = hex
            )

            displayColorInfo(color)
        }
    }

    private fun displayColorInfo(color: Color) {
        selectedColor = color
        
        binding.colorPreview.setBackgroundColor(color.toColorInt())
        binding.colorName.text = color.name
        binding.colorHex.text = color.hexValue
        binding.colorRgb.text = color.getRGBString()
        
        binding.colorInfoCard.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentBitmap?.recycle()
        currentBitmap = null
    }
}
