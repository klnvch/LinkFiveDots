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
import android.view.*
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import by.klnvch.link5dots.databinding.GameBoardBinding
import by.klnvch.link5dots.domain.models.*
import by.klnvch.link5dots.domain.models.RoomExt.getHostDotType
import by.klnvch.link5dots.domain.repositories.Analytics
import by.klnvch.link5dots.domain.repositories.RoomLocalDataSource
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.models.Game
import by.klnvch.link5dots.models.GameViewState
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GameFragment : DaggerFragment() {
    private var mListener: OnGameListener? = null
    private lateinit var binding: GameBoardBinding

    @Inject
    lateinit var roomLocalDataSource: RoomLocalDataSource

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var analytics: Analytics

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnGameListener) {
            mListener = context
        }
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

        lifecycleScope.launch {
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

    fun update(room: IRoom) {
        val user1 = room.user1
        val user2 = room.user2

        if (user1 != null) {
            binding.gameInfo.visibility = View.VISIBLE

            val hostDotType = room.getHostDotType(user1)

            if (hostDotType == Dot.HOST) {
                binding.textUser1.text = getUserName(user1)
                binding.textUser2.text = getUserName(user2)
            } else {
                binding.textUser1.text = getUserName(user2)
                binding.textUser2.text = getUserName(user1)
            }

            binding.gameView.setGameState(Game.createGame(room.dots, hostDotType))
        } else {
            binding.gameInfo.visibility = View.GONE
            binding.gameView.setGameState(Game.createGame(room.dots, Dot.HOST))
        }

        // add non-empty to the database
        lifecycleScope.launch {
            roomLocalDataSource.save(room)
        }
    }

    private fun getUserName(user: IUser?): String? {
        return when (user) {
            is BotUser -> getString(R.string.computer)
            is NetworkUser -> user.name
            is DeviceOwnerUser -> settings.getUserNameBlocking()
                .ifEmpty { getString(R.string.unknown) }
            else -> null
        }
    }

    fun reset() {
        binding.gameView.newGame(null)
    }

    private fun onMoveDone(dot: Point) {
        mListener?.onMoveDone(dot)
    }

    private fun onGameFinished() {
        mListener?.onGameFinished()
    }

    fun focus(): Boolean {
        analytics.logEvent(Analytics.EVENT_SEARCH)
        binding.gameView.focus()
        return true
    }

    interface OnGameListener {
        fun getUser(): IUser?

        fun onMoveDone(dot: Point)

        fun onGameFinished()
    }

    companion object {
        const val TAG = "GameFragment"
        private const val KEY_VIEW_STATE = "KEY_VIEW_STATE"
    }
}
