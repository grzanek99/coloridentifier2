package com.example.coloridentifier.ui

import android.app.AlertDialog
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.coloridentifier.R
import com.example.coloridentifier.databinding.FragmentColorWheelBinding
import com.example.coloridentifier.model.Color
import com.example.coloridentifier.utils.ColorNameMapper
import com.example.coloridentifier.utils.ColorUtils
import com.example.coloridentifier.viewmodel.PaletteViewModel
import com.example.coloridentifier.viewmodel.ColorViewModel
import com.google.android.material.slider.Slider

class ColorWheelFragment : Fragment() {

    private var _binding: FragmentColorWheelBinding? = null
    private val binding get() = _binding!!
    private val paletteViewModel: PaletteViewModel by activityViewModels()
    private val colorViewModel: ColorViewModel by activityViewModels()
    
    private val colorSlots = mutableListOf<View>()
    private val slotColors = mutableListOf<Color?>(null, null, null, null, null)
    private var selectedSlotIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColorWheelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupColorWheel()
        setupSlots()
        setupBrightnessSlider()
        setupAddSavedColorButton()
        setupSaveColorButton()
        setupSaveButton()
    }

    private fun setupColorWheel() {
        binding.colorWheelView.setOnColorSelectedListener { colorInt ->
            if (selectedSlotIndex >= 0) {
                val red = AndroidColor.red(colorInt)
                val green = AndroidColor.green(colorInt)
                val blue = AndroidColor.blue(colorInt)
                val hex = ColorUtils.rgbToHex(red, green, blue)
                val colorName = ColorNameMapper.getColorName(red, green, blue)

                val color = Color(
                    name = colorName,
                    red = red,
                    green = green,
                    blue = blue,
                    hexValue = hex
                )

                slotColors[selectedSlotIndex] = color
                colorSlots[selectedSlotIndex].setBackgroundColor(colorInt)
            }
            
            // Aktualizuj gradient suwaka przy zmianie hue
            updateSliderTrackColor()
        }
    }

    private fun setupSlots() {
        colorSlots.add(binding.colorSlot1)
        colorSlots.add(binding.colorSlot2)
        colorSlots.add(binding.colorSlot3)
        colorSlots.add(binding.colorSlot4)
        colorSlots.add(binding.colorSlot5)

        val slotContainers = listOf(
            binding.slot1Container,
            binding.slot2Container,
            binding.slot3Container,
            binding.slot4Container,
            binding.slot5Container
        )

        slotContainers.forEachIndexed { index, container ->
            container.setOnClickListener {
                selectSlot(index)
            }
        }
    }

    private fun selectSlot(index: Int) {
        selectedSlotIndex = index
        
        // Highlight selected slot
        colorSlots.forEachIndexed { i, slot ->
            if (i == index) {
                slot.alpha = 1.0f
            } else {
                slot.alpha = 0.5f
            }
        }

        Toast.makeText(
            requireContext(),
            getString(R.string.slot_label, index + 1),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupBrightnessSlider() {
        // Ustaw początkowy gradient suwaka
        updateSliderTrackColor()
        
        binding.brightnessSlider.addOnChangeListener { _, value, _ ->
            binding.colorWheelView.setValue(value)
        }
    }
    
    /**
     * Aktualizuje kolor tła suwaka na podstawie aktualnie wybranego hue
     */
    private fun updateSliderTrackColor() {
        val hue = binding.colorWheelView.getSelectedHue()
        val fullColor = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
        
        // Tworzymy gradient drawable od czarnego do pełnego koloru
        val gradientDrawable = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(AndroidColor.BLACK, fullColor)
        )
        gradientDrawable.cornerRadius = 8f
        
        // Ustawiamy gradient jako tło track
        binding.brightnessSlider.trackHeight = 12
        binding.brightnessSlider.setCustomThumbDrawable(
            android.graphics.drawable.ShapeDrawable(android.graphics.drawable.shapes.OvalShape()).apply {
                intrinsicWidth = 24
                intrinsicHeight = 24
                paint.color = AndroidColor.WHITE
                paint.style = android.graphics.Paint.Style.FILL
            }
        )
    }

    private fun setupAddSavedColorButton() {
        binding.addSavedColorButton.setOnClickListener {
            if (selectedSlotIndex < 0) {
                Toast.makeText(
                    requireContext(),
                    "Please select a slot first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            
            // Pobierz listę zapisanych kolorów
            val savedColors = colorViewModel.colors.value ?: emptyList()
            
            if (savedColors.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "No saved colors available",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            
            // Pokaż dialog z listą kolorów
            val colorNames = savedColors.map { it.name }.toTypedArray()
            
            AlertDialog.Builder(requireContext())
                .setTitle("Select a color")
                .setItems(colorNames) { _, which ->
                    val selectedColor = savedColors[which]
                    slotColors[selectedSlotIndex] = selectedColor
                    colorSlots[selectedSlotIndex].setBackgroundColor(selectedColor.toColorInt())
                    Toast.makeText(
                        requireContext(),
                        "Added ${selectedColor.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupSaveColorButton() {
        binding.saveColorButton.setOnClickListener {
            // Sprawdź czy jest wybrany kolor w slocie
            if (selectedSlotIndex >= 0 && slotColors[selectedSlotIndex] != null) {
                val color = slotColors[selectedSlotIndex]!!
                if (colorViewModel.saveColor(color)) {
                    Toast.makeText(
                        requireContext(),
                        "Color saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.color_already_exists),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select a slot with a color first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupSaveButton() {
        binding.savePaletteButton.setOnClickListener {
            val filledColors = slotColors.filterNotNull()
            
            if (filledColors.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_at_least_one_color),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            showSavePaletteDialog(filledColors)
        }
    }

    private fun showSavePaletteDialog(colors: List<Color>) {
        val input = EditText(requireContext())
        input.hint = getString(R.string.enter_palette_name)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.palette_name))
            .setView(input)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotBlank()) {
                    if (paletteViewModel.createPalette(name, colors)) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.palette_created),
                            Toast.LENGTH_SHORT
                        ).show()
                        resetSlots()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_creating_palette),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.invalid_palette_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun resetSlots() {
        slotColors.clear()
        slotColors.addAll(listOf(null, null, null, null, null))
        colorSlots.forEach { slot ->
            slot.setBackgroundColor(resources.getColor(R.color.gray_light, null))
            slot.alpha = 1.0f
        }
        selectedSlotIndex = -1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
