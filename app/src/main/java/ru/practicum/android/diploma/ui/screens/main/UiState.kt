package ru.practicum.android.diploma.ui.screens.main

import ru.practicum.android.diploma.domain.network.models.VacancyDetails

sealed interface UiState {

    object Idle : UiState

    object Loading : UiState

    data class Content(
        val vacancies: List<VacancyDetails>,
        val allCount: String
    ) : UiState

    object NotFound : UiState

    data class Error(
        val vacancies: List<VacancyDetails>,
        val errorMessage: String
    ) : UiState

}
