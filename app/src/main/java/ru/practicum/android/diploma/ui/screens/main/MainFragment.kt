package ru.practicum.android.diploma.ui.screens.main

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentMainBinding
import ru.practicum.android.diploma.domain.OnItemClickListener
import ru.practicum.android.diploma.domain.network.models.FilterSettings
import ru.practicum.android.diploma.domain.network.models.VacancyDetails
import ru.practicum.android.diploma.util.AppFormatters
import ru.practicum.android.diploma.util.debounce

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding get() = requireNotNull(_binding)
    private var adapter: VacancyAdapter? = null
    private val viewModel by viewModel<MainViewModel>()
    private var isClickAllowed = true
    private var filterMenuItem: MenuItem? = null
    private var isToastAllowed = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        observeViewModel()
        observeFilterSettings()
    }

    private fun setupToolbar() {
        filterMenuItem = binding.toolbar.menu.findItem(R.id.action_filter)
        viewModel.currentFilterSettings?.let { updateFilterIcon(isFilterApplied(it)) }

        binding.toolbar.setOnMenuItemClickListener {
            val action = MainFragmentDirections.actionHomeFragmentToFilterFragment()
            findNavController().navigate(action)
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val onItemClickListener = OnItemClickListener<VacancyDetails> { vacancy ->
            if (clickDebounce()) {
                val action = MainFragmentDirections.actionHomeFragmentToDetailsFragment(vacancy.id)
                findNavController().navigate(action)
            }
        }

        binding.recyclerView.apply {
            this@MainFragment.adapter = VacancyAdapter(onItemClickListener)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MainFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((viewModel.isLoading.value == true).not() &&
                        visibleItemCount + firstVisibleItemPosition >= totalItemCount &&
                        firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadMoreItems()
                    }
                }
            })
        }
    }

    private fun setupSearchView() {
        binding.buttonCleanSearch.setOnClickListener {
            binding.searchView.setText("")
            viewModel.lastSearchQuery = ""
            viewModel.clearSearchResults()
            val inputMethodManager =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.buttonCleanSearch.windowToken, 0)
            binding.errorMessage.isVisible = false
            binding.imageStart.isVisible = true
            binding.infoSearch.isVisible = false
        }

        binding.searchView.setText(viewModel.lastSearchQuery ?: "")
        updateClearButtonVisibility()
        val debouncedSearch = debounce(
            delayMillis = CLICK_DEBOUNCE_DELAY,
            coroutineScope = viewLifecycleOwner.lifecycleScope,
            useLastParam = true
        ) { query: String ->
            if (query.isBlank()) {
                binding.recyclerView.isVisible = false
                binding.errorMessage.isVisible = false
                binding.imageStart.isVisible = true
                adapter?.submitList(emptyList())
            } else if (query != viewModel.lastSearchQuery) {
                viewModel.lastSearchQuery = query
                viewModel.searchVacancies(query)
            }
        }

        binding.searchView.addTextChangedListener(
            onTextChanged = { p0: CharSequence?, _, _, _ ->
                debouncedSearch(p0?.toString() ?: "")
                updateClearButtonVisibility()

            },
            afterTextChanged = { _: Editable? ->
                binding.errorMessage.isVisible = false
            }
        )
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.isVisible = adapter?.currentList.isNullOrEmpty()
                    binding.imageStart.isVisible = false
                    binding.infoSearch.isVisible = false
                }

                is UiState.Content -> {
                    binding.recyclerView.isVisible = true
                    binding.errorMessage.isVisible = false
                    binding.imageStart.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.infoSearch.isVisible = true
                    binding.infoSearch.text = AppFormatters.vacanciesCountTextFormatter(
                        context = requireContext(),
                        count = state.allCount.toInt()
                    )
                    adapter?.submitList(state.vacancies)
                }

                is UiState.NotFound -> {
                    showMessage(getString(R.string.empty_search), R.drawable.image_kat)
                    binding.infoSearch.text = getString(R.string.no_such_vacancies)
                    binding.infoSearch.isVisible = true
                }

                is UiState.Error -> {
                    if (isToastAllowed) {
                        isToastAllowed = false
                        if (state.vacancies.isEmpty()) {
                            showMessage(getString(R.string.no_internet), R.drawable.image_skull)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.check_your_internet_connection),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.i("errors", "no internet toast")
                            lifecycleScope.launch {
                                delay(TOAST_DEBOUNCE_DELAY)
                                isToastAllowed = true
                            }
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.check_your_internet_connection),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    binding.infoSearch.isVisible = false
                    binding.progressBar.isVisible = false
                }

                is UiState.Idle -> {
                    binding.recyclerView.isVisible = false
                    binding.errorMessage.isVisible = false
                    binding.imageStart.isVisible = true
                    binding.infoSearch.isVisible = false
                    adapter?.submitList(emptyList())
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            val hasContent = adapter?.currentList?.isNotEmpty() == true
            binding.bottomProgressBar.visibility =
                if (isLoading && hasContent) View.VISIBLE else View.GONE
        }
    }

    private fun showMessage(text: String, drawable: Int) =
        with(binding) {
            imageStart.isVisible = false
            progressBar.isVisible = false
            recyclerView.isVisible = false
            imageView.setImageResource(drawable)
            if (text.isNotEmpty()) {
                errorMessage.isVisible = true
                adapter?.submitList(emptyList())
                errorText.text = text
            } else {
                errorMessage.isVisible = false
            }
        }

    private fun clickDebounce(): Boolean {
        if (isClickAllowed) {
            isClickAllowed = false
            viewLifecycleOwner.lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return true
    }

    private fun updateClearButtonVisibility() {
        val searchDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.search_24px)
        if (binding.searchView.text.isEmpty()) {
            binding.searchView.setCompoundDrawablesWithIntrinsicBounds(null, null, searchDrawable, null)
            binding.buttonCleanSearch.isVisible = false
            binding.imageStart.isVisible = true
        } else {
            binding.searchView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.buttonCleanSearch.isVisible = !binding.searchView.text.isNullOrEmpty()
        }
    }

    private fun observeFilterSettings() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<FilterSettings>("filter_settings")
            ?.observe(viewLifecycleOwner) { filterSettings ->
                filterSettings?.let {
                    viewModel.currentFilterSettings = it
                    val isApplied = isFilterApplied(it)
                    updateFilterIcon(isApplied)
                    viewModel.searchVacancies(
                        query = viewModel.lastSearchQuery ?: "",
                        isNewSearch = true,
                        filterSettings = filterSettings
                    )
                }
            }
    }

    private fun isFilterApplied(filter: FilterSettings): Boolean {
        return !filter.salary.isNullOrBlank() ||
            filter.selectedIndustry != null ||
            filter.onlyWithSalary!! ||
            filter.area != null
    }

    private fun updateFilterIcon(isApplied: Boolean) {
        val iconRes = if (isApplied) {
            R.drawable.filter_on_24px // активный фильтр
        } else {
            R.drawable.filter_off__24px // неактивный фильтр
        }
        filterMenuItem?.icon = AppCompatResources.getDrawable(requireContext(), iconRes)
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val TOAST_DEBOUNCE_DELAY = 3000L
    }

}
