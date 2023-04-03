package by.klnvch.link5dots.data

import android.content.Context
import javax.inject.Inject

class StringProvider @Inject constructor(private val context: Context) {
    fun getString(resId: Int) = context.getString(resId)
}
