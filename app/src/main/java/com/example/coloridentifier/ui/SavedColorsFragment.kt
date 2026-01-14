package com.example.coloridentifier.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coloridentifier.R
import com.example.coloridentifier.adapter.ColorAdapter
import com.example.coloridentifier.databinding.FragmentSavedColorsBinding
import com.example.coloridentifier.model.Color
import com.example.coloridentifier.utils.ShareUtils
import com.example.coloridentifier.viewmodel.ColorViewModel
import com.example.coloridentifier.viewmodel.PaletteViewModel

/**
 * fragment wyswietlajacy zapisane kolory
 */
class SavedColorsFragment : Fragment() {

    // nullable binding dla layoutu fragmentu
    private var _binding: FragmentSavedColorsBinding? = null
    // non-null binding getter
    private val binding get() = _binding!!
    // viewmodel kolorow wspoldzielony z aktywnoscia
    private val colorViewModel: ColorViewModel by activityViewModels()
    // viewmodel palet wspoldzielony z aktywnoscia
    private val paletteViewModel: PaletteViewModel by activityViewModels()
    
    // adapter recyclerview dla kolorow
    private lateinit var colorAdapter: ColorAdapter
    // flaga trybu zaznaczania kolorow
    private var isSelectionMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // inflatuje layout fragmentu za pomoca view binding
        _binding = FragmentSavedColorsBinding.inflate(inflater, container, false)
        // zwraca root widoku
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // konfiguruje recyclerview
        setupRecyclerView()
        // konfiguruje observery livedata
        setupObservers()
        // konfiguruje listenery klikniec
        setupClickListeners()
        // konfiguruje menu w toolbarze
        setupMenu()
    }

    private fun setupRecyclerView() {
        // tworzy adapter z callbackami
        colorAdapter = ColorAdapter(
            colors = emptyList(),
            // callback dla zwyklego klikniecia
            onColorClick = { color ->
                // sprawdza czy aktywny tryb zaznaczania
                if (isSelectionMode) {
                    // przelacza zaznaczenie koloru
                    colorViewModel.toggleColorSelection(color)
                } else {
                    // pokazuje opcje koloru
                    showColorOptionsDialog(color)
                }
            },
            // callback dla dlugiego klikniecia
            onColorLongClick = { color ->
                // wlacza tryb zaznaczania jesli nieaktywny
                if (!isSelectionMode) {
                    // rozpoczyna tryb zaznaczania
                    startSelectionMode()
                    // zaznacza klikniety kolor
                    colorViewModel.toggleColorSelection(color)
                }
            },
            // funkcja sprawdzajaca czy kolor zaznaczony
            isColorSelected = { color ->
                colorViewModel.isColorSelected(color)
            }
        )

        // konfiguruje recyclerview
        binding.colorsRecyclerView.apply {
            // ustawia layout manager linearny
            layoutManager = LinearLayoutManager(requireContext())
            // ustawia adapter
            adapter = colorAdapter
        }
    }

    private fun setupObservers() {
        // obserwuje zmiany w liscie kolorow
        colorViewModel.colors.observe(viewLifecycleOwner) { colors ->
            // sprawdza czy lista pusta
            if (colors.isEmpty()) {
                // pokazuje tekst pustej listy
                binding.emptyTextView.visibility = View.VISIBLE
                // ukrywa recyclerview
                binding.colorsRecyclerView.visibility = View.GONE
            } else {
                // ukrywa tekst pustej listy
                binding.emptyTextView.visibility = View.GONE
                // pokazuje recyclerview
                binding.colorsRecyclerView.visibility = View.VISIBLE
                // aktualizuje adapter nowymi kolorami
                colorAdapter.updateColors(colors)
            }
        }

        // obserwuje zmiany w zaznaczonych kolorach
        colorViewModel.selectedColors.observe(viewLifecycleOwner) { selectedColors ->
            // aktualizuje interfejs zaznaczenia
            updateSelectionUI(selectedColors.size)
            // odswieza adapter
            colorAdapter.notifyDataSetChanged()
        }
    }

    private fun setupClickListeners() {
        // listener przycisku utworz palete
        binding.createPaletteButton.setOnClickListener {
            // pokazuje dialog tworzenia palety
            showCreatePaletteDialog()
        }

        // listener przycisku anuluj zaznaczenie
        binding.cancelSelectionButton.setOnClickListener {
            // wylacza tryb zaznaczania
            exitSelectionMode()
        }
    }

    private fun setupMenu() {
        // pobiera menu host z aktywnosci
        val menuHost: MenuHost = requireActivity()
        // dodaje provider menu
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // czysci istniejace menu
                menu.clear()
                // sprawdza czy nie w trybie zaznaczania
                if (!isSelectionMode) {
                    // inflatuje menu z xml
                    menuInflater.inflate(R.menu.saved_colors_menu, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // obsluguje wybor elementu menu
                return when (menuItem.itemId) {
                    // opcja wyczysc wszystko
                    R.id.action_clear_all -> {
                        // pokazuje dialog potwierdzenia
                        showClearAllDialog()
                        true
                    }
                    // opcja zaznacz kolory
                    R.id.action_select_colors -> {
                        // wlacza tryb zaznaczania
                        startSelectionMode()
                        true
                    }
                    // inny element nie obsluzony
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun startSelectionMode() {
        // wlacza flage trybu zaznaczania
        isSelectionMode = true
        // aktualizuje interfejs zaznaczenia
        updateSelectionUI(0)
        // odswieza menu
        requireActivity().invalidateOptionsMenu()
    }

    private fun exitSelectionMode() {
        // wylacza flage trybu zaznaczania
        isSelectionMode = false
        // czysci zaznaczenie w viewmodel
        colorViewModel.clearSelection()
        // ukrywa przyciski akcji
        binding.actionButtonsLayout.visibility = View.GONE
        // odswieza menu
        requireActivity().invalidateOptionsMenu()
    }

    private fun updateSelectionUI(count: Int) {
        // sprawdza czy tryb zaznaczania i sa zaznaczone kolory
        if (isSelectionMode && count > 0) {
            // pokazuje layout z przyciskami akcji
            binding.actionButtonsLayout.visibility = View.VISIBLE
            // ustawia tekst z liczba zaznaczonych
            binding.selectionCountText.text = getString(R.string.selected_count, count)
        } else if (!isSelectionMode) {
            // ukrywa przyciski akcji gdy nie w trybie zaznaczania
            binding.actionButtonsLayout.visibility = View.GONE
        }
    }

    private fun showColorOptionsDialog(color: Color) {
        val options = arrayOf(
            getString(R.string.copy_hex),
            getString(R.string.copy_rgb),
            "Copy All",
            getString(R.string.share_color),
            getString(R.string.delete_color)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(color.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        ShareUtils.copyToClipboard(requireContext(), "HEX", color.hexValue)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.copied_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    1 -> {
                        ShareUtils.copyToClipboard(requireContext(), "RGB", color.getRGBString())
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.copied_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    2 -> {
                        val text = """
                            Name: ${color.name}
                            RGB: ${color.getRGBString()}
                            HEX: ${color.hexValue}
                        """.trimIndent()
                        ShareUtils.copyToClipboard(requireContext(), "Color Info", text)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.copied_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    3 -> {
                        showShareOptionsDialog(color)
                    }
                    4 -> {
                        showDeleteColorDialog(color)
                    }
                }
            }
            .show()
    }

    private fun showShareOptionsDialog(color: Color) {
        val options = arrayOf(
            getString(R.string.share_as_text),
            getString(R.string.share_as_image)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.share_color))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ShareUtils.shareColorAsText(requireContext(), color)
                    1 -> ShareUtils.shareColorAsImage(requireContext(), color)
                }
            }
            .show()
    }

    private fun showDeleteColorDialog(color: Color) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete))
            .setMessage(getString(R.string.confirm_delete_color))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                colorViewModel.deleteColor(color)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.color_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showClearAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_all_colors))
            .setMessage(getString(R.string.confirm_clear_all))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                colorViewModel.clearAllColors()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.colors_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showCreatePaletteDialog() {
        val selectedColors = colorViewModel.getSelectedColorsList()
        
        if (selectedColors.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.select_at_least_one_color),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val input = EditText(requireContext())
        input.hint = getString(R.string.enter_palette_name)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.palette_name))
            .setView(input)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotBlank()) {
                    if (paletteViewModel.createPalette(name, selectedColors)) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.palette_created),
                            Toast.LENGTH_SHORT
                        ).show()
                        exitSelectionMode()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
