package ru.practicum.android.diploma.ui.screens.region

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.interactor.VacancyInteractorImpl
import ru.practicum.android.diploma.domain.network.models.Area
import ru.practicum.android.diploma.ui.screens.filter.FilterUiState
import ru.practicum.android.diploma.ui.screens.filter.FilterViewModel.Companion.FILTER_DELAY
import java.io.IOException

class AreasViewModel(
    private val interactor: VacancyInteractorImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    private val _countries = MutableStateFlow<List<Area>>(emptyList())
    val countries: StateFlow<List<Area>> = _countries

    private var firstList: List<Area> = emptyList()
    private var filterJob: Job? = null

    fun loadCountries() {
        viewModelScope.launch {
            try {
                val areas = interactor.getAreas()
                if (areas != null) {
                    _countries.value = areas
                } else {
                    _uiState.update { it.copy(isError = true) }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isError = true) }
            }
        }
    }

    fun loadRegions(country: Area?) {
        val regions = country?.areas ?: emptyList()
        firstList = regions
        _uiState.update { it.copy(regions = regions) }
    }

    fun filterList(text: CharSequence?) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch(Dispatchers.Default) {
            delay(FILTER_DELAY)
            val filteredList = if (text.isNullOrEmpty()) {
                firstList
            } else {
                firstList.filter { it.name.contains(text, ignoreCase = true) }
            }
            val isNotFound = text?.isNotEmpty() == true && filteredList.isEmpty()
            _uiState.update { it.copy(regions = filteredList, isRegionNotFound = isNotFound) }
        }
    }

    fun onSelectRegion(region: Area?) {
        _uiState.update { it.copy(selectedRegion = region) }
    }

}
