package com.varsitycollege.birdvue.data

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.birdvue.R

class HotspotAdapter(private val hotspotList: List<Hotspot>): RecyclerView.Adapter<HotspotAdapter.HotspotViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotspotViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.hotspot_item, parent, false)
        return HotspotViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HotspotViewHolder, position: Int) {
        val currentItem = hotspotList[position]

        holder.title.text = currentItem.locName
        holder.species.text = "Species observed: ${currentItem.numSpeciesAllTime}"
    }

    override fun getItemCount(): Int {
        return hotspotList.size
    }

    class HotspotViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.hotspotTitle)
        val species: TextView = itemView.findViewById(R.id.speciesObserved)
    }

}