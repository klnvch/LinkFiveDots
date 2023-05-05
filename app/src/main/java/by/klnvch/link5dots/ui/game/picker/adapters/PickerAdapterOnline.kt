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
package by.klnvch.link5dots.ui.game.picker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.klnvch.link5dots.R
import by.klnvch.link5dots.data.firebase.OnlineRoomMapper
import by.klnvch.link5dots.data.firebase.OnlineRoomRemote
import by.klnvch.link5dots.databinding.ItemRoomBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import javax.inject.Inject

class PickerAdapterOnline @Inject constructor(
    options: FirebaseRecyclerOptions<OnlineRoomRemote>,
    private val mapper: OnlineRoomMapper,
) : FirebaseRecyclerAdapter<OnlineRoomRemote, PickerAdapterOnline.RoomHolder>(options),
    OnPickerItemSelected, OnlineRoomScanner {

    var itemClickListener: OnPickerItemSelected? = null
    var emptyStateListener: OnEmptyStateListener? = null

    override fun onBindViewHolder(holder: RoomHolder, position: Int, remoteRoom: OnlineRoomRemote) {
        val key = getRef(position).key
        if (key != null) {
            val room = mapper.map(key, remoteRoom)
            val userName =
                room.user1.name.ifEmpty { holder.binding.root.context.getString(R.string.unknown) }
            holder.binding.viewState = PickerItemViewState(room.key, room.timestamp, userName)
            holder.binding.listener = this
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomHolder(binding)
    }

    override fun onDataChanged() {
        emptyStateListener?.onEmptyState(itemCount == 0)
    }

    override fun onError(e: DatabaseError) {
    }

    override fun startScan() = startListening()

    override fun stopScan() {
        val size = snapshots.size
        stopListening()
        notifyItemRangeRemoved(0, size)
        emptyStateListener?.onEmptyState(false)
    }

    override fun isScanning() = snapshots.isListening(this)

    class RoomHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onPickerItemSelected(viewState: PickerItemViewState) {
        itemClickListener?.onPickerItemSelected(viewState)
    }
}
