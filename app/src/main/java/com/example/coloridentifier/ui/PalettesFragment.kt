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

class PalettesFragment : Fragment() {

    private var _binding: FragmentPalettesBinding? = null
    private val binding get() = _binding!!
    private val paletteViewModel: PaletteViewModel by activityViewModels()
    
    private lateinit var paletteAdapter: PaletteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPalettesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupMenu()
    }

    private fun setupRecyclerView() {
        paletteAdapter = PaletteAdapter(
            palettes = emptyList(),
            onPaletteClick = { palette ->
                showPaletteDetailsDialog(palette)
            },
            onPaletteLongClick = { palette ->
                showPaletteOptionsDialog(palette)
            }
        )

        binding.palettesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = paletteAdapter
        }
    }

    private fun setupObservers() {
        paletteViewModel.palettes.observe(viewLifecycleOwner) { palettes ->
            if (palettes.isEmpty()) {
                binding.emptyTextView.visibility = View.VISIBLE
                binding.palettesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyTextView.visibility = View.GONE
                binding.palettesRecyclerView.visibility = View.VISIBLE
                paletteAdapter.updatePalettes(palettes)
            }
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.palettes_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_clear_all_palettes -> {
                        showClearAllDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showPaletteDetailsDialog(palette: Palette) {
        val message = buildString {
            append("Colors in palette:\n\n")
            palette.colors.forEachIndexed { index, color ->
                append("${index + 1}. ${color.name}\n")
                append("   ${color.hexValue} - ${color.getRGBString()}\n\n")
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(palette.name)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    private fun showPaletteOptionsDialog(palette: Palette) {
        val options = arrayOf(
            getString(R.string.rename_palette),
            getString(R.string.share_palette),
            getString(R.string.delete_palette)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(palette.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenamePaletteDialog(palette)
                    1 -> showSharePaletteDialog(palette)
                    2 -> showDeletePaletteDialog(palette)
                }
            }
            .show()
    }

    private fun showRenamePaletteDialog(palette: Palette) {
        val input = EditText(requireContext())
        input.setText(palette.name)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.rename_palette))
            .setView(input)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotBlank()) {
                    if (paletteViewModel.updatePaletteName(palette.id, newName)) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.palette_renamed),
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

    private fun showSharePaletteDialog(palette: Palette) {
        val options = arrayOf(
            getString(R.string.share_as_text),
            getString(R.string.share_as_json),
            getString(R.string.share_as_image)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.share_palette))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ShareUtils.sharePaletteAsText(requireContext(), palette)
                    1 -> ShareUtils.sharePaletteAsJson(requireContext(), palette)
                    2 -> ShareUtils.sharePaletteAsImage(requireContext(), palette)
                }
            }
            .show()
    }

    private fun showDeletePaletteDialog(palette: Palette) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete))
            .setMessage(getString(R.string.confirm_delete_palette))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                paletteViewModel.deletePalette(palette)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.palette_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showClearAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_all_palettes))
            .setMessage(getString(R.string.confirm_clear_all))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                paletteViewModel.clearAllPalettes()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.palettes_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
