package com.abs192.sudokai.views

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abs192.sudokai.BoardData
import com.abs192.sudokai.Entry
import com.abs192.sudokai.R
import com.abs192.sudokai.listeners.EntryClickListener
import com.abs192.sudokai.listeners.NumberDialogSelectedListener
import com.abs192.sudokai.storage.Storage

class RVGridNumbersViewAdapter(var context: Context?, data: BoardData) :
    RecyclerView.Adapter<RVGridNumbersViewAdapter.ViewHolder>(),
    EntryClickListener, NumberDialogSelectedListener {

    private var tag: String = this.javaClass.simpleName

    private var board: BoardData = data
    private val size: Int = board.getSize()
    private var mInflater: LayoutInflater? = null
    private var store: Storage? = null

    var isEditMode = false

    private var selectedRow: Int = 0
    private var selectedCol: Int = 0

    init {
        mInflater = LayoutInflater.from(context)
        store = context?.let { Storage(it) }
    }

    override fun onEntryClick(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        if (isEditMode) {
            val alertDialog: NumberDialog? = context?.let {
                NumberDialog(
                    it,
                    this
                )
            }
            alertDialog?.show()
        } else {
            store?.getSquareImage(row, col)?.let { showImageDialog(it) }
        }
    }

    public fun editMode(checked: Boolean) {
        isEditMode = checked
    }

    override fun numberSelected(num: Int) {
        if (num == -1) {
            board.putEmpty(selectedRow, selectedCol)
        } else {
            board.editHardCoded(selectedRow, selectedCol, num)
        }
        this.notifyDataSetChanged()
    }

    public fun showImageDialog(bitmap: Bitmap) {
        val builder = Dialog(context!!)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imageView = ImageView(context)
        imageView.setImageBitmap(bitmap)
        builder.addContentView(
            imageView, RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        builder.show()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private lateinit var listener: EntryClickListener
        private var textView: TextView = itemView.findViewById(R.id.rvGridItemTextView)
        private val colorYellow = Color.parseColor("#EFF700")
        private val colorBlack = Color.parseColor("#121212")
        private var row: Int? = null
        private var col: Int? = null


        init {
            itemView.setOnClickListener(this)
            val context = itemView.context
        }

        override fun onClick(p0: View?) {
            listener.onEntryClick(row!!, col!!)
        }


        fun set(row: Int, col: Int, s: String, type: Entry.Type) {
            this.row = row
            this.col = col
            textView.text = s
            when (type) {
                Entry.Type.HARDCODED -> {
                    textView.setTextColor(colorBlack)
                    textView.setTypeface(textView.typeface, Typeface.BOLD)
                }
//                Entry.Type.MARKER ->
//                    textView.setTextColor(colorRed)
                Entry.Type.SOLUTION -> {
                    textView.setTextColor(colorYellow)
                    textView.setTypeface(textView.typeface, Typeface.NORMAL)
                }
            }
        }


        fun setEntryClickListener(listener: EntryClickListener) {
            this.listener = listener
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = ViewHolder(
            mInflater!!.inflate(
                R.layout.layout_rvgrid_numbers_item,
                parent,
                false
            )
        )
        v.setEntryClickListener(this)
        return v
    }

    override fun getItemCount(): Int {
        return size * size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = position / size
        val col = position % size
        val num = board.getNum(row, col)
        val type = board.getType(row, col)
        if (board.isEmpty(row, col)) {
            // do empty
            holder.set(row, col, " ", type)
        } else {
            num.let { holder.set(row, col, it.toString(), type) }
        }
    }

}