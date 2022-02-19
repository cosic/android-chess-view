package com.cosic.chessview.sample

import android.app.Activity
import android.view.View
import android.view.ViewStub
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

fun <T> RecyclerView.ViewHolder.bindView(@IdRes res: Int): Lazy<T> = lazy { itemView.findViewById<View>(res) as T }

fun <T> RecyclerView.ViewHolder.bindViewOptional(@IdRes res: Int): Lazy<T?> = lazy {
    itemView.findViewById<View?>(res) as T
}

fun <T> Activity.bindView(@IdRes res: Int): Lazy<T> = lazy { findViewById<View>(res) as T }

fun <T> Activity.bindViewOptional(@IdRes res: Int): Lazy<T?> = lazy { findViewById<View?>(res) as T? }

fun <T> View.bindView(@IdRes res: Int, block: (T.() -> Unit)? = null): Lazy<T> {
    return lazy {
        val view = findViewById<View>(res) as T
        block?.invoke(view)
        return@lazy view
    }
}

fun <T> View.bindViewByStub(
    @IdRes stubResId: Int,
    initializer: ((view: T) -> Unit)? = null
): Lazy<T> = lazy {
    val viewStub = findViewById<ViewStub>(stubResId)
    val result = viewStub.inflate() as T
    initializer?.invoke(result)
    result
}

fun <T> View.bindViewByStubOptional(
    @IdRes stubResId: Int,
    initializer: ((view: T?) -> Unit)? = null
): Lazy<T?> = lazy {
    val viewStub = findViewById<ViewStub?>(stubResId)
    val result = viewStub?.let { it.inflate() as T }
    initializer?.invoke(result)
    result
}

fun <T> View.bindViewOptional(@IdRes res: Int): Lazy<T?> = lazy { findViewById<View>(res) as T }
