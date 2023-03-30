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
package by.klnvch.link5dots.ui.game.end

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.DialogEndGameBinding
import by.klnvch.link5dots.ui.game.OfflineGameViewModel
import by.klnvch.link5dots.ui.game.create.NewGameDialog
import by.klnvch.link5dots.ui.scores.ScoresActivity
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class EndGameDialog : DaggerDialogFragment(), DialogInterface.OnClickListener {
    private lateinit var binding: DialogEndGameBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: OfflineGameViewModel by activityViewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        binding = DialogEndGameBinding.inflate(requireActivity().layoutInflater)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewState = viewModel.getEndGameViewState()
        binding.viewState = viewState

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(if (viewState.title != null) getString(viewState.title) else null)
            .setPositiveButton(R.string.new_game, this)
            .setNegativeButton(R.string.undo, this)
        if (viewState.isShareable) {
            builder.setNeutralButton(R.string.share, this)
        }
        return builder.show()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> moveToGameCreation()
            DialogInterface.BUTTON_NEGATIVE -> viewModel.undoLastMove()
            DialogInterface.BUTTON_NEUTRAL -> moveToScores()
        }
    }

    private fun moveToGameCreation() {
        NewGameDialog().show(parentFragmentManager, NewGameDialog.TAG)
    }

    private fun moveToScores() {
        viewModel.saveScore()
        startActivity(Intent(requireContext(), ScoresActivity::class.java))
    }

    companion object {
        const val TAG = "EndGameDialog"
    }
}
