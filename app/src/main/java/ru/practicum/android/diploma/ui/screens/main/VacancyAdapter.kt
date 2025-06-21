package ru.practicum.android.diploma.ui.screens.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import format
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ItemVacancyBinding
import ru.practicum.android.diploma.domain.OnItemClickListener
import ru.practicum.android.diploma.domain.network.models.VacancyDetails
import ru.practicum.android.diploma.util.AppFormatters.getCurrencyIcon

class VacancyAdapter(private val onItemClickListener: OnItemClickListener<VacancyDetails>) :
    ListAdapter<VacancyDetails, VacancyAdapter.VacancyViewHolder>(VacancyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyViewHolder {
        val binding = ItemVacancyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VacancyViewHolder(binding, onItemClickListener)
    }

    override fun onBindViewHolder(holder: VacancyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VacancyViewHolder(
        private val binding: ItemVacancyBinding,
        private val onItemClickListener: OnItemClickListener<VacancyDetails>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vacancyDetails: VacancyDetails) {
            val value = vacancyDetails.salary
            binding.apply {
                jobTitle.text = buildString {
                    append(vacancyDetails.name)
                    append(", ")
                    append(vacancyDetails.area.name)
                }
                companyName.text = vacancyDetails.employer.name

                salary.text = value.format(
                    getString = { resId, args -> itemView.context.getString(resId, *args) },
                    getCurrencyIcon = ::getCurrencyIcon
                )

                Glide.with(itemView)
                    .load(vacancyDetails.employer.logoUrls?.original)
                    .placeholder(R.drawable.empty_image)
                    .centerCrop()
                    .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.indent_12dp)))
                    .into(image)

                root.setOnClickListener {
                    onItemClickListener.onItemClick(vacancyDetails)
                }
            }
        }
    }

    private class VacancyDiffCallback : DiffUtil.ItemCallback<VacancyDetails>() {
        override fun areItemsTheSame(oldItem: VacancyDetails, newItem: VacancyDetails): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VacancyDetails, newItem: VacancyDetails): Boolean {
            return oldItem == newItem
        }
    }
}
