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

package by.klnvch.link5dots.ui.menu

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.klnvch.link5dots.R
import dagger.android.support.DaggerAppCompatDialogFragment
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class UsernameDialog : DaggerAppCompatDialogFragment(), DialogInterface.OnClickListener {

    private lateinit var viewModel: MainMenuViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = View.inflate(activity, R.layout.dialog_username, null)
        mEditText = v.findViewById(R.id.username)

        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[MainMenuViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.map { it.userName }.collect { userName ->
                    mEditText.setText(userName)
                    mEditText.setSelection(userName.length)
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setPositiveButton(R.string.okay, this)
            .setNegativeButton(R.string.cancel, null)
            .setView(v)
            .create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            viewModel.setUserName(mEditText.text.toString())
        }
    }

    companion object {
        const val TAG = "UsernameDialog"
    }
}
