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

package by.klnvch.link5dots.multiplayer.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.FragmentGamePickerBinding
import by.klnvch.link5dots.multiplayer.adapters.OnEmptyStateListener
import by.klnvch.link5dots.multiplayer.adapters.OnItemClickListener
import by.klnvch.link5dots.multiplayer.adapters.TargetAdapterInterface
import by.klnvch.link5dots.multiplayer.targets.Target
import by.klnvch.link5dots.multiplayer.utils.GameState

open class PickerFragment : Fragment(), OnItemClickListener, OnEmptyStateListener,
    OnPickerClickListener {

    protected lateinit var binding: FragmentGamePickerBinding
    protected lateinit var mListener: OnPickerListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = try {
            context as OnPickerListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnPickerListener")
        }
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGamePickerBinding.inflate(inflater, container, false)
        binding.listener = this
        binding.listTargets.setHasFixedSize(true)
        binding.listTargets.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onDestroyView() {
        binding.listTargets.adapter = null // prevent memory leak
        super.onDestroyView()
    }

    override fun onItemSelected(target: Target<*>) {
        Log.d(TAG, "onItemSelected: $target")
        val msg = getString(R.string.connection_dialog_text, target.shortName)
        AlertDialog.Builder(requireContext())
            .setMessage(msg)
            .setPositiveButton(R.string.yes) { _, _ -> mListener.onConnect(target) }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.textWarningEmpty.visibility = View.VISIBLE
        } else {
            binding.textWarningEmpty.visibility = View.INVISIBLE
        }
    }

    fun setState(state: GameState) {
        if (view == null) return

        binding.viewState = PickerViewState(state)

        when (state.targetState) {
            GameState.TargetState.NONE,
            GameState.TargetState.DELETED,
            GameState.TargetState.CREATING,
            GameState.TargetState.DELETING -> binding.textStatusValue.setText(R.string.name_not_set)
            GameState.TargetState.CREATED -> binding.textStatusValue.text =
                mListener.targetLongName
        }

        val adapter = mListener.adapter
        when (state.scanState) {
            GameState.ScanState.NONE, GameState.ScanState.OFF -> {
                adapter.setOnItemClickListener(null)
                adapter.setOnEmptyStateListener(null)
                binding.listTargets.adapter = null
                binding.textWarningEmpty.visibility = View.INVISIBLE
            }
            GameState.ScanState.ON -> {
                adapter.setOnItemClickListener(this)
                adapter.setOnEmptyStateListener(this)
                onEmptyState(adapter.isEmpty)
                binding.listTargets.adapter = adapter.adapter
            }
            GameState.ScanState.DONE -> {
                adapter.setOnItemClickListener(this)
                adapter.setOnEmptyStateListener(null)
                onEmptyState(adapter.isEmpty)
                binding.listTargets.adapter = adapter.adapter
            }
        }
    }

    override fun onCreateButtonClicked(isOn: Boolean) =
        if (isOn) mListener.onCreateRoom() else mListener.onDeleteRoom()

    override fun onScanButtonClicked(isOn: Boolean) =
        if (isOn) mListener.onStartScan() else mListener.onStopScan()


    interface OnPickerListener {
        fun onCreateRoom()
        fun onDeleteRoom()
        fun onStartScan()
        fun onStopScan()
        fun onConnect(destination: Target<*>)
        val targetLongName: String
        val adapter: TargetAdapterInterface
    }

    companion object {
        const val TAG = "OnlineGamePickerFr"
    }
}
