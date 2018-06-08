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

package by.klnvch.link5dots

import android.content.Context
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.util.Log
import android.view.*
import android.widget.TextView
import by.klnvch.link5dots.db.RoomDao
import by.klnvch.link5dots.models.*
import by.klnvch.link5dots.settings.SettingsUtils
import by.klnvch.link5dots.utils.RoomUtils
import com.crashlytics.android.Crashlytics
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.game_board.*
import javax.inject.Inject

class GameFragment : DaggerFragment() {
    private val mDisposables = CompositeDisposable()
    private lateinit var mListener: OnGameListener
    @Inject
    lateinit var roomDao: RoomDao
    @Inject
    lateinit var settingsUtils: SettingsUtils

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            mListener = context as OnGameListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement OnGameListener")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.game_board, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        gameView.setOnMoveDoneListener { onMoveDone(it) }
        gameView.setOnGameEndListener { onGameFinished() }

        if (savedInstanceState != null) {
            gameView.viewState = GameViewState.fromJson(savedInstanceState.getString(KEY_VIEW_STATE))
        }

        mDisposables.add(settingsUtils.dotsType
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.setDotsType(it) })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_VIEW_STATE, gameView.viewState.toJson())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        mDisposables.clear()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_game_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.menu_search -> {
                gameView.switchHideArrow()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setDotsType(@SettingsUtils.DotsType dotsType: Int) {
        gameView.init(context!!, dotsType)

        if (dotsType == SettingsUtils.DOTS_TYPE_ORIGINAL) {
            setLeftDrawable(textUser1, R.drawable.game_dot_circle_red)
            setLeftDrawable(textUser2, R.drawable.game_dot_circle_blue)
        } else {
            setLeftDrawable(textUser1, R.drawable.game_dot_cross_red)
            setLeftDrawable(textUser2, R.drawable.game_dot_ring_blue)
        }
    }

    private fun setLeftDrawable(view: TextView, @DrawableRes drawable: Int) {
        view.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
    }

    fun update(room: Room) {

        val user = mListener.getUser()

        if (user != null) {
            gameInfo.visibility = View.VISIBLE

            val user1 = room.user1
            val user2 = room.user2

            val hostDotType = RoomUtils.getHostDotType(room, user)

            if (hostDotType == Dot.HOST) {
                if (user1 != null) textUser1.text = user1.name
                if (user2 != null) textUser2.text = user2.name
            } else {
                if (user2 != null) textUser1.text = user2.name
                if (user1 != null) textUser2.text = user1.name
            }

            gameView.setGameState(Game.createGame(room.dots, hostDotType))
        } else {
            gameInfo.visibility = View.GONE
            gameView.setGameState(Game.createGame(room.dots, Dot.HOST))
        }

        // add non-empty to the database
        mDisposables.add(Observable.just(room)
                .filter { !RoomUtils.isEmpty(it) }
                .doOnNext { roomDao.insertRoom(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { Log.d(TAG, MSG_DB_INSERT_SUCCESS) },
                        { Crashlytics.logException(it) }))
    }

    fun reset() {
        gameView.newGame(null)
    }

    private fun onMoveDone(dot: Dot) {
        mListener.onMoveDone(dot)
    }

    private fun onGameFinished() {
        mListener.onGameFinished()
    }

    fun focus() {
        gameView.switchHideArrow()
    }

    interface OnGameListener {
        fun getUser(): User?

        fun onMoveDone(dot: Dot)

        fun onGameFinished()
    }

    companion object {
        const val TAG = "GameFragment"
        private const val KEY_VIEW_STATE = "KEY_VIEW_STATE"
        private const val MSG_DB_INSERT_SUCCESS = "room inserted successfully"
    }
}