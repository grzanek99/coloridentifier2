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

/**
 * fragment umozliwiajacy identyfikacje kolorow z obrazow
 */
class ColorPickerFragment : Fragment() {

    // nullable binding dla layoutu fragmentu
    private var _binding: FragmentColorPickerBinding? = null
    // non-null binding getter
    private val binding get() = _binding!!
    // viewmodel kolorow wspoldzielony z aktywnoscia
    private val colorViewModel: ColorViewModel by activityViewModels()
    
    // aktualnie zaladowany obraz
    private var currentBitmap: Bitmap? = null
    // aktualnie wybrany kolor
    private var selectedColor: Color? = null
    // obiekt do przechwytywania zdjec
    private var imageCapture: ImageCapture? = null

    // launcher do wybierania obrazow z galerii
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // laduje obraz gdy wybrano uri
        uri?.let { loadImage(it) }
    }

    // launcher do pytania o uprawnienia kamery
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // sprawdza czy udzielono uprawnien
        if (isGranted) {
            // uruchamia kamere
            startCamera()
        } else {
            // pokazuje komunikat o braku uprawnien
            Toast.makeText(
                requireContext(),
                getString(R.string.camera_permission_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // launcher do pytania o uprawnienia do plikow
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // sprawdza czy udzielono uprawnien
        if (isGranted) {
            // uruchamia wybor obrazu
            pickImageLauncher.launch("image/*")
        } else {
            // pokazuje komunikat o braku uprawnien
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
        // inflatuje layout fragmentu za pomoca view binding
        _binding = FragmentColorPickerBinding.inflate(inflater, container, false)
        // zwraca root widoku
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // konfiguruje listenery klikniec
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // listener przycisku wyboru obrazu
        binding.chooseImageButton.setOnClickListener {
            // sprawdza uprawnienia i wybiera obraz
            checkStoragePermissionAndPick()
        }

        // listener przycisku zrobienia zdjecia
        binding.takePhotoButton.setOnClickListener {
            // sprawdza uprawnienia kamery
            checkCameraPermission()
        }

        // listener przycisku przechwycenia zdjecia
        binding.btnCapturePhoto.setOnClickListener {
            // wykonuje zdjecie
            takePhoto()
        }

        // listener dotyku obrazu do identyfikacji koloru
        binding.imageView.setOnTouchListener { _, event ->
            // sprawdza czy akcja to dotyk i czy jest zaladowany obraz
            if (event.action == MotionEvent.ACTION_DOWN && currentBitmap != null) {
                // oblicza rozmiar znacznika w pikselach
                val markerSize = 20 * resources.displayMetrics.density
                // ustawia pozycje x znacznika wycentrowana na dotyku
                binding.touchMarker.x = event.x - (markerSize / 2)
                // ustawia pozycje y znacznika wycentrowana na dotyku
                binding.touchMarker.y = event.y - (markerSize / 2)
                // pokazuje znacznik dotyku
                binding.touchMarker.visibility = View.VISIBLE
                
                // identyfikuje kolor w miejscu dotyku
                identifyColorAtPosition(event.x.toInt(), event.y.toInt())
                // zwraca true event obsluzony
                return@setOnTouchListener true
            }
            // zwraca false event nie obsluzony
            false
        }

        // listener przycisku zapisz kolor
        binding.saveColorButton.setOnClickListener {
            // sprawdza czy jest wybrany kolor
            selectedColor?.let { color ->
                // probuje zapisac kolor w viewmodel
                if (colorViewModel.saveColor(color)) {
                    // pokazuje komunikat sukcesu
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.color_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // pokazuje komunikat ze kolor juz istnieje
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.color_already_exists),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // listener przycisku kopiuj wszystko
        binding.copyAllButton.setOnClickListener {
            // sprawdza czy jest wybrany kolor
            selectedColor?.let { color ->
                // tworzy sformatowany tekst z informacjami
                val text = """
                    Name: ${color.name}
                    RGB: ${color.getRGBString()}
                    HEX: ${color.hexValue}
                """.trimIndent()
                
                // pobiera serwis schowka
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                // tworzy clip z tekstem
                val clip = ClipData.newPlainText("Color Info", text)
                // ustawia clip w schowku
                clipboard.setPrimaryClip(clip)
                
                // pokazuje komunikat potwierdzajacy
                Toast.makeText(requireContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkStoragePermissionAndPick() {
        // wybiera odpowiednie uprawnienie w zaleznosci od wersji android
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // android 13+ uzywa system permissions do zdjec
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // starsze wersje uzywaja zewnetrznego storage
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // sprawdza stan uprawnienia
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // uprawnienie juz udzielone uruchamia wybor
                pickImageLauncher.launch("image/*")
            }
            else -> {
                // prosi o uprawnienie
                storagePermissionLauncher.launch(permission)
            }
        }
    }

    private fun checkCameraPermission() {
        // sprawdza stan uprawnienia kamery
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // uprawnienie juz udzielone uruchamia kamere
                startCamera()
            }
            else -> {
                // prosi o uprawnienie kamery
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        // pobiera future providera kamery
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        // dodaje listener gdy provider gotowy
        cameraProviderFuture.addListener({
            // pobiera provider kamery
            val cameraProvider = cameraProviderFuture.get()
            
            // konfiguruje podglad kamery
            val preview = Preview.Builder()
                .build()
                // laczy podglad z powierzchnia widoku
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
            
            // konfiguruje przechwytywanie obrazu
            imageCapture = ImageCapture.Builder().build()
            
            // wybiera tylna kamere
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                // odpina wszystkie use case przed przypieciem nowych
                cameraProvider.unbindAll()
                // przypina use case do cyklu zycia
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
                // pokazuje widok podgladu kamery
                binding.previewView.visibility = View.VISIBLE
                // ukrywa widok statycznego obrazu
                binding.imageView.visibility = View.GONE
                // pokazuje przycisk przechwycenia
                binding.btnCapturePhoto.visibility = View.VISIBLE
                // ukrywa znacznik dotyku
                binding.touchMarker.visibility = View.GONE
                // ukrywa karte z informacjami o kolorze
                binding.colorInfoCard.visibility = View.GONE
                
            } catch (e: Exception) {
                // pokazuje komunikat o bledzie
                Toast.makeText(requireContext(), "Error starting camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // pobiera obiekt przechwytywania lub wychodzi jesli null
        val imageCapture = imageCapture ?: return
        
        // tworzy plik tymczasowy dla zdjecia
        val photoFile = File(
            requireContext().cacheDir,
            "color_photo_${System.currentTimeMillis()}.jpg"
        )
        
        // konfiguruje opcje wyjsciowe dla pliku
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // wykonuje przechwycenie zdjecia
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // dekoduje zdjecie z pliku na bitmap
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    // laduje bitmap do widoku
                    loadImageToBitmap(bitmap)
                    
                    // ukrywa podglad kamery
                    binding.previewView.visibility = View.GONE
                    // ukrywa przycisk przechwycenia
                    binding.btnCapturePhoto.visibility = View.GONE
                    // pokazuje widok statycznego obrazu
                    binding.imageView.visibility = View.VISIBLE
                    // pokazuje instrukcje
                    binding.instructionText.visibility = View.VISIBLE
                }
                
                override fun onError(exception: ImageCaptureException) {
                    // pokazuje komunikat o bledzie
                    Toast.makeText(requireContext(), "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun loadImageToBitmap(bitmap: Bitmap) {
        // zapisuje bitmap jako aktualny
        currentBitmap = bitmap
        // ustawia bitmap w imageview
        binding.imageView.setImageBitmap(bitmap)
        // pokazuje imageview
        binding.imageView.visibility = View.VISIBLE
        // pokazuje tekst instrukcji
        binding.instructionText.visibility = View.VISIBLE
        // ukrywa karte informacji o kolorze
        binding.colorInfoCard.visibility = View.GONE
    }

    private fun loadImage(uri: Uri) {
        // laduje bitmap z uri uzywajac imageutils
        val bitmap = ImageUtils.loadBitmapFromUri(requireContext(), uri)
        // sprawdza czy bitmap zaladowano pomyslnie
        if (bitmap != null) {
            // laduje bitmap do widoku
            loadImageToBitmap(bitmap)
        } else {
            // pokazuje komunikat o bledzie ladowania
            Toast.makeText(
                requireContext(),
                getString(R.string.error_loading_image),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun identifyColorAtPosition(x: Int, y: Int) {
        // pobiera aktualny bitmap lub wychodzi jesli null
        val bitmap = currentBitmap ?: return

        // konwertuje wspolrzedne dotyku na wspolrzedne bitmap
        val imageView = binding.imageView
        // pobiera macierz transformacji obrazu
        val imageMatrix = imageView.imageMatrix
        // tworzy tablice na wartosci macierzy
        val values = FloatArray(9)
        // pobiera wartosci z macierzy
        imageMatrix.getValues(values)

        // wyciaga skale x z macierzy
        val scaleX = values[0]
        // wyciaga skale y z macierzy
        val scaleY = values[4]
        // wyciaga przesuniecie x z macierzy
        val transX = values[2]
        // wyciaga przesuniecie y z macierzy
        val transY = values[5]

        // konwertuje wspolrzedna x dotyku na wspolrzedna bitmap
        val bitmapX = ((x - transX) / scaleX).toInt()
        // konwertuje wspolrzedna y dotyku na wspolrzedna bitmap
        val bitmapY = ((y - transY) / scaleY).toInt()

        // pobiera kolor piksela z bitmap
        val colorInt = ColorUtils.getPixelColor(bitmap, bitmapX, bitmapY)
        
        // sprawdza czy kolor nie jest przezroczysty
        if (colorInt != android.graphics.Color.TRANSPARENT) {
            // wyciaga skladnik czerwony
            val red = android.graphics.Color.red(colorInt)
            // wyciaga skladnik zielony
            val green = android.graphics.Color.green(colorInt)
            // wyciaga skladnik niebieski
            val blue = android.graphics.Color.blue(colorInt)
            // konwertuje rgb na hex
            val hex = ColorUtils.rgbToHex(red, green, blue)
            // mapuje rgb na nazwe koloru
            val colorName = ColorNameMapper.getColorName(red, green, blue)

            // tworzy obiekt koloru
            val color = Color(
                name = colorName,
                red = red,
                green = green,
                blue = blue,
                hexValue = hex
            )

            // wyswietla informacje o kolorze
            displayColorInfo(color)
        }
    }

    private fun displayColorInfo(color: Color) {
        // zapisuje kolor jako wybrany
        selectedColor = color
        
        // ustawia kolor tla podgladu
        binding.colorPreview.setBackgroundColor(color.toColorInt())
        // ustawia tekst nazwy koloru
        binding.colorName.text = color.name
        // ustawia tekst wartosci hex
        binding.colorHex.text = color.hexValue
        // ustawia tekst wartosci rgb
        binding.colorRgb.text = color.getRGBString()
        
        // pokazuje karte z informacjami
        binding.colorInfoCard.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // czysci binding zeby uniknac wyciekow pamieci
        _binding = null
        // zwalnia zasoby bitmap
        currentBitmap?.recycle()
        // zeruje referencje do bitmap
        currentBitmap = null
    }
}
