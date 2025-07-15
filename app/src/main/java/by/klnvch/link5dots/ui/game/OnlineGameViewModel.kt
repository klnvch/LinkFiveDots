/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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
import by.klnvch.link5dots.domain.models.FeatureDisabled
import by.klnvch.link5dots.domain.models.NetworkRoomExtended
import by.klnvch.link5dots.domain.models.NetworkRoomState
import by.klnvch.link5dots.domain.models.NetworkRoomStateCreated
import by.klnvch.link5dots.domain.models.NetworkRoomStateDeleted
import by.klnvch.link5dots.domain.models.NetworkRoomStateFinished
import by.klnvch.link5dots.domain.models.NetworkRoomStateStarted
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
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
import by.klnvch.link5dots.domain.usecases.network.GetNetworkGameActionUseCase
import by.klnvch.link5dots.domain.usecases.network.GetNetworkRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.InitMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import by.klnvch.link5dots.ui.game.RoomToTitleMapper.actionToTitle
import by.klnvch.link5dots.ui.game.activities.ConnectError
import by.klnvch.link5dots.ui.game.activities.GameScreen
import by.klnvch.link5dots.ui.game.activities.InitError
import by.klnvch.link5dots.ui.game.activities.MultiplayerNavigationEvent
import by.klnvch.link5dots.ui.game.picker.PickerViewStateImpl
import by.klnvch.link5dots.ui.game.picker.states.createInitialPickerState
import by.klnvch.link5dots.ui.game.picker.toPickerViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class OnlineGameViewModel @Inject constructor(
    private val initMultiplayerUseCase: InitMultiplayerUseCase,
    private val createMultiplayerRoomUseCase: CreateMultiplayerRoomUseCase,
    private val getNetworkRoomStateUseCase: GetNetworkRoomStateUseCase,
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

    private val _pickerState = MutableStateFlow(createInitialPickerState())

    val pickerUiState = _pickerState
        .map { it.toPickerViewState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PickerViewStateImpl())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiTitleState = _pickerState.flatMapLatest {
        when {
            it.isCreating -> flowOf(R.string.connecting)
            it.isDeleting -> flowOf(R.string.connecting)
            it.isCreated -> flowOf(R.string.progress_text)
            it.isScanning -> flowOf(R.string.searching)
            it.isConnecting -> flowOf(R.string.connecting)
            it.isDisconnected -> flowOf(R.string.disconnected)
            it.isConnected -> roomFlow
                .map { room -> getNetworkGameActionUseCase.get(room) }
                .map { action -> actionToTitle(action) }

            else -> flowOf(0)
        }
    }

    private var scanJob: Job? = null

    lateinit var disconnectViewState: DisconnectViewState

    init {
        viewModelScope.launch {
            try {
                initMultiplayerUseCase.init()
            } catch (e: Throwable) {
                _navigationEvent.emit(InitError(e))
            }
        }
        viewModelScope.launch {
            roomFlow.collect {
                if (it is NetworkRoomExtended) {
                    disconnectViewState =
                        DisconnectViewState(getUserNameUseCase.get(it.opponent) ?: "")
                }
            }
        }
        viewModelScope.launch { getNetworkRoomStateUseCase.get().collect { onStatedChanged(it) } }
    }

    fun isConnected() = _pickerState.value.isConnected

    fun createRoom() {
        viewModelScope.launch {
            _pickerState.update { it.creating() }
            try {
                createMultiplayerRoomUseCase.create()
            } catch (e: Throwable) {
                when (e) {
                    is FeatureDisabled -> {
                        _pickerState.update { it.reset() }
                        _navigationEvent.emit(InitError(e))
                    }

                    else -> {
                        _pickerState.update { it.creationFailed(e) }
                    }
                }
            }
        }
    }

    fun deleteRoom() {
        _pickerState.update { it.deleting() }
        deleteMultiplayerRoomUseCase.delete()
    }

    fun startScan() {
        _pickerState.update { it.scanning() }
        scanJob = viewModelScope.launch {
            try {
                scanUseCase
                    .scan()
                    .onCompletion {
                        if (it == null) {
                            _pickerState.update { state -> state.scanDone() }
                        }
                    }
                    .catch { e -> _pickerState.update { it.scanFailed(e) } }
                    .collect { items -> _pickerState.update { it.scanning(items.toTypedArray()) } }
            } catch (e: Throwable) {
                _pickerState.update { it.reset() }
                _navigationEvent.emit(InitError(e))
            }
        }
    }

    fun stopScan() {
        _pickerState.update { it.reset() }
        scanJob?.cancel()
    }

    fun connect(descriptor: RemoteRoomDescriptor) {
        scanJob?.cancel()
        _pickerState.update { it.connecting() }
        viewModelScope.launch {
            try {
                connectRemoteRoomUseCase.connect(descriptor)
            } catch (e: Throwable) {
                _navigationEvent.emit(ConnectError(descriptor.title, e))
                startScan()
            }
        }
    }

    fun cleanUp() {
        if (_pickerState.value.isCreated) {
            deleteMultiplayerRoomUseCase.delete()
        }
        if (_pickerState.value.isConnected) {
            deleteMultiplayerRoomUseCase.finish()
        }
        _pickerState.update { it.reset() }
    }

    private suspend fun onStatedChanged(state: NetworkRoomState) {
        when (state) {
            is NetworkRoomStateCreated -> _pickerState.update { it.created(state.descriptor) }
            is NetworkRoomStateDeleted -> _pickerState.update { it.reset() }
            is NetworkRoomStateStarted -> onConnected(state.descriptor)
            is NetworkRoomStateFinished -> _pickerState.update { it.disconnected() }
        }
    }

    private suspend fun onConnected(descriptor: RemoteRoomDescriptor) {
        _pickerState.update { it.connected(descriptor) }
        setParam(RoomByDescriptor(descriptor))
        _navigationEvent.emit(GameScreen)
    }
}

data class DisconnectViewState(val name: String)
