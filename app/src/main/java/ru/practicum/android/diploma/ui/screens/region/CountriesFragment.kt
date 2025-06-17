package ru.practicum.android.diploma.ui.screens.region

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.bundle.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.databinding.FragmentCountriesBinding
import ru.practicum.android.diploma.ui.screens.favourites.region.AreaAdapter

class CountriesFragment : Fragment() {
    private val viewModel by viewModel<AreasViewModel>()
    private var _binding: FragmentCountriesBinding? = null
    private val binding: FragmentCountriesBinding get() = requireNotNull(_binding)
    private var countriesAdapter: AreaAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View? {
        _binding = FragmentCountriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        countriesAdapter = AreaAdapter(emptyList()) { country ->
            val bundle = Bundle().apply {
                putParcelable("selected_country", country)
            }
            parentFragmentManager.setFragmentResult("country_result", bundle)
            findNavController().popBackStack()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = countriesAdapter

        lifecycleScope.launch {
            viewModel.countries.collect { list ->
                countriesAdapter?.submitList(list)
            }
        }
        viewModel.loadCountries()

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

