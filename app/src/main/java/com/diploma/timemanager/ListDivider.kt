package com.diploma.timemanager

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration

class ListDivider(context: Context) : DividerItemDecoration(context, VERTICAL) {
    init {
        val drawable = ContextCompat.getDrawable(context, R.drawable.list_divider)
        drawable?.let { setDrawable(it) }
    }
}