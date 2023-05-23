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
package by.klnvch.link5dots.ui.game.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.FragmentGamePickerBinding
import by.klnvch.link5dots.ui.game.OfflineGameViewModel
import by.klnvch.link5dots.ui.game.OnlineGameViewModel
import by.klnvch.link5dots.ui.game.picker.adapters.OnPickerItemSelected
import by.klnvch.link5dots.ui.game.picker.adapters.PickerAdapter
import by.klnvch.link5dots.ui.game.picker.adapters.PickerItemViewState
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

open class PickerFragment : DaggerFragment(), OnPickerClickListener, OnPickerItemSelected {

    protected lateinit var binding: FragmentGamePickerBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: OnlineGameViewModel

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[OfflineGameViewModel.KEY, OnlineGameViewModel::class.java]

        binding = FragmentGamePickerBinding.inflate(inflater, container, false)
        binding.listener = this
        binding.listTargets.setHasFixedSize(true)
        binding.listTargets.layoutManager = LinearLayoutManager(context)
        binding.listTargets.adapter = PickerAdapter(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pickerUiState.collect { binding.viewState = it }
            }
        }
    }

    override fun onDestroyView() {
        binding.listTargets.adapter = null // prevent memory leak
        super.onDestroyView()
    }

    override fun onPickerItemSelected(viewState: PickerItemViewState) {
        val msg = getString(R.string.connection_dialog_text, viewState.shortName)
        AlertDialog.Builder(requireContext())
            .setMessage(msg)
            .setPositiveButton(R.string.yes) { _, _ -> viewModel.connect(viewState.descriptor) }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onCreateButtonClicked(isOn: Boolean) {
        if (isOn) {
            viewModel.createRoom()
        } else {
            val viewState = binding.viewState
            if (viewState != null && viewState.targetState is TargetCreated) {
                viewModel.deleteRoom(viewState.targetState.itemViewState.descriptor)
            }
        }
    }

    override fun onScanButtonClicked(isOn: Boolean) =
        if (isOn) viewModel.startScan() else viewModel.stopScan()

    companion object {
        const val TAG = "OnlineGamePickerFr"
    }
}
