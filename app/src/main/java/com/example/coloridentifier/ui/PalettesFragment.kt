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
import com.example.coloridentifier.adapter.PaletteAdapter
import com.example.coloridentifier.databinding.FragmentPalettesBinding
import com.example.coloridentifier.model.Palette
import com.example.coloridentifier.utils.ShareUtils
import com.example.coloridentifier.viewmodel.PaletteViewModel

/**
 * fragment wyswietlajacy liste zapisanych palet
 */
class PalettesFragment : Fragment() {

    // nullable binding dla layoutu fragmentu
    private var _binding: FragmentPalettesBinding? = null
    // non-null binding getter
    private val binding get() = _binding!!
    // viewmodel palet wspoldzielony z aktywnoscia
    private val paletteViewModel: PaletteViewModel by activityViewModels()
    
    // adapter recyclerview dla palet
    private lateinit var paletteAdapter: PaletteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // inflatuje layout fragmentu za pomoca view binding
        _binding = FragmentPalettesBinding.inflate(inflater, container, false)
        // zwraca root widoku
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // konfiguruje recyclerview
        setupRecyclerView()
        // konfiguruje observery livedata
        setupObservers()
        // konfiguruje menu w toolbarze
        setupMenu()
    }

    private fun setupRecyclerView() {
        // tworzy adapter z callbackami
        paletteAdapter = PaletteAdapter(
            palettes = emptyList(),
            // callback dla zwyklego klikniecia
            onPaletteClick = { palette ->
                // pokazuje szczegoly palety
                showPaletteDetailsDialog(palette)
            },
            // callback dla dlugiego klikniecia
            onPaletteLongClick = { palette ->
                // pokazuje opcje palety
                showPaletteOptionsDialog(palette)
            }
        )

        // konfiguruje recyclerview
        binding.palettesRecyclerView.apply {
            // ustawia layout manager linearny
            layoutManager = LinearLayoutManager(requireContext())
            // ustawia adapter
            adapter = paletteAdapter
        }
    }

    private fun setupObservers() {
        // obserwuje zmiany w liscie palet
        paletteViewModel.palettes.observe(viewLifecycleOwner) { palettes ->
            // sprawdza czy lista pusta
            if (palettes.isEmpty()) {
                // pokazuje tekst pustej listy
                binding.emptyTextView.visibility = View.VISIBLE
                // ukrywa recyclerview
                binding.palettesRecyclerView.visibility = View.GONE
            } else {
                // ukrywa tekst pustej listy
                binding.emptyTextView.visibility = View.GONE
                // pokazuje recyclerview
                binding.palettesRecyclerView.visibility = View.VISIBLE
                // aktualizuje adapter nowymi paletami
                paletteAdapter.updatePalettes(palettes)
            }
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
                // inflatuje menu z xml
                menuInflater.inflate(R.menu.palettes_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // obsluguje wybor elementu menu
                return when (menuItem.itemId) {
                    // opcja wyczysc wszystkie palety
                    R.id.action_clear_all_palettes -> {
                        // pokazuje dialog potwierdzenia
                        showClearAllDialog()
                        true
                    }
                    // inny element nie obsluzony
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showPaletteDetailsDialog(palette: Palette) {
        // buduje tekst ze szczegolami palety
        val message = buildString {
            append("Colors in palette:\n\n")
            // iteruje przez kolory z indeksem
            palette.colors.forEachIndexed { index, color ->
                // dodaje numer i nazwe
                append("${index + 1}. ${color.name}\n")
                // dodaje hex i rgb
                append("   ${color.hexValue} - ${color.getRGBString()}\n\n")
            }
        }

        // tworzy i pokazuje dialog
        AlertDialog.Builder(requireContext())
            // ustawia tytul na nazwe palety
            .setTitle(palette.name)
            // ustawia wiadomosc ze szczegolami
            .setMessage(message)
            // dodaje przycisk ok
            .setPositiveButton(getString(R.string.ok), null)
            // wyswietla dialog
            .show()
    }

    private fun showPaletteOptionsDialog(palette: Palette) {
        // tablica z opcjami
        val options = arrayOf(
            getString(R.string.rename_palette),
            getString(R.string.share_palette),
            getString(R.string.delete_palette)
        )

        // tworzy i pokazuje dialog opcji
        AlertDialog.Builder(requireContext())
            // ustawia tytul na nazwe palety
            .setTitle(palette.name)
            // ustawia liste opcji
            .setItems(options) { _, which ->
                // obsluguje wybor opcji
                when (which) {
                    // opcja 0 zmien nazwe
                    0 -> showRenamePaletteDialog(palette)
                    // opcja 1 udostepnij
                    1 -> showSharePaletteDialog(palette)
                    // opcja 2 usun
                    2 -> showDeletePaletteDialog(palette)
                }
            }
            // wyswietla dialog
            .show()
    }

    private fun showRenamePaletteDialog(palette: Palette) {
        // tworzy pole tekstowe
        val input = EditText(requireContext())
        // ustawia aktualna nazwe
        input.setText(palette.name)

        // tworzy i pokazuje dialog
        AlertDialog.Builder(requireContext())
            // ustawia tytul
            .setTitle(getString(R.string.rename_palette))
            // dodaje pole tekstowe
            .setView(input)
            // dodaje przycisk zapisz
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // pobiera nowa nazwe i usuwa biale znaki
                val newName = input.text.toString().trim()
                // sprawdza czy nazwa nie jest pusta
                if (newName.isNotBlank()) {
                    // aktualizuje nazwe w viewmodel
                    if (paletteViewModel.updatePaletteName(palette.id, newName)) {
                        // pokazuje komunikat sukcesu
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.palette_renamed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // pokazuje komunikat o bledzie
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

    /**
     * pokazuje dialog z opcjami udostepniania palety
     */
    private fun showSharePaletteDialog(palette: Palette) {
        // tablica z opcjami udostepniania
        val options = arrayOf(
            getString(R.string.share_as_image),
            getString(R.string.share_as_text),
            getString(R.string.copy_to_clipboard)
        )

        // tworzy i pokazuje dialog wyboru
        AlertDialog.Builder(requireContext())
            // ustawia tytul dialogu
            .setTitle(getString(R.string.share_palette))
            // ustawia liste opcji
            .setItems(options) { _, which ->
                // obsluguje wybor opcji
                when (which) {
                    // opcja 0 udostepnij jako obraz
                    0 -> ShareUtils.sharePaletteAsImage(requireContext(), palette)
                    // opcja 1 udostepnij jako tekst
                    1 -> ShareUtils.sharePaletteAsText(requireContext(), palette)
                    // opcja 2 kopiuj do schowka
                    2 -> copyPaletteToClipboard(palette)
                }
            }
            // wyswietla dialog
            .show()
    }

    /**
     * kopiuje informacje o palecie do schowka systemowego
     */
    private fun copyPaletteToClipboard(palette: Palette) {
        // tworzy sformatowany tekst z wszystkimi kolorami
        val text = buildString {
            // dodaje nazwe palety
            append("Palette: ${palette.name}\n\n")
            // iteruje przez wszystkie kolory w palecie z indeksem
            palette.colors.forEachIndexed { index, color ->
                // dodaje numer i nazwe koloru
                append("${index + 1}. ${color.name}\n")
                // dodaje wartosci rgb z wcieciem
                append("   ${color.getRGBString()}\n")
                // dodaje wartosc hex z wcieciem
                append("   ${color.hexValue}\n")
                // dodaje pusta linie jezeli to nie ostatni kolor
                if (index < palette.colors.size - 1) {
                    append("\n")
                }
            }
        }
        
        // kopiuje tekst do schowka systemowego
        ShareUtils.copyToClipboard(requireContext(), "Palette", text)
        // pokazuje komunikat potwierdzajacy
        Toast.makeText(
            requireContext(),
            getString(R.string.copied_to_clipboard),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showDeletePaletteDialog(palette: Palette) {
        // tworzy i pokazuje dialog potwierdzenia
        AlertDialog.Builder(requireContext())
            // ustawia tytul
            .setTitle(getString(R.string.confirm_delete))
            // ustawia pytanie
            .setMessage(getString(R.string.confirm_delete_palette))
            // dodaje przycisk usun
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                // usuwa palete z viewmodel
                paletteViewModel.deletePalette(palette)
                // pokazuje komunikat potwierdzajacy
                Toast.makeText(
                    requireContext(),
                    getString(R.string.palette_deleted),
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
            .setTitle(getString(R.string.clear_all_palettes))
            // ustawia pytanie
            .setMessage(getString(R.string.confirm_clear_all))
            // dodaje przycisk tak
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                // czysci wszystkie palety
                paletteViewModel.clearAllPalettes()
                // pokazuje komunikat potwierdzajacy
                Toast.makeText(
                    requireContext(),
                    getString(R.string.palettes_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }
            // dodaje przycisk nie
            .setNegativeButton(getString(R.string.no), null)
            // wyswietla dialog
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // czysci binding zeby uniknac wyciekow pamieci
        _binding = null
    }
}
