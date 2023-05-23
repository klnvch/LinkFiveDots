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

import android.util.Log
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.NetworkRoomExtended
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.Analytics
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.GetUserNameUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreUseCase
import by.klnvch.link5dots.domain.usecases.RoomByDescriptor
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.ConnectRemoteRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.DeleteMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.GetMultiplayerRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.GetNetworkGameActionUseCase
import by.klnvch.link5dots.domain.usecases.network.InitMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import by.klnvch.link5dots.ui.game.RoomToTitleMapper.actionToTitle
import by.klnvch.link5dots.ui.game.activities.ConnectError
import by.klnvch.link5dots.ui.game.activities.GameScreen
import by.klnvch.link5dots.ui.game.activities.InitError
import by.klnvch.link5dots.ui.game.activities.MultiplayerNavigationEvent
import by.klnvch.link5dots.ui.game.picker.ConnectConnected
import by.klnvch.link5dots.ui.game.picker.ConnectConnecting
import by.klnvch.link5dots.ui.game.picker.ConnectDisconnected
import by.klnvch.link5dots.ui.game.picker.PickerViewState
import by.klnvch.link5dots.ui.game.picker.ScanOn
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
    private val initMultiplayerUseCase: InitMultiplayerUseCase,
    private val createMultiplayerRoomUseCase: CreateMultiplayerRoomUseCase,
    private val getStateUseCase: GetMultiplayerRoomStateUseCase,
    private val deleteMultiplayerRoomUseCase: DeleteMultiplayerRoomUseCase,
    private val scanUseCase: ScanUseCase,
    private val connectRemoteRoomUseCase: ConnectRemoteRoomUseCase,
    private val getNetworkGameActionUseCase: GetNetworkGameActionUseCase,
    private val getUserNameUseCase: GetUserNameUseCase,
    getRoomUseCase: GetRoomUseCase,
    newGameUseCase: NewGameUseCase,
    addDotUseCase: AddDotUseCase,
    undoMoveUseCase: UndoMoveUseCase,
    prepareScoreUseCase: PrepareScoreUseCase,
    analytics: Analytics,
    saveScoreUseCase: SaveScoreUseCase,
    settings: Settings,
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
            it.scanState is ScanOn -> flowOf(R.string.searching)
            it.connectState is ConnectConnecting -> flowOf(R.string.connecting)
            it.connectState is ConnectDisconnected -> flowOf(R.string.disconnected)
            it.connectState is ConnectConnected -> roomFlow
                .map { room -> getNetworkGameActionUseCase.get(room) }
                .map { action -> actionToTitle(action) }

            else -> flowOf(0)
        }
    }

    private var roomStatesJob: Job? = null
    private var scanJob: Job? = null

    lateinit var disconnectViewState: DisconnectViewState

    init {
        viewModelScope.launch {
            try {
                initMultiplayerUseCase.init()
                _pickerUiState.value = PickerViewState.IDLE
            } catch (e: Error) {
                _pickerUiState.value = PickerViewState.ERROR
                _navigationEvent.emit(InitError)
            }
        }
        viewModelScope.launch {
            roomFlow.collect {
                if (it is NetworkRoomExtended) {
                    disconnectViewState =
                        DisconnectViewState(getUserNameUseCase.get(it.opponent) ?: "")
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
            val descriptor = createMultiplayerRoomUseCase.create()
            getStateUseCase.get(descriptor).collect {
                when (it) {
                    RoomState.CREATED -> {
                        _pickerUiState.value =
                            PickerViewState.created(PickerItemViewState(descriptor))
                    }

                    RoomState.STARTED -> {
                        onConnected(descriptor)
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

    fun deleteRoom(descriptor: RemoteRoomDescriptor) {
        _pickerUiState.value = PickerViewState.TARGET_DELETING
        deleteMultiplayerRoomUseCase.delete(descriptor)
    }

    fun startScan() {
        _pickerUiState.value = PickerViewState.scanning(emptyList())
        scanJob = viewModelScope.launch {
            scanUseCase.scan().collect { list ->
                _pickerUiState.value =
                    PickerViewState.scanning(list.map { PickerItemViewState(it) })
            }
        }
    }

    fun stopScan() {
        _pickerUiState.value = PickerViewState.IDLE
        scanJob?.cancel()
    }

    fun connect(descriptor: RemoteRoomDescriptor) {
        _pickerUiState.value = PickerViewState.CONNECTING
        viewModelScope.launch {
            try {
                connectRemoteRoomUseCase.connect(descriptor)
                onConnected(descriptor)
                scanJob?.cancel()
            } catch (e: Throwable) {
                Log.e("Multiplayer", "${e.message}")
                _navigationEvent.emit(ConnectError(descriptor.title))
                stopScan()
            }
        }
    }

    fun cleanUp() {
        val pickerState = pickerUiState.value
        val targetState = pickerState.targetState
        if (targetState is TargetCreated) {
            deleteMultiplayerRoomUseCase.delete(targetState.descriptor)
        }
        val connectState = pickerState.connectState
        if (connectState is ConnectConnected) {
            deleteMultiplayerRoomUseCase.finish(connectState.descriptor)
        }
        _pickerUiState.value = PickerViewState.IDLE
    }

    private suspend fun onConnected(descriptor: RemoteRoomDescriptor) {
        _pickerUiState.value = PickerViewState.connected(descriptor)
        setParam(RoomByDescriptor(descriptor))
        _navigationEvent.emit(GameScreen)
    }
}

data class DisconnectViewState(val name: String)
