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
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.klnvch.link5dots.R
import by.klnvch.link5dots.db.RoomDao
import by.klnvch.link5dots.models.Room
import by.klnvch.link5dots.scores.HistoryAdapter.OnItemClickListener
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_scores.*
import kotlinx.android.synthetic.main.fragment_scores.view.*
import javax.inject.Inject


class HistoryFragment : DaggerFragment(), OnItemClickListener {

    @Inject
    lateinit var roomDao: RoomDao
    private val mCompositeDisposable = CompositeDisposable()

    abstract class SwipeToDeleteCallback : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView?,
                            viewHolder: RecyclerView.ViewHolder?,
                            target: RecyclerView.ViewHolder?): Boolean {
            return false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scores, container, false)

        view.recyclerView.setHasFixedSize(true)
        view.recyclerView.layoutManager = LinearLayoutManager(context)

        val swipeHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                val adapter = recyclerView.adapter as HistoryAdapter
                val position = viewHolder!!.adapterPosition
                val room = adapter.get(position)

                mCompositeDisposable.add(Completable.fromAction { roomDao.deleteRoom(room) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { onRoomDeleted(position, room) })
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(view.recyclerView)

        return view
    }

    override fun onStart() {
        super.onStart()

        mCompositeDisposable.add(roomDao.loadAll()
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onDataLoaded(it) })
    }

    override fun onStop() {
        super.onStop()
        mCompositeDisposable.clear()
    }

    override fun onItemSelected(room: Room) {
        startActivity(Intent(context, GameInfoActivity::class.java).putExtra("room", room))
    }

    private fun onDataLoaded(rooms: List<Room>) {
        if (rooms.isEmpty()) {
            showError(R.string.search_no_results)
        } else {
            val adapter = HistoryAdapter(rooms.toMutableList())
            view?.recyclerView?.adapter = adapter
            adapter.onItemClickListener = this
        }
    }

    private fun onRoomDeleted(position: Int, room: Room) {
        (view?.recyclerView?.adapter as HistoryAdapter).removeAt(position)

        val msg = getString(R.string.deleted_toast, position.toString())
        Snackbar.make(view!!.recyclerView!!, msg, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    mCompositeDisposable.add(Completable.fromAction { roomDao.insertRoom(room) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { onRoomInserted(position, room) })
                }
                .show()
    }

    private fun onRoomInserted(position: Int, room: Room) {
        (view?.recyclerView?.adapter as HistoryAdapter).insertAt(position, room)
    }

    private fun showError(msg: Int) {
        view?.recyclerView?.visibility = View.GONE
        view?.textError?.visibility = View.VISIBLE
        view?.textError?.text = getString(msg)
    }
}