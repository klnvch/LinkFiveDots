/*
 * MIT License
 *
 * Copyright (c) 2017 klnvch
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

package by.klnvch.link5dots.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import by.klnvch.link5dots.R
import com.crashlytics.android.Crashlytics
import java.util.*

class NewGameDialog : DialogFragment(), DialogInterface.OnClickListener {

    private lateinit var mEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = View.inflate(context, R.layout.dialog_new_game, null)

        mEditText = v.findViewById(R.id.editTextSeed)

        val seed = Random().nextInt(0xFFFF)
        val seedStr = seed.toString()
        mEditText.setText(seedStr)
        mEditText.setSelection(seedStr.length)

        return AlertDialog.Builder(context!!)
                .setCancelable(false)
                .setPositiveButton(R.string.okay, this)
                .setNegativeButton(R.string.cancel, this)
                .setView(v)
                .create()
    }

    override fun onClick(dialogInterface: DialogInterface, i: Int) {
        val listener = context as OnSeedNewGameListener

        when (i) {
            DialogInterface.BUTTON_POSITIVE -> {
                val seedStr = mEditText.text.toString()
                try {
                    val seed = seedStr.toLong()
                    listener.onSeedNewGame(seed)
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                }

            }
            DialogInterface.BUTTON_NEGATIVE -> listener.onSeedNewGame(null)
        }
    }

    interface OnSeedNewGameListener {
        fun onSeedNewGame(seed: Long?)
    }

    companion object {
        const val TAG = "NewGameDialog"
    }
}