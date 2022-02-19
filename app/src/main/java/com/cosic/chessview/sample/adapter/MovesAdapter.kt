package com.cosic.chessview.sample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cosic.chessview.sample.R
import com.cosic.chessview.sample.bindView

const val NO_POSITION = -1

class MovesAdapter : RecyclerView.Adapter<MovesAdapter.ViewHolder>() {

    var onSelectedItemChange: ((newPosition: Int, oldPosition: Int) -> Unit)? = null

    private val moveItems = mutableListOf<MoveItem>()

    private val items: List<MoveItem>
        get() = moveItems

    val selectedPosition: Int
        get() {
            val items = items
            for (i in items.indices) {
                if (items[i].isSelected) {
                    return i
                }
            }
            return NO_POSITION
        }

    fun setSelection(position: Int) {
        val items = items
        if (items.isEmpty()) return
        val selectedPosition = selectedPosition
        if (selectedPosition != position) {
            items[selectedPosition].isSelected = false
            notifyItemChanged(selectedPosition)
            items[position].isSelected = true
            notifyItemChanged(position)
        }
            onSelectedItemChange?.invoke(position, selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.move_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.mStep.visibility = if (position % 2 == 0) View.VISIBLE else View.GONE
        viewHolder.mStep.text = (position / 2 + 1).toString()
        viewHolder.mMove.text = item.san
        viewHolder.mRoot.background = if (item.isSelected) {
            ContextCompat.getDrawable(viewHolder.mRoot.context, R.drawable.sp_rect_accent)
        } else {
            null
        }
        if (item.san.contains("O-O")) {
            viewHolder.mFigure.visibility = View.GONE
        } else {
            val cell = item.cell
            if (cell != null) {
                viewHolder.mFigure.setImageResource(cell.drawable)
            }
            viewHolder.mFigure.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return moveItems.size
    }

    fun getItem(position: Int): MoveItem {
        return moveItems[position]
    }

    override fun getItemId(position: Int): Long {
        return moveItems[position].num.toLong()
    }

    fun addAll(list: List<MoveItem>) {
        moveItems.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mRoot by bindView<ViewGroup>(R.id.board_move_item_root)
        val mStep by bindView<TextView>(R.id.board_move_item_step)
        val mFigure by bindView<ImageView>(R.id.board_move_item_figure)
        val mMove by bindView<TextView>(R.id.board_move_item_cell)
    }
}