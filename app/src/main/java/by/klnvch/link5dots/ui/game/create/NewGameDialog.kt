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
package by.klnvch.link5dots.ui.game.create

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.DialogNewGameBinding
import by.klnvch.link5dots.ui.game.OfflineGameViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class NewGameDialog : DaggerDialogFragment(), DialogInterface.OnClickListener {
    private lateinit var binding: DialogNewGameBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: OfflineGameViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[OfflineGameViewModel.KEY, OfflineGameViewModel::class.java]

        binding = DialogNewGameBinding.inflate(requireActivity().layoutInflater)
        binding.viewState = viewModel.getNewGameViewState()

        return AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setPositiveButton(R.string.okay, this)
            .setNegativeButton(R.string.cancel, this)
            .setView(binding.root)
            .create()
    }

    override fun onClick(dialogInterface: DialogInterface, i: Int) {
        when (i) {
            DialogInterface.BUTTON_POSITIVE -> processInput()
            DialogInterface.BUTTON_NEGATIVE -> viewModel.newGame(null)
        }
    }

    private fun processInput() {
        try {
            val seed = binding.editTextSeed.text.toString().toLong()
            viewModel.newGame(seed)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    companion object {
        const val TAG = "NewGameDialog"
    }
}
