package ru.practicum.android.diploma.ui.screens.filter

import ru.practicum.android.diploma.domain.network.models.Area
import ru.practicum.android.diploma.domain.network.models.Industry

data class FilterUiState(
    val workIndustry: String = "",
    val workArea: String = "",
    val industries: List<Industry> = emptyList(),
    val selectedIndustry: Industry? = null,
    val salary: String = "",
    val onlyWithSalary: Boolean = false,
    val isError: Boolean = false,
    val selectedCountry: Area? = null,
    val selectedRegion: Area? = null,
    val regions: List<Area> = emptyList(),
    val isRegionNotFound: Boolean = false
)
