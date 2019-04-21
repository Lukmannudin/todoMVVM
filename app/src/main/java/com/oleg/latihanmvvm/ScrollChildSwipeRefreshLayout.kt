package com.oleg.latihanmvvm

import android.content.Context
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.View

class ScrollChildSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.swiperefreshlayout.widget.SwipeRefreshLayout(context, attrs) {

    var scrollUpChild: View? = null

    override fun canChildScrollUp() =
        scrollUpChild?.canScrollVertically(-1) ?: super.canChildScrollUp()
}