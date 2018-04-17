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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import by.klnvch.link5dots.R
import by.klnvch.link5dots.db.AppDatabase
import by.klnvch.link5dots.models.Room
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_scores.*
import kotlinx.android.synthetic.main.fragment_scores.view.*


class HistoryFragment : Fragment() {

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

                mCompositeDisposable.add(Completable.fromAction { deleteRoom(room) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ onRoomDeleted(position) }
                        ) { throwable -> Log.e("HistoryFragment", "db error: ", throwable) })
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(view.recyclerView)

        return view
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onPause() {
        super.onPause()
        mCompositeDisposable.clear()
    }

    private fun loadData() {
        val disposable = AppDatabase.getDB(context!!).roomDao().loadAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rooms -> onDataLoaded(rooms) })

        mCompositeDisposable.add(disposable)
    }

    private fun onDataLoaded(rooms: List<Room>) {
        if (rooms.isEmpty()) {
            showError(R.string.search_settings_no_results)
        } else {
            view?.recyclerView?.adapter = HistoryAdapter(rooms.toMutableList())
        }
    }

    private fun deleteRoom(room: Room) {
        AppDatabase.getDB(context!!).roomDao().deleteRoom(room)
    }

    private fun onRoomDeleted(position: Int) {
        (view?.recyclerView?.adapter as HistoryAdapter).removeAt(position)
        val msg = getString(R.string.contacts_deleted_one_named_toast, position.toString())
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showError(msg: Int) {
        view?.recyclerView?.visibility = View.GONE
        view?.textError?.visibility = View.VISIBLE
        view?.textError?.text = getString(msg)
    }
}