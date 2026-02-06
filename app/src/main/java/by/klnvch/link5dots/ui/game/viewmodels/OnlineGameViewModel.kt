package by.klnvch.link5dots.ui.game.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.application.services.gameRoomOrchestrator.GameRoomOrchestrator
import by.klnvch.link5dots.domain.events.DomainHandler
import by.klnvch.link5dots.domain.models.FoundRemoteRoom
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.NetworkRoomState
import by.klnvch.link5dots.domain.models.NetworkRoomStateCreated
import by.klnvch.link5dots.domain.models.NetworkRoomStateDeleted
import by.klnvch.link5dots.domain.models.NetworkRoomStateFinished
import by.klnvch.link5dots.domain.models.NetworkRoomStateStarted
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.CleanMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.GetNetworkGameActionUseCase
import by.klnvch.link5dots.domain.usecases.network.GetNetworkRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.InitMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import by.klnvch.link5dots.ui.game.picker.PickerViewStateImpl
import by.klnvch.link5dots.ui.game.picker.states.createInitialPickerState
import by.klnvch.link5dots.ui.game.picker.toPickerViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    private val cleanMultiplayerRoomUseCase: CleanMultiplayerRoomUseCase,
    private val scanUseCase: ScanUseCase,
    private val getNetworkGameActionUseCase: GetNetworkGameActionUseCase,
    handlers: Set<@JvmSuppressWildcards DomainHandler>,
    getRoomUseCase: GameRoomOrchestrator,
    newGameUseCase: NewGameUseCase,
    addDotUseCase: AddDotUseCase,
    undoMoveUseCase: UndoMoveUseCase,
    saveScoreUseCase: SaveScoreUseCase,
) : OfflineGameViewModel(
    getRoomUseCase,
    newGameUseCase,
    addDotUseCase,
    undoMoveUseCase,
    saveScoreUseCase,
) {
    private val _pickerState = MutableStateFlow(createInitialPickerState())

    val pickerUiState = _pickerState
        .map { it.toPickerViewState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PickerViewStateImpl())

    val uiTitleState = combine(
        roomFlow.map { it.room as? INetworkRoom },
        _pickerState
    ) { room, pickerState -> getNetworkGameActionUseCase.get(pickerState, room) }

    private var scanJob: Job? = null

    init {
        Log.d("VM", "${handlers.size} handlers activated")
        viewModelScope.launch {
            try {
                initMultiplayerUseCase.init()
            } catch (e: Throwable) {
                _pickerState.update { it.failed(e) }
            }
        }
        viewModelScope.launch { getNetworkRoomStateUseCase.get().collect { onStatedChanged(it) } }
    }

    fun createRoom() {
        viewModelScope.launch {
            _pickerState.update { it.creating() }
            try {
                createMultiplayerRoomUseCase.create()
            } catch (e: Throwable) {
                _pickerState.update { it.failed(e) }
            }
        }
    }

    fun deleteRoom() {
        _pickerState.update { it.deleting() }
        cleanMultiplayerRoomUseCase.delete()
    }

    fun startScan(e: Throwable? = null) {
        _pickerState.update { it.scanning() }
        scanJob = viewModelScope.launch {
            scanUseCase
                .scan()
                .onCompletion {
                    if (it == null) {
                        _pickerState.update { state -> state.scanDone(e) }
                    }
                }
                .catch { e -> _pickerState.update { it.failed(e) } }
                .collect { items -> _pickerState.update { it.scanning(items.toTypedArray()) } }
        }
    }

    fun stopScan() {
        _pickerState.update { it.reset() }
        scanJob?.cancel()
    }

    fun connect(invitation: FoundRemoteRoom) {
        scanJob?.cancel()
        _pickerState.update { it.connecting() }
        invitation.connect({}, { e -> _pickerState.update { it.failed(e) } })
    }

    fun setError(e: Throwable) = _pickerState.update { it.failed(e) }

    fun exitGame() {
        if (_pickerState.value.isConnected) {
            cleanMultiplayerRoomUseCase.finish()
        }
        _pickerState.update { it.reset() }
    }

    private fun onStatedChanged(state: NetworkRoomState) {
        when (state) {
            is NetworkRoomStateCreated -> _pickerState.update { it.created(state.descriptor) }
            is NetworkRoomStateDeleted -> _pickerState.update { it.reset() }
            is NetworkRoomStateStarted -> _pickerState.update { it.connected(state.descriptor) }
            is NetworkRoomStateFinished -> _pickerState.update { it.disconnected() }
        }
    }
}
