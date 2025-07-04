package ru.practicum.android.diploma.domain

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.data.db.entity.VacancyEntity
import ru.practicum.android.diploma.domain.network.models.Area
import ru.practicum.android.diploma.domain.network.models.VacancyDetails
import ru.practicum.android.diploma.util.Resource

interface VacancyRepository {
    fun searchVacancy(
        query: String,
        page: Int,
        industry: String? = null,
        salary: Int? = null,
        onlyWithSalary: Boolean? = null,
        area: String? = null
    ): Flow<Triple<List<VacancyDetails>?, String?, String?>>
    suspend fun getVacancyDetails(id: String): Flow<Resource<VacancyDetails>>
    suspend fun addToFavorites(vacancy: VacancyEntity)
    suspend fun removeFromFavorites(vacancyId: Int)
    suspend fun getFavoriteVacancy(vacancyId: Int): VacancyEntity?
    fun getAllFavoriteVacancy(): Flow<List<VacancyDetails>>
    suspend fun getAreas(): List<Area>?
}
