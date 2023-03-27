/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package by.klnvch.link5dots.ui.scores.scores

import android.view.View
import by.klnvch.link5dots.R

data class ScoresViewState(val firebaseState: FirebaseState, val highScorePath: String? = null) {
    val errorMsg = when (firebaseState) {
        FirebaseState.NOT_SUPPORTED -> R.string.error_feature_not_available
        FirebaseState.ERROR -> R.string.connection_error_message
        else -> R.string.loading
    }
    val errorVisibility = if (firebaseState != FirebaseState.SIGNED_IN) View.VISIBLE else View.GONE

    companion object {
        fun initial() = ScoresViewState(FirebaseState.UNKNOWN)
    }
}

enum class FirebaseState {
    UNKNOWN, NOT_SUPPORTED, SIGNED_IN, ERROR
}