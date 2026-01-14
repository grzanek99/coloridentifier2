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
        // tablica z opcjami
        val options = arrayOf(
            getString(R.string.copy_hex),
            getString(R.string.copy_rgb),
            "Copy All",
            getString(R.string.share_color),
            getString(R.string.delete_color)
        )

        // tworzy i pokazuje dialog opcji
        AlertDialog.Builder(requireContext())
            // ustawia tytul na nazwe koloru
            .setTitle(color.name)
            // ustawia liste opcji
            .setItems(options) { _, which ->
                // obsluguje wybor opcji
                when (which) {
                    // opcja 0 kopiuj hex
                    0 -> {
                        // kopiuje hex do schowka
                        ShareUtils.copyToClipboard(requireContext(), "HEX", color.hexValue)
                        // pokazuje komunikat potwierdzajacy
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.copied_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // opcja 1 kopiuj rgb
                    1 -> {
                        // kopiuje rgb do schowka
                        ShareUtils.copyToClipboard(requireContext(), "RGB", color.getRGBString())
                        // pokazuje komunikat potwierdzajacy
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.copied_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // opcja 2 kopiuj wszystko
                    2 -> {
                        // tworzy pelny tekst z informacjami
                        val text = """
                            Name: ${color.name}
                            RGB: ${color.getRGBString()}
                            HEX: ${color.hexValue}
                        """.trimIndent()
                        // kopiuje do schowka
                        ShareUtils.copyToClipboard(requireContext(), "Color Info", text)
                        // pokazuje komunikat potwierdzajacy
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.copied_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // opcja 3 udostepnij
                    3 -> {
                        // pokazuje dialog opcji udostepniania
                        showShareOptionsDialog(color)
                    }
                    // opcja 4 usun
                    4 -> {
                        // pokazuje dialog potwierdzenia usuniecia
                        showDeleteColorDialog(color)
                    }
                }
            }
            // wyswietla dialog
            .show()
    }

    private fun showShareOptionsDialog(color: Color) {
        // tablica z opcjami udostepniania
        val options = arrayOf(
            getString(R.string.share_as_text),
            getString(R.string.share_as_image)
        )

        // tworzy i pokazuje dialog opcji
        AlertDialog.Builder(requireContext())
            // ustawia tytul
            .setTitle(getString(R.string.share_color))
            // ustawia liste opcji
            .setItems(options) { _, which ->
                // obsluguje wybor opcji
                when (which) {
                    // opcja 0 udostepnij jako tekst
                    0 -> ShareUtils.shareColorAsText(requireContext(), color)
                    // opcja 1 udostepnij jako obraz
                    1 -> ShareUtils.shareColorAsImage(requireContext(), color)
                }
            }
            // wyswietla dialog
            .show()
    }

    private fun showDeleteColorDialog(color: Color) {
        // tworzy i pokazuje dialog potwierdzenia
        AlertDialog.Builder(requireContext())
            // ustawia tytul
            .setTitle(getString(R.string.confirm_delete))
            // ustawia pytanie
            .setMessage(getString(R.string.confirm_delete_color))
            // dodaje przycisk usun
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                // usuwa kolor z viewmodel
                colorViewModel.deleteColor(color)
                // pokazuje komunikat potwierdzajacy
                Toast.makeText(
                    requireContext(),
                    getString(R.string.color_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }
            // dodaje przycisk anuluj
            .setNegativeButton(getString(R.string.cancel), null)
            // wyswietla dialog
            .show()
    }

    private fun showClearAllDialog() {
        // tworzy i pokazuje dialog potwierdzenia
        AlertDialog.Builder(requireContext())
            // ustawia tytul
            .setTitle(getString(R.string.clear_all_colors))
            // ustawia pytanie
            .setMessage(getString(R.string.confirm_clear_all))
            // dodaje przycisk tak
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                // czysci wszystkie kolory
                colorViewModel.clearAllColors()
                // pokazuje komunikat potwierdzajacy
                Toast.makeText(
                    requireContext(),
                    getString(R.string.colors_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }
            // dodaje przycisk nie
            .setNegativeButton(getString(R.string.no), null)
            // wyswietla dialog
            .show()
    }

    private fun showCreatePaletteDialog() {
        // pobiera liste zaznaczonych kolorow
        val selectedColors = colorViewModel.getSelectedColorsList()
        
        // sprawdza czy lista nie jest pusta
        if (selectedColors.isEmpty()) {
            // pokazuje komunikat o braku zaznaczenia
            Toast.makeText(
                requireContext(),
                getString(R.string.select_at_least_one_color),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

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
                    if (paletteViewModel.createPalette(name, selectedColors)) {
                        // pokazuje komunikat sukcesu
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.palette_created),
                            Toast.LENGTH_SHORT
                        ).show()
                        // wylacza tryb zaznaczania
                        exitSelectionMode()
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

    override fun onDestroyView() {
        super.onDestroyView()
        // czysci binding zeby uniknac wyciekow pamieci
        _binding = null
    }
}
