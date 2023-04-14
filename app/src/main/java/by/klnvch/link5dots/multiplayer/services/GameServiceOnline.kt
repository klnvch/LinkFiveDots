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

package by.klnvch.link5dots.multiplayer.services

import android.util.Log
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.usecases.network.*
import by.klnvch.link5dots.multiplayer.targets.Target
import by.klnvch.link5dots.multiplayer.targets.TargetOnline
import by.klnvch.link5dots.multiplayer.utils.OnTargetDeletedListener
import kotlinx.coroutines.*
import javax.inject.Inject

/*
    HOST                        GUEST

    INITIALIZATION PHASE:
    1. STATE_TARGET_DELETED     1. STATE_TARGET_DELETED
    2. STATE_SCAN_OFF           2. STATE_SCAN_OFF

    CONNECTING PHASE:
    3. STATE_TARGET_CREATING    3. STATE_SCAN_ON
    4. STATE_TARGET_CREATED

    PLAYING PHASE:
    5. STATE_CONNECTED          4. STATE_CONNECTED
 */
class GameServiceOnline : GameService() {
    @Inject
    lateinit var getOnlineRoomUseCase: GetOnlineRoomUseCase

    @Inject
    lateinit var createOnlineRoomUseCase: CreateOnlineRoomUseCase

    @Inject
    lateinit var getOnlineRoomStateUseCase: GetOnlineRoomStateUseCase

    @Inject
    lateinit var updateOnlineRoomStateUseCase: UpdateOnlineRoomStateUseCase

    @Inject
    lateinit var connectOnlineRoomUseCase: ConnectOnlineRoomUseCase

    @Inject
    lateinit var addDotUseCase: AddDotUseCase

    @Inject
    lateinit var getNetworkUserUseCase: GetNetworkUserUseCase

    private var roomUpdatesJob: Job? = null
    private var roomStatesJob: Job? = null
    private var createdRoom: NetworkRoom? = null

    private lateinit var mUser: NetworkUser

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            mUser = getNetworkUserUseCase.get()
        }
    }

    override fun onDestroy() {
        roomUpdatesJob?.cancel()
        deleteRoom(RoomState.DELETED, null)
        super.onDestroy()
    }

    override fun createTarget() {
        super.createTarget()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val room = createOnlineRoomUseCase.create().also { createdRoom = it }
                roomStatesJob = launch {
                    getOnlineRoomStateUseCase.get(room.key).collect {
                        when (it) {
                            RoomState.CREATED -> onTargetCreated(TargetOnline(room))
                            RoomState.STARTED -> {
                                onRoomConnected(room)
                                coroutineContext.job.cancel()
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                onTargetCreationFailed(e)
                deleteRoom(RoomState.DELETED, null)
            }
        }
    }

    override fun deleteTarget() {
        super.deleteTarget()
        deleteRoom(RoomState.DELETED, this)
    }

    override fun connect(target: Target<*>) {
        super.connect(target)
        val room = (target as TargetOnline).target

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedRoom = connectOnlineRoomUseCase.connect(room)
                onRoomConnected(updatedRoom)
            } catch (e: Exception) {
                onRoomConnectFailed(e)
            }
        }
    }

    override fun getUser(): NetworkUser {
        return mUser
    }

    override fun newGame() {
        throw UnsupportedOperationException()
    }

    override fun reset() {
        roomUpdatesJob?.cancel()
        deleteRoom(RoomState.FINISHED, null)
        super.reset()
    }

    override fun getTarget(): Target<*>? {
        val room = this.room
        return if (room != null) {
            TargetOnline(room)
        } else {
            Log.e(TAG, "target is null")
            null
        }
    }

    override fun onTargetCreated(target: Target<*>) {
        room = (target as TargetOnline).target
        super.onTargetCreated(target)
    }

    override fun onTargetDeleted(exception: Exception?) {
        room = null
        super.onTargetDeleted(exception)
    }

    override fun updateRoomLocally(room: NetworkRoom) {
        sendMsg(room)
    }

    override fun updateRoomRemotely(room: NetworkRoom) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                addDotUseCase.addDot(room, room.getLatsDot()!!)
            } catch (e: Exception) {
                onRoomUpdated(null, e)
            }
        }
    }

    override fun startGame(room: NetworkRoom?) {
        setRoom(room)
        roomUpdatesJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                getOnlineRoomUseCase.get(room!!.key).collect {
                    onRoomUpdated(it, null)
                }
            } catch (e: Exception) {
                onRoomUpdated(null, e)
            }
        }
    }

    private fun deleteRoom(state: Int, listener: OnTargetDeletedListener?) {
        roomStatesJob?.cancel()
        val room = createdRoom
        if (room != null) {
            createdRoom = null
            CoroutineScope(Dispatchers.IO).launch {
                val error = try {
                    updateOnlineRoomStateUseCase.update(room.key, state)
                    null
                } catch (e: Exception) {
                    e
                }
                withContext(Dispatchers.Main) {
                    listener?.onTargetDeleted(error)
                }
            }
        }
    }

    companion object {
        const val TAG = "OnlineGameService"
    }
}
