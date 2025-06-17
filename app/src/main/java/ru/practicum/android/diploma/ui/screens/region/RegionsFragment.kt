package ru.practicum.android.diploma.ui.screens.region

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.bundle.Bundle
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentRegionBinding

class RegionsFragment : Fragment() {
    private val viewModel by viewModel<AreasViewModel>()
    private var _binding: FragmentRegionBinding? = null
    private val binding: FragmentRegionBinding get() = requireNotNull(_binding)
    private var regionAdapter: AreaAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View? {
        _binding = FragmentRegionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.loadCountries()
        setupToolbar()
        setupFilterField()
        observeIndustries()

        val args: RegionsFragmentArgs by navArgs()
        val country = args.country
        regionAdapter = AreaAdapter(emptyList()) { region ->
            val bundle = Bundle().apply {
                putParcelable("selected_region", region)
            }
            parentFragmentManager.setFragmentResult("region_result", bundle)
            binding.industryFilterField.setText(region.name)
            viewModel.onSelectRegion(region)
            findNavController().popBackStack()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = regionAdapter

        viewModel.loadRegions(country)
    }

    private fun setupFilterField() {
        binding.clearRegion.setOnClickListener {
            binding.industryFilterField.text?.clear()
            regionAdapter?.clearSelectedItem()
            viewModel.onSelectRegion(null)
        }
        binding.industryFilterField.addTextChangedListener(
            onTextChanged = { text: CharSequence?, _, _, _ ->
                viewModel.filterList(text)
                binding.clearRegion.isVisible = !text.isNullOrEmpty()
            },
        )
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()

        }
    }

    private fun observeIndustries() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                regionAdapter?.submitList(state.regions)
                binding.error.isVisible = state.isError
                if (state.isRegionNotFound) {
                    binding.error.isVisible = true
                    binding.errorText.text = getString(R.string.region_no_found)
                    binding.errorImage.setImageResource(R.drawable.image_kat)
                } else {
                    binding.error.isVisible = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
