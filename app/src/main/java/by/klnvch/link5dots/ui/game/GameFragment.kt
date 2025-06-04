/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
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
package by.klnvch.link5dots.ui.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.GameBoardBinding
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.repositories.Analytics
import by.klnvch.link5dots.domain.usecases.ActionAvailability
import by.klnvch.link5dots.models.GameViewState
import by.klnvch.link5dots.ui.game.GameView.OnMoveDoneListener
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameFragment : DaggerFragment(), OnMoveDoneListener, MenuProvider {
    private lateinit var binding: GameBoardBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: OfflineGameViewModel

    internal lateinit var onNewGameClickListener: OnNewGameClickListener

    @Inject
    lateinit var analytics: Analytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = GameBoardBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[OfflineGameViewModel.KEY, OfflineGameViewModel::class.java]

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onNewGameClickListener = context as OnNewGameClickListener
        } catch (_: ClassCastException) {
            throw ClassCastException(("$context must implement NoticeDialogListener"))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gameView.setOnMoveDoneListener(this)

        if (savedInstanceState != null) {
            binding.gameView.viewState =
                GameViewState.fromJson(savedInstanceState.getString(KEY_VIEW_STATE))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.viewState = it
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.menuUi.collect {
                    requireActivity().invalidateMenu()
                }
            }
        }

        viewModel.focusEvent.observe(viewLifecycleOwner) {
            binding.gameView.focus()
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_VIEW_STATE, binding.gameView.viewState.toJson())
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val TAG = "GameFragment"
        private const val KEY_VIEW_STATE = "KEY_VIEW_STATE"
    }

    override fun onMoveDone(dot: Point) = viewModel.addDot(dot)

    override fun onPrepareMenu(menu: Menu) {
        val menuViewState = viewModel.menuUi.value

        val newGameItem = menu.findItem(R.id.menu_new_game)
        when (menuViewState.newGameAvailability) {
            ActionAvailability.Gone -> {
                newGameItem.isVisible = false
                newGameItem.isEnabled = false
            }

            ActionAvailability.Disabled -> {
                newGameItem.isVisible = true
                newGameItem.isEnabled = false
            }

            ActionAvailability.Available -> {
                newGameItem.isVisible = true
                newGameItem.isEnabled = true
            }
        }

        menu.findItem(R.id.menu_undo).isVisible = menuViewState.isUndoSupported
        menu.findItem(R.id.menu_undo).isEnabled = menuViewState.isUndoAvailable
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_game, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.menu_search -> {
            viewModel.focus()
            true
        }

        R.id.menu_undo -> {
            viewModel.undoLastMove()
            true
        }

        R.id.menu_new_game -> {
            onNewGameClickListener.onNewGameClicked()
            true
        }

        else -> true
    }
}
