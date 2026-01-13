package com.example.coloridentifier.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

class ColorPickerFragment : Fragment() {

    private var _binding: FragmentColorPickerBinding? = null
    private val binding get() = _binding!!
    private val colorViewModel: ColorViewModel by activityViewModels()
    
    private var currentBitmap: Bitmap? = null
    private var selectedColor: Color? = null

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
            // TODO: Open camera
            Toast.makeText(requireContext(), "Camera feature coming soon", Toast.LENGTH_SHORT).show()
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

        binding.imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && currentBitmap != null) {
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
                // TODO: Open camera
                Toast.makeText(requireContext(), "Camera feature coming soon", Toast.LENGTH_SHORT).show()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun loadImage(uri: Uri) {
        val bitmap = ImageUtils.loadBitmapFromUri(requireContext(), uri)
        if (bitmap != null) {
            currentBitmap = bitmap
            binding.imageView.setImageBitmap(bitmap)
            binding.imageView.visibility = View.VISIBLE
            binding.instructionText.visibility = View.VISIBLE
            binding.colorInfoCard.visibility = View.GONE
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
