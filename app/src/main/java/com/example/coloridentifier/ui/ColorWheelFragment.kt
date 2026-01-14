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

/**
 * fragment z kolem kolorow do tworzenia palet
 */
class ColorWheelFragment : Fragment() {

    // nullable binding dla layoutu fragmentu
    private var _binding: FragmentColorWheelBinding? = null
    // non-null binding getter
    private val binding get() = _binding!!
    // viewmodel palet wspoldzielony z aktywnoscia
    private val paletteViewModel: PaletteViewModel by activityViewModels()
    // viewmodel kolorow wspoldzielony z aktywnoscia
    private val colorViewModel: ColorViewModel by activityViewModels()
    
    // lista widokow slotow kolorow
    private val colorSlots = mutableListOf<View>()
    // lista kolorow w slotach maksymalnie 5
    private val slotColors = mutableListOf<Color?>(null, null, null, null, null)
    // indeks aktualnie zaznaczonego slotu
    private var selectedSlotIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // inflatuje layout fragmentu za pomoca view binding
        _binding = FragmentColorWheelBinding.inflate(inflater, container, false)
        // zwraca root widoku
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // konfiguruje kolo kolorow
        setupColorWheel()
        // konfiguruje sloty kolorow
        setupSlots()
        // konfiguruje suwak jasnosci
        setupBrightnessSlider()
        // konfiguruje przycisk dodaj zapisany kolor
        setupAddSavedColorButton()
        // konfiguruje przycisk zapisz kolor
        setupSaveColorButton()
        // konfiguruje przycisk zapisz palete
        setupSaveButton()
    }

    private fun setupColorWheel() {
        // ustawia listener wyboru koloru
        binding.colorWheelView.setOnColorSelectedListener { colorInt ->
            // sprawdza czy jest wybrany slot
            if (selectedSlotIndex >= 0) {
                // wyciaga skladnik czerwony
                val red = AndroidColor.red(colorInt)
                // wyciaga skladnik zielony
                val green = AndroidColor.green(colorInt)
                // wyciaga skladnik niebieski
                val blue = AndroidColor.blue(colorInt)
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

                // zapisuje kolor w slocie
                slotColors[selectedSlotIndex] = color
                // ustawia kolor tla slotu
                colorSlots[selectedSlotIndex].setBackgroundColor(colorInt)
            }
            
            // aktualizuje gradient suwaka przy zmianie hue
            updateSliderTrackColor()
        }
    }

    private fun setupSlots() {
        // dodaje widoki slotow do listy
        colorSlots.add(binding.colorSlot1)
        colorSlots.add(binding.colorSlot2)
        colorSlots.add(binding.colorSlot3)
        colorSlots.add(binding.colorSlot4)
        colorSlots.add(binding.colorSlot5)

        // tworzy liste kontenerow slotow
        val slotContainers = listOf(
            binding.slot1Container,
            binding.slot2Container,
            binding.slot3Container,
            binding.slot4Container,
            binding.slot5Container
        )

        // iteruje przez kontenery z indeksem
        slotContainers.forEachIndexed { index, container ->
            // ustawia listener klikniecia
            container.setOnClickListener {
                // zaznacza slot
                selectSlot(index)
            }
        }
    }

    private fun selectSlot(index: Int) {
        // zapisuje indeks zaznaczonego slotu
        selectedSlotIndex = index
        
        // iteruje przez sloty z indeksem
        colorSlots.forEachIndexed { i, slot ->
            // sprawdza czy to zaznaczony slot
            if (i == index) {
                // ustawia pelna nieprzezroczystosc
                slot.alpha = 1.0f
            } else {
                // ustawia polprzezroczystosc
                slot.alpha = 0.5f
            }
        }

        // pokazuje komunikat o wybranym slocie
        Toast.makeText(
            requireContext(),
            getString(R.string.slot_label, index + 1),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupBrightnessSlider() {
        // aktualizuje gradient suwaka na starcie
        updateSliderTrackColor()
        
        // dodaje listener zmiany wartosci suwaka
        binding.brightnessSlider.addOnChangeListener { _, value, _ ->
            // ustawia wartosc value w kole kolorow
            binding.colorWheelView.setValue(value)
        }
    }
    
    /**
     * aktualizuje kolor tla suwaka na podstawie wybranego hue
     */
    private fun updateSliderTrackColor() {
        // pobiera aktualny hue z kola kolorow
        val hue = binding.colorWheelView.getSelectedHue()
        // konwertuje hue na pelny kolor
        val fullColor = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
        
        // tworzy gradient od czarnego do pelnego koloru
        val gradientDrawable = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(AndroidColor.BLACK, fullColor)
        )
        // ustawia zaokraglone narozniki
        gradientDrawable.cornerRadius = 8f
        
        // ustawia wysokosc tracka
        binding.brightnessSlider.trackHeight = 12
        // tworzy okragly kciuk
        binding.brightnessSlider.setCustomThumbDrawable(
            android.graphics.drawable.ShapeDrawable(android.graphics.drawable.shapes.OvalShape()).apply {
                // ustawia szerokosc kciuka
                intrinsicWidth = 24
                // ustawia wysokosc kciuka
                intrinsicHeight = 24
                // ustawia kolor farby na bialy
                paint.color = AndroidColor.WHITE
                // ustawia styl na wypelniony
                paint.style = android.graphics.Paint.Style.FILL
            }
        )
    }

    private fun setupAddSavedColorButton() {
        // ustawia listener klikniecia przycisku
        binding.addSavedColorButton.setOnClickListener {
            // sprawdza czy wybrany slot
            if (selectedSlotIndex < 0) {
                // pokazuje komunikat o braku wyboru
                Toast.makeText(
                    requireContext(),
                    "Please select a slot first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            
            // pobiera liste zapisanych kolorow
            val savedColors = colorViewModel.colors.value ?: emptyList()
            
            // sprawdza czy lista nie jest pusta
            if (savedColors.isEmpty()) {
                // pokazuje komunikat o braku kolorow
                Toast.makeText(
                    requireContext(),
                    "No saved colors available",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            
            // tworzy tablice nazw kolorow
            val colorNames = savedColors.map { it.name }.toTypedArray()
            
            // pokazuje dialog wyboru koloru
            AlertDialog.Builder(requireContext())
                .setTitle("Select a color")
                // ustawia liste kolorow
                .setItems(colorNames) { _, which ->
                    // pobiera wybrany kolor
                    val selectedColor = savedColors[which]
                    // zapisuje w slocie
                    slotColors[selectedSlotIndex] = selectedColor
                    // ustawia kolor tla slotu
                    colorSlots[selectedSlotIndex].setBackgroundColor(selectedColor.toColorInt())
                    // pokazuje komunikat potwierdzajacy
                    Toast.makeText(
                        requireContext(),
                        "Added ${selectedColor.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // dodaje przycisk anuluj
                .setNegativeButton("Cancel", null)
                // wyswietla dialog
                .show()
        }
    }

    private fun setupSaveColorButton() {
        // ustawia listener klikniecia przycisku
        binding.saveColorButton.setOnClickListener {
            // sprawdza czy wybrany slot ma kolor
            if (selectedSlotIndex >= 0 && slotColors[selectedSlotIndex] != null) {
                // pobiera kolor ze slotu
                val color = slotColors[selectedSlotIndex]!!
                // zapisuje kolor w viewmodel
                if (colorViewModel.saveColor(color)) {
                    // pokazuje komunikat sukcesu
                    Toast.makeText(
                        requireContext(),
                        "Color saved!",
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
            } else {
                // pokazuje komunikat o braku wyboru
                Toast.makeText(
                    requireContext(),
                    "Please select a slot with a color first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupSaveButton() {
        // ustawia listener klikniecia przycisku
        binding.savePaletteButton.setOnClickListener {
            // filtruje sloty usuwajac puste
            val filledColors = slotColors.filterNotNull()
            
            // sprawdza czy jest chociaz jeden kolor
            if (filledColors.isEmpty()) {
                // pokazuje komunikat o braku kolorow
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_at_least_one_color),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // pokazuje dialog zapisywania palety
            showSavePaletteDialog(filledColors)
        }
    }

    private fun showSavePaletteDialog(colors: List<Color>) {
        // tworzy pole tekstowe
        val input = EditText(requireContext())
        // ustawia placeholder
        input.hint = getString(R.string.enter_palette_name)

        // tworzy i pokazuje dialog
        AlertDialog.Builder(requireContext())
            // ustawia tytul
            .setTitle(getString(R.string.palette_name))
            // dodaje pole tekstowe
            .setView(input)
            // dodaje przycisk zapisz
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // pobiera nazwe i usuwa biale znaki
                val name = input.text.toString().trim()
                // sprawdza czy nazwa nie jest pusta
                if (name.isNotBlank()) {
                    // tworzy palete w viewmodel
                    if (paletteViewModel.createPalette(name, colors)) {
                        // pokazuje komunikat sukcesu
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.palette_created),
                            Toast.LENGTH_SHORT
                        ).show()
                        // resetuje sloty po zapisaniu
                        resetSlots()
                    } else {
                        // pokazuje komunikat o bledzie
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_creating_palette),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // pokazuje komunikat o niepoprawnej nazwie
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.invalid_palette_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            // dodaje przycisk anuluj
            .setNegativeButton(getString(R.string.cancel), null)
            // wyswietla dialog
            .show()
    }

    private fun resetSlots() {
        // czysci liste kolorow w slotach
        slotColors.clear()
        // dodaje puste sloty z powrotem
        slotColors.addAll(listOf(null, null, null, null, null))
        // iteruje przez widoki slotow
        colorSlots.forEach { slot ->
            // ustawia szary kolor tla
            slot.setBackgroundColor(resources.getColor(R.color.gray_light, null))
            // ustawia pelna nieprzezroczystosc
            slot.alpha = 1.0f
        }
        // resetuje indeks zaznaczonego slotu
        selectedSlotIndex = -1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // czysci binding zeby uniknac wyciekow pamieci
        _binding = null
    }
}
