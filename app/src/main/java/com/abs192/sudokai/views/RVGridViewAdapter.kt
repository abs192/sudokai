package com.abs192.sudokai.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abs192.sudokai.R
import com.abs192.sudokai.listeners.EntryClickListener
import com.abs192.sudokai.listeners.NumberDialogSelectedListener

class RVGridViewAdapter(var context: Context?, var size: Int) :
    RecyclerView.Adapter<RVGridViewAdapter.ViewHolder>() {

    private var tag: String = this.javaClass.simpleName

    private var mInflater: LayoutInflater? = null

    public var isEditMode = false

    private var selectedRow: Int = 0
    private var selectedCol: Int = 0

    init {
        mInflater = LayoutInflater.from(context)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var gridCanvas: GridCanvas = itemView.findViewById(R.id.gridCanvas)

        init {
            val context = itemView.context
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            mInflater!!.inflate(
                R.layout.layout_rvgrid_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return size * size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = position / size
        val col = position % size
        holder.gridCanvas.setPosition(row, col)
    }

}