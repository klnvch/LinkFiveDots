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

package by.klnvch.link5dots

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import by.klnvch.link5dots.databinding.GameBoardBinding
import by.klnvch.link5dots.domain.repositories.RoomDao
import by.klnvch.link5dots.domain.models.DotsType
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.models.*
import by.klnvch.link5dots.utils.RoomUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GameFragment : DaggerFragment() {
    private val mDisposables = CompositeDisposable()
    private lateinit var mListener: OnGameListener
    private lateinit var binding: GameBoardBinding

    @Inject
    lateinit var roomDao: RoomDao

    @Inject
    lateinit var settings: Settings

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnGameListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GameBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gameView.setOnMoveDoneListener { onMoveDone(it) }
        binding.gameView.setOnGameEndListener { onGameFinished() }

        if (savedInstanceState != null) {
            binding.gameView.viewState =
                GameViewState.fromJson(savedInstanceState.getString(KEY_VIEW_STATE))
        }

        CoroutineScope(Dispatchers.IO).launch {
            settings.getDotsType().collect {
                withContext(Dispatchers.Main) {
                    setDotsType(it)
                }
            }
        }

        setupMenu()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_game_fragment, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_search -> focus()
                    else -> true
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_VIEW_STATE, binding.gameView.viewState.toJson())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        mDisposables.clear()
        super.onDestroyView()
    }

    private fun setDotsType(dotsType: Int) {
        binding.gameView.init(requireContext(), dotsType)

        if (dotsType == DotsType.ORIGINAL) {
            setLeftDrawable(binding.textUser1, R.drawable.game_dot_circle_red)
            setLeftDrawable(binding.textUser2, R.drawable.game_dot_circle_blue)
        } else {
            setLeftDrawable(binding.textUser1, R.drawable.game_dot_cross_red)
            setLeftDrawable(binding.textUser2, R.drawable.game_dot_ring_blue)
        }
    }

    private fun setLeftDrawable(view: TextView, @DrawableRes drawable: Int) {
        view.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
    }

    fun update(room: Room) {

        val user = mListener.getUser()

        if (user != null) {
            binding.gameInfo.visibility = View.VISIBLE

            val user1 = room.user1
            val user2 = room.user2

            val hostDotType = RoomUtils.getHostDotType(room, user)

            if (hostDotType == Dot.HOST) {
                binding.textUser1.text = user1?.name
                binding.textUser2.text = user2?.name
            } else {
                binding.textUser1.text = user2?.name
                binding.textUser2.text = user1?.name
            }

            binding.gameView.setGameState(Game.createGame(room.dots, hostDotType))
        } else {
            binding.gameInfo.visibility = View.GONE
            binding.gameView.setGameState(Game.createGame(room.dots, Dot.HOST))
        }

        // add non-empty to the database
        mDisposables.add(Observable.just(room)
            .filter { !RoomUtils.isEmpty(it) }
            .doOnNext { roomDao.insertRoom(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.d(TAG, MSG_DB_INSERT_SUCCESS) },
                { FirebaseCrashlytics.getInstance().recordException(it) })
        )
    }

    fun reset() {
        binding.gameView.newGame(null)
    }

    private fun onMoveDone(dot: Dot) {
        mListener.onMoveDone(dot)
    }

    private fun onGameFinished() {
        mListener.onGameFinished()
    }

    fun focus(): Boolean {
        binding.gameView.focus()
        return true
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
