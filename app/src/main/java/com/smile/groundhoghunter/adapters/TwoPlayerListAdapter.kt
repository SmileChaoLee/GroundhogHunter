package com.smile.groundhoghunter.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smile.groundhoghunter.R
import com.smile.smilelibraries.utilities.ScreenUtil

class TwoPlayerListAdapter(
    dataMap: LinkedHashMap<String, String>,
    private val textFontSize: Float,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TwoPlayerListAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "TwoPlayerAdapter"
    }

    private var selectedPosition: Int = -1

    interface OnItemClickListener {
        fun onItemClick(position: Int, key: String, value: String)
    }

    private var dataMap: LinkedHashMap<String, String> = LinkedHashMap(dataMap)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.player_list_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder.position = $position")
        val entry = dataMap.entries.elementAt(position)
        holder.apply {
            playerNameTextView.text = entry.value
            ScreenUtil.resizeTextSize(playerNameTextView, textFontSize)
            itemView.setOnClickListener {
                listener.onItemClick(position, entry.key, entry.value)
            }
            Log.d(TAG, "onBindViewHolder.selectedPosition = $selectedPosition")
            if (position == selectedPosition) {
                playerNameTextView.setBackgroundColor(Color.RED)
            } else {
                playerNameTextView.setBackgroundColor(Color.argb(0x0, 0x0, 0x0, 0x0))
            }
        }
    }

    override fun getItemCount(): Int = dataMap.size

    fun addItem(key: String, value: String) {
        Log.d(TAG, "addItem.key = $key, value = $value")
        dataMap[key] = value
        notifyItemInserted(dataMap.size - 1)
    }

    fun removeItem(key: String) {
        Log.d(TAG, "removeItem")
        val position = dataMap.keys.indexOf(key)
        if (position >= 0) {
            dataMap.remove(key)
            notifyItemRemoved(position)
        }
    }

    fun myNotifyItemChanged(position: Int) {
        Log.d(TAG, "myNotifyItemChanged.selectedPosition = $selectedPosition")
        val prevPosition = selectedPosition
        selectedPosition = position          // update state BEFORE notifying
        if (prevPosition != -1) {
            notifyItemChanged(prevPosition)  // tells old item to redraw (will be transparent)
        }
        notifyItemChanged(position)          // tells new item to redraw (will be RED)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: LinkedHashMap<String, String>) {
        Log.d(TAG, "updateData")
        dataMap = LinkedHashMap(newData)
        selectedPosition = -1
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        Log.d(TAG, "clear")
        dataMap.clear()
        selectedPosition = -1
        notifyDataSetChanged()
    }
}
