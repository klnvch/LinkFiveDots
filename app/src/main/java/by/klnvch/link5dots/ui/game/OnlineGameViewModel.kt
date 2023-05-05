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
package by.klnvch.link5dots.ui.game

import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.Analytics
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.GetUserNameUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreUseCase
import by.klnvch.link5dots.domain.usecases.RoomByKey
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.ConnectOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.DisconnectOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.GetOnlineRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.InitUseCase
import by.klnvch.link5dots.domain.usecases.network.UpdateOnlineRoomStateUseCase
import by.klnvch.link5dots.ui.game.RoomToTitleMapper.roomToTitle
import by.klnvch.link5dots.ui.game.activities.MultiplayerNavigationEvent
import by.klnvch.link5dots.ui.game.picker.ConnectConnected
import by.klnvch.link5dots.ui.game.picker.ConnectConnecting
import by.klnvch.link5dots.ui.game.picker.ConnectDisconnected
import by.klnvch.link5dots.ui.game.picker.PickerViewState
import by.klnvch.link5dots.ui.game.picker.ScanState
import by.klnvch.link5dots.ui.game.picker.TargetCreated
import by.klnvch.link5dots.ui.game.picker.TargetCreating
import by.klnvch.link5dots.ui.game.picker.TargetDeleting
import by.klnvch.link5dots.ui.game.picker.adapters.PickerItemViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import javax.inject.Inject

class OnlineGameViewModel @Inject constructor(
    private val initUseCase: InitUseCase,
    private val createOnlineRoomUseCase: CreateOnlineRoomUseCase,
    private val getOnlineRoomStateUseCase: GetOnlineRoomStateUseCase,
    private val updateOnlineRoomStateUseCase: UpdateOnlineRoomStateUseCase,
    private val connectOnlineRoomUseCase: ConnectOnlineRoomUseCase,
    private val disconnectOnlineRoomUseCase: DisconnectOnlineRoomUseCase,
    private val firebaseManager: FirebaseManager,
    getRoomUseCase: GetRoomUseCase,
    newGameUseCase: NewGameUseCase,
    addDotUseCase: AddDotUseCase,
    undoMoveUseCase: UndoMoveUseCase,
    prepareScoreUseCase: PrepareScoreUseCase,
    analytics: Analytics,
    saveScoreUseCase: SaveScoreUseCase,
    settings: Settings,
    private val getUserNameUseCase: GetUserNameUseCase,
) : OfflineGameViewModel(
    getRoomUseCase,
    newGameUseCase,
    addDotUseCase,
    undoMoveUseCase,
    prepareScoreUseCase,
    analytics,
    saveScoreUseCase,
    settings,
    getUserNameUseCase,
) {
    private val _navigationEvent = MutableSharedFlow<MultiplayerNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _pickerUiState = MutableStateFlow(PickerViewState.INITIAL)
    val pickerUiState = _pickerUiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiTitleState = _pickerUiState.flatMapLatest {
        when {
            it.targetState is TargetCreating -> flowOf(R.string.connecting)
            it.targetState is TargetDeleting -> flowOf(R.string.connecting)
            it.targetState is TargetCreated -> flowOf(R.string.progress_text)
            it.scanState == ScanState.ON -> flowOf(R.string.searching)
            it.connectState is ConnectConnecting -> flowOf(R.string.connecting)
            it.connectState is ConnectDisconnected -> flowOf(R.string.disconnected)
            it.connectState is ConnectConnected ->
                roomFlow.map { room ->
                    val userId = firebaseManager.getUserId()
                    return@map if (room is NetworkRoom && userId != null) {
                        roomToTitle(room, userId)
                    } else {
                        0
                    }
                }

            else -> flowOf(0)
        }
    }

    private var roomStatesJob: Job? = null

    lateinit var disconnectViewState: DisconnectViewState

    init {
        viewModelScope.launch {
            try {
                initUseCase.init()
                _pickerUiState.value = PickerViewState.IDLE
            } catch (e: Error) {
                _pickerUiState.value = PickerViewState.ERROR
                _navigationEvent.emit(MultiplayerNavigationEvent.ERROR)
            }
        }
        viewModelScope.launch {
            roomFlow.collect {
                if (it is NetworkRoom) {
                    disconnectViewState = if (it.user1.id == firebaseManager.getUserId()) {
                        DisconnectViewState(getUserNameUseCase.get(it.user2) ?: "")
                    } else {
                        DisconnectViewState(getUserNameUseCase.get(it.user1) ?: "")
                    }
                    if (it.state == RoomState.FINISHED) {
                        _pickerUiState.value = PickerViewState.DISCONNECTED
                    }
                }
            }
        }
    }

    fun isConnected() = pickerUiState.value.connectState is ConnectConnected

    fun createRoom() {
        roomStatesJob = viewModelScope.launch {
            _pickerUiState.value = PickerViewState.TARGET_CREATING
            val room = createOnlineRoomUseCase.create()
            val key = room.key
            getOnlineRoomStateUseCase.get(room.key).collect {
                when (it) {
                    RoomState.CREATED -> {
                        val userName = getUserNameUseCase.get(room.user1)
                        val itemViewState = PickerItemViewState(key, room.timestamp, userName!!)
                        _pickerUiState.value = PickerViewState.created(itemViewState)
                    }

                    RoomState.STARTED -> {
                        onConnected(key)
                        coroutineContext.job.cancel()
                    }

                    RoomState.DELETED -> {
                        _pickerUiState.value = PickerViewState.IDLE
                        coroutineContext.job.cancel()
                    }
                }
            }
        }
    }

    fun deleteRoom(key: String) {
        _pickerUiState.value = PickerViewState.TARGET_DELETING
        viewModelScope.launch {
            updateOnlineRoomStateUseCase.update(key, RoomState.DELETED)
        }
    }

    fun startScan() {
        _pickerUiState.value = PickerViewState.START_SCAN
    }

    fun stopScan() {
        _pickerUiState.value = PickerViewState.IDLE
    }

    fun connect(key: String) {
        _pickerUiState.value = PickerViewState.CONNECTING
        viewModelScope.launch {
            connectOnlineRoomUseCase.connect(key)
            onConnected(key)
        }
    }

    fun cleanUp() {
        val pickerState = pickerUiState.value
        val targetState = pickerState.targetState
        if (targetState is TargetCreated) {
            disconnectOnlineRoomUseCase.disconnect(targetState.key)
        }
        val connectState = pickerState.connectState
        if (connectState is ConnectConnected) {
            disconnectOnlineRoomUseCase.disconnect(connectState.key)
        }
        _pickerUiState.value = PickerViewState.IDLE
    }

    private suspend fun onConnected(key: String) {
        _pickerUiState.value = PickerViewState.connected(key)
        setParam(RoomByKey(key))
        _navigationEvent.emit(MultiplayerNavigationEvent.GAME)
    }
}

data class DisconnectViewState(val name: String)
