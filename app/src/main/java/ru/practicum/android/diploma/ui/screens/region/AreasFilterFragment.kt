package ru.practicum.android.diploma.ui.screens.region

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.bundle.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentAreasFilterBinding
import ru.practicum.android.diploma.domain.network.models.Area
import ru.practicum.android.diploma.ui.screens.filter.FilterViewModel

@Suppress("DEPRECATION")
class AreasFilterFragment : Fragment() {
    private val viewModel by viewModel<FilterViewModel>(ownerProducer = { requireActivity() })
    private var _binding: FragmentAreasFilterBinding? = null
    private val binding: FragmentAreasFilterBinding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View? {
        _binding = FragmentAreasFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupResultListeners()
        setupClicks()
        observeState()

    }

    private fun setupResultListeners() {
        parentFragmentManager.setFragmentResultListener("country_result", viewLifecycleOwner) { _, bundle ->
            val country = bundle.getParcelable<Area>("selected_country")
            viewModel.onSelectCountry(country)
        }
        parentFragmentManager.setFragmentResultListener("region_result", viewLifecycleOwner) { _, bundle ->
            val region = bundle.getParcelable<Area>("selected_region")
            viewModel.onSelectRegion(region)
        }
    }

    private fun setupClicks() {
        binding.countryText.setOnClickListener {
            findNavController().navigate(R.id.action_areasFilterFragment_to_countriesFragment)
        }
        binding.countryImage.setOnClickListener {
            if (viewModel.uiState.value.selectedCountry != null) {
                viewModel.onSelectCountry(null)
            } else {
                findNavController().navigate(R.id.action_areasFilterFragment_to_countriesFragment)
            }
        }
        binding.regionText.setOnClickListener {
            viewModel.uiState.value.selectedCountry?.let { country ->
                val action = AreasFilterFragmentDirections.actionAreasFilterFragmentToRegionsFragment(country)
                findNavController().navigate(action)
                Log.i("LogArea", "$country")
            }
        }
        binding.regionImage.setOnClickListener {
            viewModel.uiState.value.selectedCountry?.let { country ->
                val action = AreasFilterFragmentDirections.actionAreasFilterFragmentToRegionsFragment(country)
                findNavController().navigate(action)
                Log.i("LogArea", "$country")
            }
        }
        binding.regionImage.setOnClickListener {
            if (viewModel.uiState.value.selectedRegion != null) {
                viewModel.onSelectRegion(null)
            }
        }
        binding.applyButton.setOnClickListener {
            val country = viewModel.uiState.value.selectedCountry
            val region = viewModel.uiState.value.selectedRegion
            val area = listOfNotNull(country?.name, region?.name).joinToString(", ")
            viewModel.setAreas(area)
            findNavController().popBackStack()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->

                binding.countryText.setText(state.selectedCountry?.name ?: "")
                if (state.selectedCountry != null) {
                    binding.countryImage.setImageResource(R.drawable.close_24px)
                } else {
                    binding.countryImage.setImageResource(R.drawable.outline_arrow_forward_ios_24)
                }

                binding.regionText.setText(state.selectedRegion?.name ?: "")
                if (state.selectedRegion != null) {
                    binding.regionImage.setImageResource(R.drawable.close_24px)
                } else {
                    binding.regionImage.setImageResource(R.drawable.outline_arrow_forward_ios_24)
                }

                binding.applyButton.isVisible = state.selectedCountry != null
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
