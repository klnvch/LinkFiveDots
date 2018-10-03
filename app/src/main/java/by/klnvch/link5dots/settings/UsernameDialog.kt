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

package by.klnvch.link5dots.settings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import by.klnvch.link5dots.R
import dagger.android.support.DaggerAppCompatDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UsernameDialog : DaggerAppCompatDialogFragment(), DialogInterface.OnClickListener {
    private val mDisposables = CompositeDisposable()
    @Inject
    internal lateinit var settingsUtils: SettingsUtils
    private lateinit var mEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = View.inflate(activity, R.layout.dialog_username, null)

        mEditText = v.findViewById(R.id.username)

        return AlertDialog.Builder(context!!)
                .setCancelable(false)
                .setPositiveButton(R.string.okay, this)
                .setNegativeButton(R.string.cancel, null)
                .setView(v)
                .create()
    }

    override fun onStart() {
        super.onStart()

        mDisposables.add(settingsUtils.userNameOrDefault
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.setUsername(it) })
    }

    override fun onDestroy() {
        mDisposables.clear()
        super.onDestroy()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val listener = context as OnUsernameChangedListener

        val username = mEditText.text.toString().trim { it <= ' ' }
        if (!username.isEmpty()) {
            settingsUtils.setUserName(username)
            listener.onUsernameChanged(username)
        }
    }

    private fun setUsername(username: String) {
        mEditText.setText(username)
        mEditText.setSelection(username.length)
    }

    interface OnUsernameChangedListener {
        fun onUsernameChanged(username: String)
    }

    companion object {
        const val TAG = "UsernameDialog"
    }
}