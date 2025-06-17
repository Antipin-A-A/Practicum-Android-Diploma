package ru.practicum.android.diploma.domain.network.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Area(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("areas") val areas: List<Area>?,
) : Parcelable
