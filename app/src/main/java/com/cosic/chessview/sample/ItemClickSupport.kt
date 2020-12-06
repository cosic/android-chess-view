package com.cosic.chessview.sample

import android.view.View
import android.view.View.OnLongClickListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener

class ItemClickSupport
private constructor(private val mRecyclerView: RecyclerView) {
    var mOnItemClickListener: ((recyclerView: RecyclerView?, position: Int, v: View?) -> Unit)? = null
    var mOnItemLongClickListener: ((recyclerView: RecyclerView?, position: Int, v: View?) -> Boolean)? = null
    private var mOnClickListener = View.OnClickListener { v ->
        if (mOnItemClickListener != null) {
            // ask the RecyclerView for the viewHolder of this view.
            // then use it to get the position for the adapter
            val holder = mRecyclerView.getChildViewHolder(v)
            mOnItemClickListener?.invoke(mRecyclerView, holder.adapterPosition, v)
        }
    }

    private val mOnLongClickListener = OnLongClickListener { v ->
        if (mOnItemLongClickListener != null) {
            val holder = mRecyclerView.getChildViewHolder(v)
            return@OnLongClickListener mOnItemLongClickListener?.invoke(mRecyclerView, holder.adapterPosition, v) ?: false
        }
        false
    }

    private val mAttachListener: OnChildAttachStateChangeListener = object : OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            // every time a new child view is attached add click listeners to it
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener)
            }
            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener)
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {}
    }

    private fun detach(view: RecyclerView) {
        view.removeOnChildAttachStateChangeListener(mAttachListener)
        view.setTag(R.id.item_click_support, null)
    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(recyclerView: RecyclerView?, position: Int, v: View?): Boolean
    }

    companion object {
        fun addTo(view: RecyclerView): ItemClickSupport {
            // if there's already an ItemClickSupport attached
            // to this RecyclerView do not replace it, use it
            var support = view.getTag(R.id.item_click_support) as? ItemClickSupport
            if (support == null) {
                support = ItemClickSupport(view)
            }
            return support
        }

        fun removeFrom(view: RecyclerView): ItemClickSupport? {
            val support = view.getTag(R.id.item_click_support) as ItemClickSupport
            support.detach(view)
            return support
        }
    }

    init {
        // the ID must be declared in XML, used to avoid
        // replacing the ItemClickSupport without removing
        // the old one from the RecyclerView
        mRecyclerView.setTag(R.id.item_click_support, this)
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener)
    }
}