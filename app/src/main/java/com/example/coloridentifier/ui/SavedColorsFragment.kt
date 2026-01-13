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

class SavedColorsFragment : Fragment() {

    private var _binding: FragmentSavedColorsBinding? = null
    private val binding get() = _binding!!
    private val colorViewModel: ColorViewModel by activityViewModels()
    private val paletteViewModel: PaletteViewModel by activityViewModels()
    
    private lateinit var colorAdapter: ColorAdapter
    private var isSelectionMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedColorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupMenu()
    }

    private fun setupRecyclerView() {
        colorAdapter = ColorAdapter(
            colors = emptyList(),
            onColorClick = { color ->
                if (isSelectionMode) {
                    colorViewModel.toggleColorSelection(color)
                } else {
                    showColorOptionsDialog(color)
                }
            },
            onColorLongClick = { color ->
                if (!isSelectionMode) {
                    startSelectionMode()
                    colorViewModel.toggleColorSelection(color)
                }
            },
            isColorSelected = { color ->
                colorViewModel.isColorSelected(color)
            }
        )

        binding.colorsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = colorAdapter
        }
    }

    private fun setupObservers() {
        colorViewModel.colors.observe(viewLifecycleOwner) { colors ->
            if (colors.isEmpty()) {
                binding.emptyTextView.visibility = View.VISIBLE
                binding.colorsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyTextView.visibility = View.GONE
                binding.colorsRecyclerView.visibility = View.VISIBLE
                colorAdapter.updateColors(colors)
            }
        }

        colorViewModel.selectedColors.observe(viewLifecycleOwner) { selectedColors ->
            updateSelectionUI(selectedColors.size)
            colorAdapter.notifyDataSetChanged()
        }
    }

    private fun setupClickListeners() {
        binding.createPaletteButton.setOnClickListener {
            showCreatePaletteDialog()
        }

        binding.cancelSelectionButton.setOnClickListener {
            exitSelectionMode()
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                if (!isSelectionMode) {
                    menuInflater.inflate(R.menu.saved_colors_menu, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_clear_all -> {
                        showClearAllDialog()
                        true
                    }
                    R.id.action_select_colors -> {
                        startSelectionMode()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun startSelectionMode() {
        isSelectionMode = true
        updateSelectionUI(0)
        requireActivity().invalidateOptionsMenu()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        colorViewModel.clearSelection()
        binding.actionButtonsLayout.visibility = View.GONE
        requireActivity().invalidateOptionsMenu()
    }

    private fun updateSelectionUI(count: Int) {
        if (isSelectionMode && count > 0) {
            binding.actionButtonsLayout.visibility = View.VISIBLE
            binding.selectionCountText.text = getString(R.string.selected_count, count)
        } else if (!isSelectionMode) {
            binding.actionButtonsLayout.visibility = View.GONE
        }
    }

    private fun showColorOptionsDialog(color: Color) {
        val options = arrayOf(
            getString(R.string.copy_hex),
            getString(R.string.copy_rgb),
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
                        showShareOptionsDialog(color)
                    }
                    3 -> {
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
