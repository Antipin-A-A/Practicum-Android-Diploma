package ru.practicum.android.diploma.ui.screens.favourites.region

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ItemRegionBinding
import ru.practicum.android.diploma.domain.network.models.Area

class AreaAdapter(
    private var items: List<Area>,
    private val onClick: (Area) -> Unit
) : RecyclerView.Adapter<AreaAdapter.AreaViewHolder>() {

    private var selectedItemId: String? = null
    private var selectedItemPosition: Int? = null

    inner class AreaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemRegionBinding.bind(itemView)
        fun bind(area: Area) = with(binding) {
            regionName.text = area.name
            Log.i("LogAdap", "area.name = ${area.name}")
            regionName.setOnClickListener { onClick(area) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_region,parent,false)
        return AreaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        holder.bind(items[position])
    }
    fun clearSelectedItem() {
        val oldPosition = selectedItemPosition
        selectedItemId = null
        selectedItemPosition = null
        oldPosition?.let {
            notifyItemChanged(it)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<Area>) {
        items = newItems
        notifyDataSetChanged()
    }
}

