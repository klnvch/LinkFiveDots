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

package by.klnvch.link5dots.scores

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.FragmentScoresBinding
import by.klnvch.link5dots.domain.repositories.RoomDao
import by.klnvch.link5dots.models.Room
import by.klnvch.link5dots.scores.HistoryAdapter.OnItemClickListener
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class HistoryFragment : DaggerFragment(), OnItemClickListener {
    private lateinit var binding: FragmentScoresBinding

    @Inject
    lateinit var roomDao: RoomDao
    private val mCompositeDisposable = CompositeDisposable()
    private lateinit var mHistoryAdapter: HistoryAdapter

    abstract class SwipeToDeleteCallback : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScoresBinding.inflate(inflater, container, false)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        val swipeHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.recyclerView.adapter as HistoryAdapter
                val position = viewHolder.bindingAdapterPosition
                val room = adapter.get(position)

                mCompositeDisposable.add(Completable.fromAction { roomDao.deleteRoom(room) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { onRoomDeleted(position, room) })
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCompositeDisposable.add(roomDao.loadAll()
            .take(1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onDataLoaded(it) })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeDisposable.clear()
    }

    override fun onItemSelected(room: Room) {
        startActivity(Intent(context, GameInfoActivity::class.java).putExtra("room", room))
    }

    private fun onDataLoaded(rooms: List<Room>) {
        if (rooms.isEmpty()) {
            showError(R.string.search_no_results)
        } else {
            mHistoryAdapter = HistoryAdapter(rooms.toMutableList())
            binding.recyclerView.adapter = mHistoryAdapter
            mHistoryAdapter.onItemClickListener = this
        }
    }

    private fun onRoomDeleted(position: Int, room: Room) {
        mHistoryAdapter.removeAt(position)

        val msg = getString(R.string.deleted_toast, position.toString())
        Snackbar.make(binding.recyclerView, msg, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                mCompositeDisposable.add(Completable.fromAction { roomDao.insertRoom(room) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { onRoomInserted(position, room) })
            }
            .show()
    }

    private fun onRoomInserted(position: Int, room: Room) {
        mHistoryAdapter.insertAt(position, room)
    }

    private fun showError(msg: Int) {
        binding.recyclerView.visibility = View.GONE
        binding.textError.visibility = View.VISIBLE
        binding.textError.text = getString(msg)
    }
}
